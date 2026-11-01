"""Recipe-cost updates must not silently expand the available transmutation catalogue."""
import importlib.util
import io
import json
from contextlib import redirect_stdout
from pathlib import Path
from tempfile import TemporaryDirectory
from unittest.mock import patch
import unittest
import zipfile

spec=importlib.util.spec_from_file_location('alchemy_value_builder',Path(__file__).resolve().parents[2]/'tools/build_alchemy_values.py')
builder=importlib.util.module_from_spec(spec)
spec.loader.exec_module(builder)


def record(value):return dict(numerator=value,denominator=1,source='test fixture')


class AlchemyValueRecalculationTests(unittest.TestCase):
    def test_reduces_transitive_consumers_without_adding_items_or_changing_unrelated_prices(self):
        existing={name:record(n) for name,n in [('circuit',100),('machine',200),('upgrade',300),('unrelated',50)]}
        computed={name:record(n) for name,n in [('circuit',25),('machine',125),('upgrade',225),('unrelated',1),('new_item',1)]}
        # Reverse dependency order requires a complete walk, including alternatives in ingredient tags.
        edges=[('upgrade_recipe',[{'machine'}],'upgrade',1),
               ('machine_recipe',[{'circuit','other_circuit'}],'machine',1)]
        result=builder.limit_recalculation(existing,computed,edges,['circuit'])
        self.assertEqual(set(result),set(existing))
        for name in ('circuit','machine','upgrade'):self.assertEqual(result[name],computed[name])
        self.assertEqual(result['unrelated'],existing['unrelated'])
        self.assertEqual(existing['circuit'],record(100),'Input catalogue is not mutated')

    def test_never_increases_an_existing_redemption_value(self):
        existing={'circuit':record(100),'machine':record(20)}
        result=builder.limit_recalculation(existing,{'circuit':record(25),'machine':record(30)},
                [('machine_recipe',[{'circuit'}],'machine',1)],['circuit'])
        self.assertEqual(result['machine'],existing['machine'])

    def test_rejects_an_unknown_scope_instead_of_enabling_a_new_item(self):
        with self.assertRaises(ValueError):builder.limit_recalculation({}, {'new_item':record(1)}, [], ['new_item'])

    def test_scoped_build_uses_current_ingredient_prices_and_preserves_catalogue_membership(self):
        with TemporaryDirectory() as directory:
            root=Path(directory)
            def write(path,data):
                target=root/path;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(json.dumps(data),encoding='utf-8')
            write('tools/data/alchemy_value_seeds.json',{'items':{'minecraft:quartz':1,'jugcraft:raw':900},'tags':{}})
            existing={'jugcraft:'+name:record(n) for name,n in [('raw',4),('component',100),('machine',200),('unrelated',50)]}
            write('src/main/resources/data/jugcraft/alchemy/values.json',{'items':existing})
            for name,inputs,count in [('component',['raw','raw'],4),('machine',['component','raw'],1),('new_item',['component'],1),('unrelated',['raw'],8)]:
                write('src/main/resources/data/jugcraft/recipe/'+name+'.json',{
                    'type':'minecraft:crafting_shapeless','ingredients':['jugcraft:'+x for x in inputs],
                    'result':{'id':'jugcraft:'+name,'count':count}})
            jar=root/'vanilla.zip'
            with zipfile.ZipFile(jar,'w'):pass
            with patch.object(builder,'ROOT',root),redirect_stdout(io.StringIO()):
                result=builder.build(jar,['jugcraft:component'])
            self.assertEqual(set(result),set(existing))
            self.assertEqual(result['jugcraft:raw'],existing['jugcraft:raw'])
            self.assertEqual(result['jugcraft:unrelated'],existing['jugcraft:unrelated'])
            self.assertEqual(result['jugcraft:component']['numerator'],2)
            self.assertEqual(result['jugcraft:machine']['numerator'],6)

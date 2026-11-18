"""World-copy parsing must support actual 26.3 palette formats without writing saves."""
from pathlib import Path
import sys
import tempfile
import unittest

sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from western_city_survey import block,chunks


class WesternSurveyTests(unittest.TestCase):
    def test_string_and_wrapped_string_palette(self):
        for palette in [(8,['minecraft:stone']), (10,[{'':(8,'minecraft:stone')}]),
                        (10,[{'Name':(8,'minecraft:stone')}]), (10,[{'id':(8,'minecraft:stone')}])]:
            section={'block_states':(10,{'palette':(9,palette)})}
            self.assertEqual(block(section,2,15,8),'minecraft:stone')

    def test_packed_palette_uses_y_z_x_index(self):
        data=[0]*256
        index=11*256+3*16+7
        data[index//16]=1<<((index%16)*4)
        section={'block_states':(10,{'palette':(9,(8,['minecraft:air','minecraft:water'])), 'data':(12,data)})}
        self.assertEqual(block(section,7,11,3),'minecraft:water')
        self.assertEqual(block(section,3,11,7),'minecraft:air')

    def test_corrupt_region_is_rejected_without_modification(self):
        with tempfile.TemporaryDirectory() as folder:
            path=Path(folder)/'r.0.0.mca'
            data=bytearray(8192); data[0:4]=(257).to_bytes(4,'big') # Sector 1 is the header.
            path.write_bytes(data)
            with self.assertRaisesRegex(ValueError,'extent'):
                list(chunks(Path(folder),(0,0,15,15)))
            self.assertEqual(path.read_bytes(),data)


if __name__=='__main__':unittest.main()

"""Audit the complete owner collection without loading or executing supplied classes/programs."""
import csv
import hashlib
import json
from collections import Counter, defaultdict
from pathlib import Path
import jimtafacts

ROOT = jimtafacts.ROOT
SOURCE = jimtafacts.SOURCE
OUT = ROOT / 'art/owner-library/catalog/edited-2026-10-23-jimtafacts'

def main():
    manifest = json.loads((OUT / 'manifest.json').read_text(encoding='utf-8'))
    groups = defaultdict(lambda: {'files':0, 'bytes':0, 'extensions':Counter(), 'namespaces':set(), 'class_versions':Counter()})
    labels = {}; problems = []; omitted = []; verified = 0
    for row in manifest['files']:
        file = SOURCE / row['path']
        if not file.exists() and file.suffix == '.jar':
            omitted.append({'path':row['path'], 'reason':'Third-party dependency JAR intentionally absent from checkout; original intake hash retained.'})
            continue
        raw = file.read_bytes()
        if len(raw) != row['bytes'] or hashlib.sha256(raw).hexdigest() != row['sha256']:
            raise ValueError('Source integrity mismatch: ' + row['path'])
        verified += 1
        relative = file.relative_to(SOURCE); parts = relative.parts
        group = parts[0] if parts[0] in ('applied energentics reborn by jimbo','buildcraft reborn by jimbo','industrialcraft reborn by jimbo','computers') else 'root accessories and spellbooks'
        data = groups[group]; data['files'] += 1; data['bytes'] += len(raw); data['extensions'][file.suffix] += 1
        if file.suffix == '.jar': omitted.append({'path':row['path'], 'reason':'Third-party dependency JAR: local reference only; excluded from Git and runtime.'})
        if file.suffix == '.class' and raw[:4] == b'\xca\xfe\xba\xbe': data['class_versions'][int.from_bytes(raw[6:8],'big')] += 1
        for marker in ('assets','data'):
            if marker in parts:
                index = parts.index(marker)
                if len(parts) > index+2: data['namespaces'].add(parts[index+1])
        if file.name == 'en_us.json' and file.parent.name == 'lang':
            lang = json.loads(raw)
            for key,value in lang.items():
                if key.startswith(('item.','block.','entity.')) and isinstance(value,str):
                    kind, namespace, name = (key.split('.',2)+['',''])[:3]
                    # Tooltips and component labels are not registry IDs.
                    if '.' in name: continue
                    labels[(kind,namespace,name)] = {'kind':kind,'source_id':namespace+':'+name,'label':value,'group':group,'source':row['path']}
        if file.suffix in ('.json','.mcmeta'):
            try: json.loads(raw)
            except (UnicodeError, ValueError) as error: problems.append({'path':row['path'],'problem':str(error)[:240]})
    native = set(jimtafacts.ACCESSORIES) | set(jimtafacts.FOOD)
    rows = []
    for (kind,ns,name),row in sorted(labels.items()):
        row.update({'runtime_id':'jugcraft:jimta_'+name if ns=='artifacts' and name in native and kind=='item' else '',
                    'status':'native_functional_slice' if ns=='artifacts' and name in native and kind=='item' else 'preserved_pending_port'})
        rows.append(row)
    with (OUT/'content.csv').open('w',newline='',encoding='utf-8') as stream:
        writer=csv.DictWriter(stream,fieldnames=['kind','source_id','label','group','source','runtime_id','status']);writer.writeheader();writer.writerows(rows)
    overlaps = []
    for row in rows:
        name = row['source_id'].split(':', 1)[1]
        if row['kind'] in ('item', 'block') and row['status'] != 'native_functional_slice' and (ROOT / f'src/main/resources/assets/jugcraft/items/{name}.json').is_file():
            overlaps.append({'source_id': row['source_id'], 'existing_item': 'jugcraft:' + name,
                'decision': 'Review shared function/recipe mapping before registering a duplicate; name alone is not compatibility proof.'})
    jimtafacts.dump(OUT/'existing-name-overlaps.json', overlaps)
    serial={k:{**v,'extensions':dict(v['extensions']),'namespaces':sorted(v['namespaces']),'class_versions':dict(v['class_versions'])} for k,v in sorted(groups.items())}
    jimtafacts.dump(OUT/'audit.json',{'verified_files':verified,'bytes':sum(row['bytes'] for row in manifest['files']),
        'labeled_entries':len(rows),'native_slice_items':len(native),'groups':serial,'parse_issues':problems,'git_omissions':omitted,
        'warning':'Labels are an index, not a complete registry census. Legacy IC2/BuildCraft .lang keys, computed variants and spell registries require separate ports.'})
    print(json.dumps({'verified':verified,'labeled_entries':len(rows),'parse_issues':len(problems),'excluded_dependency_jars':len(omitted)}))

if __name__=='__main__': main()

"""Build the operator-selected western waterfront layout from local, authored geometry."""
import gzip
import json
from western_city import ROOT, payload
from western_city_terraces import build_terraced


def main():
    voxels, rooms, plots = build_terraced(expanded=True)
    plan = payload(voxels, rooms, plots, shell_first=True)
    plan['siteReferenceOrigin'] = [1296, 114, 400]
    plan['installation'] = 'survey_required_waterfront'
    path = ROOT / 'src/main/resources/data/jugcraft/town/western_waterfront.json.gz'
    raw = json.dumps(plan, separators=(',', ':')).encode('utf-8')
    packed = bytearray(gzip.compress(raw, compresslevel=6, mtime=0))
    packed[9] = 255
    path.write_bytes(packed)
    print(f'{len(voxels.b):,} cells, {len(rooms)} rooms, {len(plots)} landmarks, bounds {plan["bounds"]}')


if __name__ == '__main__':
    main()

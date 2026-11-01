"""Apply explicit authored resource repairs after canonical generation.

Preconditions make upstream generator changes visible instead of silently replacing
new content. These are data patches; no supplied library code is executed.
"""
import argparse
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'


def value_key(value):
    """Hashable JSON structural equality, including order-insensitive object keys."""
    if isinstance(value, dict):
        return ('object', frozenset((key, value_key(child)) for key, child in value.items()))
    if isinstance(value, list):
        return ('array', tuple(value_key(child) for child in value))
    return ('scalar', value)


def apply(phase='canonical'):
    if phase not in ('canonical', 'final'):
        raise ValueError(f'Unknown resource patch phase: {phase}')
    patches = json.loads((ROOT / 'tools/data/owner-resource-patches.json').read_text(encoding='utf-8'))
    for resource in patches['resources']:
        # Final additions belong after every shared tag/atlas writer. Keep the
        # original exact repairs at their existing canonical insertion point.
        if resource.get('phase', 'canonical') != phase:
            continue
        target = (RES / resource['file']).resolve()
        if not target.is_relative_to(RES.resolve()):
            raise ValueError('Resource patch escapes the resource directory')
        data = json.loads(target.read_text(encoding='utf-8'))
        for change in resource['changes']:
            parent = data
            for key in change['path'][:-1]:
                parent = parent[key]
            key = change['path'][-1]
            if change['op'] == 'add':
                if key in parent and parent[key] != change['after']:
                    raise ValueError(f'Authored resource addition conflicts: {target}: {change["path"]}')
                parent[key] = change['after']
            elif change['op'] == 'append_values':
                current, before, after = parent.get(key), change['before'], change['after']
                if not all(isinstance(values, list) for values in (current, before, after)):
                    raise ValueError(f'Invalid additive resource patch: {target}: {change["path"]}')
                current_keys, before_keys, after_keys = (set(map(value_key, values)) for values in (current, before, after))
                if not before_keys <= after_keys:
                    raise ValueError(f'Invalid additive resource patch: {target}: {change["path"]}')
                if not before_keys <= current_keys:
                    raise ValueError(f'Generated resource changed: {target}: {change["path"]}')
                # Keep later generators' members while applying only this patch's additions.
                for value in after:
                    key_value = value_key(value)
                    if key_value not in before_keys and key_value not in current_keys:
                        current.append(value)
                        current_keys.add(key_value)
            elif change['op'] == 'remove':
                if key not in parent:
                    continue
                if parent[key] != change['before']:
                    raise ValueError(f'Generated resource changed: {target}: {change["path"]}')
                del parent[key]
            else:
                if parent.get(key) not in (change['before'], change['after']):
                    raise ValueError(f'Generated resource changed: {target}: {change["path"]}')
                parent[key] = change['after']
        target.write_text(json.dumps(data, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--phase', choices=('canonical', 'final'), default='canonical')
    apply(parser.parse_args().phase)

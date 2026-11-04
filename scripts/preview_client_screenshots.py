"""Bound console previews; the workflow uploads every original screenshot separately."""
import base64
import glob
import io
from pathlib import Path


def preview_paths(paths, limit=24):
    if limit < 1:
        raise ValueError('Preview limit must be positive')
    ordered = sorted(paths)
    if len(ordered) <= limit:
        return ordered
    if limit == 1:
        return ordered[:1]
    return [ordered[round(i * (len(ordered) - 1) / (limit - 1))] for i in range(limit)]


def main():
    from PIL import Image
    paths = glob.glob('**/screenshots/*.png', recursive=True)
    selected = preview_paths(paths)
    print(f'Previewing {len(selected)} of {len(paths)} screenshots; all PNGs are in the artifact.')
    for path in selected:
        with Image.open(path) as original:
            image = original.convert('RGB')
        image.thumbnail((480, 270))
        buffer = io.BytesIO()
        image.save(buffer, 'JPEG', quality=60)
        print(f'=== SCREENSHOT {Path(path).name} {image.size}')
        print(base64.b64encode(buffer.getvalue()).decode())
        print('=== END')


if __name__ == '__main__':
    main()

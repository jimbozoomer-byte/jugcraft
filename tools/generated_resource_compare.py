"""Compare generated resources without depending on a PNG encoder version."""
from io import BytesIO
import json

from PIL import Image


def matches(path, expected):
    if not path.is_file():
        return False
    if path.suffix == '.json':
        # Windows writes CRLF while the in-memory generator uses LF. Compare
        # every resource value, not platform whitespace or key ordering.
        return json.loads(path.read_text(encoding='utf-8')) == json.loads(expected)
    if path.suffix != '.png':
        return path.read_bytes() == expected
    # Pillow/zlib versions can produce different compressed bytes for identical
    # artwork. Still compare every RGBA pixel, including transparent pixels.
    with Image.open(path) as saved, Image.open(BytesIO(expected)) as generated:
        return saved.size == generated.size and saved.convert('RGBA').tobytes() == generated.convert('RGBA').tobytes()

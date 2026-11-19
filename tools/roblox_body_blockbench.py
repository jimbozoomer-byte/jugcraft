"""Write editable Blockbench bodies and the shared Jugcraft fitting texture."""
import argparse
import json
from pathlib import Path

from PIL import Image
from guest_body_fit import write_blockbench
from guest_portrait_models import SIZE


def export(out):
    manifest=json.loads((out/"fits.json").read_text(encoding="utf8"))
    atlas=Image.new("RGBA",(SIZE,SIZE),tuple(manifest["color"]))
    atlas.save(out/"fitting-clay.png")
    for fit in manifest["presets"]:
        key=fit["id"]
        folder=out/key
        mesh=json.loads((folder/(key+".json")).read_text())
        write_blockbench(mesh,atlas,folder/(key+".bbmodel"),name=fit["label"],
                         texture_name="fitting-clay.png",keep_sockets=True)


if __name__=="__main__":
    cli=argparse.ArgumentParser(description=__doc__)
    cli.add_argument("folder",type=Path)
    export(cli.parse_args().folder)

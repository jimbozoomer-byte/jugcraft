"""Generated data for the biomes branch's wild plants (tools/plants.py): models, blockstates, items, names, loot, tags,
the registration list Java reads (/jugcraft/plants.json) and a placement feature per plant (worldgen/feature/<plant>)
that biomes scatter (tools/biomes.py EXTRAS).
"""
import plants as pl

MOD = "jugcraft"
FACINGS = {"north": 0, "east": 90, "south": 180, "west": 270}


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


def tex(name):
    return rid(f"block/{name}")


def assets(root, write, lang):
    models = root / "models" / "block"
    for plant, info in pl.PLANTS.items():
        kind = info["kind"]
        lang[f"block.{MOD}.{plant}"] = info["display"]
        if kind == "flower":
            # Vanilla's flower and potted-flower shapes, with our textures.
            write(models / f"{plant}.json", {"parent": "minecraft:block/cross", "textures": {"cross": tex(plant)}})
            write(models / f"{pl.potted(plant)}.json", {"parent": "minecraft:block/flower_pot_cross", "textures": {"plant": tex(plant)}})
            write(root / "blockstates" / f"{plant}.json", {"variants": {"": {"model": rid(f"block/{plant}")}}})
            write(root / "blockstates" / f"{pl.potted(plant)}.json", {"variants": {"": {"model": rid(f"block/{pl.potted(plant)}")}}})
            icon = tex(plant)
            lang[f"block.{MOD}.{pl.potted(plant)}"] = f"Potted {info['display']}"
        elif kind in pl.TALL:
            for half in ("bottom", "top"):
                write(models / f"{plant}_{half}.json", {"parent": "minecraft:block/cross", "textures": {"cross": tex(f"{plant}_{half}")}})
            write(root / "blockstates" / f"{plant}.json", {"variants": {
                "half=lower": {"model": rid(f"block/{plant}_bottom")}, "half=upper": {"model": rid(f"block/{plant}_top")}}})
            icon = tex(f"{plant}_top")
        elif kind == "water_plant":
            write(models / f"{plant}.json", {"parent": "minecraft:block/cross", "textures": {"cross": tex(plant)}})
            write(root / "blockstates" / f"{plant}.json", {"variants": {"": {"model": rid(f"block/{plant}")}}})
            icon = tex(plant)
        elif kind == "surface":
            # Vanilla's lily pad shape (its tint is unused: our texture carries its colours).
            write(models / f"{plant}.json", {"parent": "minecraft:block/lily_pad", "textures": {"particle": tex(plant), "texture": tex(plant)}})
            write(root / "blockstates" / f"{plant}.json", {"variants": {"": {"model": rid(f"block/{plant}")}}})
            icon = tex(plant)
        else:
            # Vanilla's flowerbed shapes (one to four clumps), turned by facing, like pink petals.
            for n in range(1, 5):
                write(models / f"{plant}_{n}.json", {"parent": f"minecraft:block/flowerbed_{n}",
                                                      "textures": {"flowerbed": tex(plant), "stem": tex(f"{plant}_stem")}})
            parts = []
            for n in range(1, 5):
                amounts = "|".join(str(a) for a in range(n, 5))
                for facing, y in FACINGS.items():
                    when = {"facing": facing} if n == 1 else {"facing": facing, "flower_amount": amounts}
                    apply = {"model": rid(f"block/{plant}_{n}")}
                    if y:
                        apply["y"] = y
                    parts.append({"apply": apply, "when": when})
            write(root / "blockstates" / f"{plant}.json", {"multipart": parts})
            icon = tex(plant)
        write(root / "models" / "item" / f"{plant}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": icon}})
        write(root / "items" / f"{plant}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{plant}")}})


def match_block(block, **state):
    return {"type": "minecraft:match_block", "blocks": rid(block), "state": {k: str(v) for k, v in state.items()}}


def loot(out, write):
    explosion = {"type": "minecraft:survives_explosion"}
    for plant, info in pl.PLANTS.items():
        kind = info["kind"]
        if kind == "flower":
            write(out / f"{plant}.json", {"type": "minecraft:block", "pools": [
                {"condition": explosion, "entries": [{"type": "minecraft:item", "name": rid(plant)}], "rolls": 1}],
                "random_sequence": rid(f"blocks/{plant}")})
            # Vanilla's potted flowers: the pot and the flower.
            write(out / f"{pl.potted(plant)}.json", {"type": "minecraft:block", "pools": [
                {"condition": explosion, "entries": [{"type": "minecraft:item", "name": "minecraft:flower_pot"}], "rolls": 1},
                {"condition": explosion, "entries": [{"type": "minecraft:item", "name": rid(plant)}], "rolls": 1}],
                "random_sequence": rid(f"blocks/{pl.potted(plant)}")})
        elif kind in pl.TALL:
            # From the lower half only, like the lilac.
            write(out / f"{plant}.json", {"type": "minecraft:block", "pools": [
                {"condition": explosion, "entries": [{"type": "minecraft:item", "condition": match_block(plant, half="lower"),
                                                      "name": rid(plant)}], "rolls": 1}], "random_sequence": rid(f"blocks/{plant}")})
        elif kind == "water_plant":
            # Only shears take it, like seagrass.
            write(out / f"{plant}.json", {"type": "minecraft:block", "pools": [
                {"condition": "minecraft:tool/can_shear", "entries": [{"type": "minecraft:item", "name": rid(plant)}], "rolls": 1}],
                "random_sequence": rid(f"blocks/{plant}")})
        elif kind == "surface":
            write(out / f"{plant}.json", {"type": "minecraft:block", "pools": [
                {"condition": explosion, "entries": [{"type": "minecraft:item", "name": rid(plant)}], "rolls": 1}],
                "random_sequence": rid(f"blocks/{plant}")})
        else:
            # One per clump, like pink petals.
            write(out / f"{plant}.json", {"type": "minecraft:block", "pools": [{"entries": [{"type": "minecraft:item", "modifier": [
                *[{"type": "minecraft:set_count", "condition": match_block(plant, flower_amount=n), "count": n} for n in range(1, 5)],
                {"type": "minecraft:explosion_decay"}], "name": rid(plant)}], "rolls": 1}], "random_sequence": rid(f"blocks/{plant}")})


def tags(tags):
    for plant, info in pl.PLANTS.items():
        kind = info["kind"]
        if kind in ("tall_plant", "dune_plant", "water_plant", "surface"):
            if kind in ("tall_plant", "dune_plant"):
                tags.add("block", "minecraft:replaceable_by_trees", rid(plant))
                tags.add("block", "minecraft:sword_efficient", rid(plant))
            continue
        if kind == "flower":
            for registry in ("block", "item"):
                tags.add(registry, "minecraft:small_flowers", rid(plant))
            tags.add("block", "minecraft:flower_pots", rid(pl.potted(plant)))
        elif kind == "tall_flower":
            for registry in ("block", "item"):
                tags.add(registry, "minecraft:flowers", rid(plant))
        else:
            for registry in ("block", "item"):
                tags.add(registry, "minecraft:flowers", rid(plant))
            tags.add("block", "minecraft:inside_step_sound_blocks", rid(plant))
        tags.add("block", "minecraft:bee_attractive", rid(plant))
        tags.add("block", "minecraft:replaceable_by_trees", rid(plant))
        tags.add("block", "minecraft:enchantment_power_transmitter", rid(plant))


def placement_state(plant):
    """What a plant's feature places: one block, the lower half of a tall flower, or clumps of any size and facing."""
    kind = pl.PLANTS[plant]["kind"]
    if kind == "flower":
        return {"id": rid(plant)}
    if kind in pl.TALL:
        return {"id": rid(plant), "properties": {"half": "lower"}}
    if kind in ("water_plant", "surface"):
        return {"id": rid(plant)}
    return {"type": "minecraft:weighted", "entries": [
        {"data": {"id": rid(plant), "properties": {"facing": facing, "flower_amount": str(n)}}, "weight": 1}
        for n in range(1, 5) for facing in FACINGS]}


def worldgen(data, write):
    write(data.parent / MOD / "plants.json", pl.registration())
    for plant in pl.PLANTS:
        write(data / MOD / "worldgen" / "feature" / f"{plant}.json", {"type": "minecraft:simple_block", "to_place": placement_state(plant)})

"""Winter quay counters. North is the customer side; closed backs face the shopkeeper."""
import json
from pathlib import Path

RES = Path(__file__).resolve().parents[1] / "src/main/resources"
ART = RES / "assets/jugcraft"
DATA = RES / "data/jugcraft"
NAMES = ("winter_wrapping_counter", "winter_bakery_counter", "winter_toy_counter")
TEX = {k:"minecraft:block/"+v for k,v in {
    "timber":"stripped_spruce_log","board":"spruce_planks","frame":"dark_oak_planks","brass":"cut_copper",
    "gold":"gold_block","red":"red_wool","green":"green_wool","blue":"blue_wool","cream":"white_wool",
    "linen":"white_wool","metal":"iron_block","spice":"terracotta","bun":"stripped_oak_log",
    "icing":"calcite","honey":"yellow_terracotta","black":"black_terracotta","particle":"spruce_planks"}.items()}


LANGUAGE = {
    "block.jugcraft.winter_wrapping_counter": "Winter Wrapping Counter",
    "block.jugcraft.winter_bakery_counter": "Quayside Bakehouse",
    "block.jugcraft.winter_toy_counter": "Winter Toy Cabinet",
    "item.jugcraft.addressed_winter_gift": "Addressed Winter Gift",
    "item.jugcraft.market.address": "Gift for %s",
    "item.jugcraft.market.sender": "Wrapped by %s",
    "message.jugcraft.market.waiting": "You have %s winter gifts waiting at a wrapping counter.",
    "message.jugcraft.market.full": "Their parcel shelf is full. Keep this gift until they collect some mail.",
    "message.jugcraft.market.carried": "Wrapped for %s. Hand it to them or leave it under a gift fir. They can also collect it at a wrapping counter.",
    "message.jugcraft.market.sent": "Your gift is waiting for %s at the wrapping counter.",
    "message.jugcraft.market.arrival": "%s wrapped a gift for you! Visit a wrapping counter to collect it.",
    "message.jugcraft.market.make_room": "Make room in your inventory first.",
    "message.jugcraft.market.sealed": "Only the addressee can open an unclaimed parcel.",
    "message.jugcraft.market.opened": "You unwrapped a gift from %s: %s.",
    "screen.jugcraft.market.wares": "Click a purchase below. Prices are in Jugs.",
    "screen.jugcraft.market.to": "Choose the recipient",
    "screen.jugcraft.market.from": "Your gifts from",
    "screen.jugcraft.market.hold_hint": "Hold the stack you want to wrap, then open the counter.",
    "screen.jugcraft.market.unseal_hint": "Select a parcel to open it.",
    "screen.jugcraft.market.inbox": "My parcels",
    "screen.jugcraft.market.address_book": "Address book",
    "screen.jugcraft.market.post": "Wrap & post",
    "screen.jugcraft.market.carry": "Wrap & carry",
    "screen.jugcraft.market.unwrap": "Unwrap gift",
    "screen.jugcraft.market.no_mail": "Your shelf is empty.",
    "screen.jugcraft.market.no_names": "Other players appear here after they join the server."
}


def doc(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2)+"\n", encoding="utf8")


def furniture(kind):
    parts = []
    def box(tex, x,y,z,X,Y,Z):
        parts.append({"from":[x,y,z],"to":[X,Y,Z],"faces":{
            face:{"texture":"#"+tex} for face in ("up","down","north","south","east","west")}})
    cloth = ("green","red","blue")[kind]
    box("board",1,1,2,15,10,15)
    for x in (0,13):
        for z in (1,13):
            box("timber",x,0,z,x+3,11,z+2)
            box("brass",x,2,z-.12,x+3,3,z+2.12)
    box("frame",0,9,0,16,11,16)
    box(cloth,3,.8,.65,13,10,1.9)
    for x in (3.5,11.5): box("linen",x,1.1,.52,x+.65,9.6,.8)
    box("linen",3,10.9,0,13,11.3,15.8)
    # A shop emblem stitched into the apron. Parcel, gingerbread figure and rocking horse.
    if kind == 0:
        box("red",5,3,.25,11,7,.7); box("gold",7.6,3,.1,8.5,7.7,.3); box("gold",5,5,.1,11,5.7,.3)
        for x,material in ((2,"red"),(5,"cream"),(8,"green")):
            box(material,x,12,11,x+2.5,14.5,14.5)
        box("brass",1,13,12.5,12,13.6,13.3)
        for x in (1,11.5): box("frame",x,11.1,10.8,x+.6,15,14.6)
        box("cream",2,11.3,2,8,11.5,8); box("red",4,11.5,3,5,11.7,8)
        box("brass",12,11.2,3,15,11.8,6); box("gold",12.3,11.8,3.4,14.7,13,5.6)
        box("brass",13.2,13,4.2,13.8,13.7,4.8)
    elif kind == 1:
        box("honey",6.7,6.2,.2,9.3,8.7,.7); box("honey",6.6,3,.2,9.4,6.3,.7)
        for x in (5,9): box("honey",x,5,.2,x+2,6.2,.7)
        for x in (6,8.5): box("honey",x,2,.2,x+1.3,3.4,.7)
        for x in (7.1,8.45): box("icing",x,7.5,.1,x+.4,7.9,.3)
        box("metal",1,11.3,2,10,11.7,10)
        for x,z in ((2,3),(6,4),(3.5,7)):
            box("spice",x,11.7,z,x+3,12.7,z+2.7); box("bun",x+.2,12.7,z+.3,x+2.8,13.2,z+2.4)
            box("icing",x+.65,13.2,z+.9,x+2.1,13.4,z+1.35)
        box("black",10,11.2,9,15.5,12,15)
        box("brass",10.5,12,9.6,15,15,14.6); box("metal",10,15,9,15.5,15.5,15)
        box("brass",12,13,8.2,13,13.8,10); box("brass",12,12.6,8.2,13,13.3,9)
    else:
        box("honey",5,4,.2,10,6,.7); box("honey",9,5,.2,10.3,7.7,.7)
        for x in (5.5,9): box("honey",x,2.9,.2,x+.6,4.3,.7)
        box("cream",4,2,.15,11,2.8,.7)
        # A miniature train with brass hubs and a two-tone sailboat on turned display plinths.
        for x in (1,4.8):
            box("frame",x,11.3,2,x+3.4,12,5); box("red",x+.25,12,2.2,x+3.1,13.5,4.8)
            for dx in (.3,2): box("gold",x+dx,11.4,1.8,x+dx+.9,12.4,2.2)
        box("black",1.6,13.5,3,2.4,15,4); box("blue",5.1,13.5,2,7.7,14,5)
        box("timber",8,11.3,9,15,12.1,13); box("brass",11.3,12,11,11.8,16,11.5)
        box("cream",10,14,10.8,13.8,15.6,11); box("red",9,12.6,10.8,14.5,13.8,11)
    return {"parent":"minecraft:block/block","textures":TEX,"elements":parts}


def generate():
    for i,name in enumerate(NAMES):
        model = "jugcraft:block/"+name
        doc(ART/"models/block"/(name+".json"),furniture(i))
        doc(ART/"blockstates"/(name+".json"),{"variants":{"facing="+d:{"model":model,**({"y":90*j} if j else {})}
            for j,d in enumerate(("north","east","south","west"))}})
        doc(ART/"items"/(name+".json"),{"model":{"type":"minecraft:model","model":model}})
        materials = ("minecraft:paper","minecraft:cookie","minecraft:oak_button")
        doc(DATA/"recipe"/(name+".json"),{"type":"minecraft:crafting_shaped","fabric:load_conditions":[{"condition":"jugcraft:feature_enabled","feature":"town"}],"category":"misc","pattern":["XWX","PPP","S S"],
            "key":{"X":materials[i],"W":("minecraft:green_wool","minecraft:red_wool","minecraft:blue_wool")[i],
                   "P":"minecraft:spruce_planks","S":"minecraft:stick"},"result":{"id":"jugcraft:"+name}})
        doc(DATA/"loot_table/blocks"/(name+".json"),{"type":"minecraft:block","pools":[{"rolls":1,
            "condition":{"type":"minecraft:survives_explosion"},"entries":[{"type":"minecraft:item","name":"jugcraft:"+name}]}]})
    doc(ART/"items/addressed_winter_gift.json",{"model":{"type":"minecraft:model","model":"jugcraft:newest/holidayworks/block/christmas/red_present"}})


if __name__ == "__main__": generate()

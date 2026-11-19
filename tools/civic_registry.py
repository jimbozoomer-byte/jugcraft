"""Town, guild, bathhouse and seasonal registry entries used in resource validation."""
import deepstead_shop_goods as guild
import deepstead_wares
import hot_spring_furniture as spring
import winter_market_counters as market
import winter_trimmings as winter

WINTER_BLOCKS = {k.removeprefix("block.jugcraft.") for k in winter.LANGUAGE if k.startswith("block.jugcraft.")}
SPRING_BLOCKS = {spring.P + k for k in spring.PIECES}
SPRING_KITS = {spring.P + k for k in spring.KITS}
TOWN_BLOCKS = {"counterweight_lift", *deepstead_wares.BLOCKS, *guild.FURNISHINGS, *market.NAMES}
BLOCKS = TOWN_BLOCKS | WINTER_BLOCKS | SPRING_BLOCKS
ITEMS = (BLOCKS - {"civic_gift_skirt"}) | SPRING_KITS | {
    "addressed_winter_gift", *deepstead_wares.TOOLS, *guild.TOOLS, *guild.FOODS,
}

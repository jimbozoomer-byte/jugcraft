package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** Real trees, timber comparisons, winter berries, clipped sprigs and wall/door wreaths in a saved normal world. */
public class HollyClientGameTests extends TreeBatchTwoClientGameTests {
	private final Map<BlockPos,BlockState> saved = new LinkedHashMap<>();
	@Override protected List<String> shapes() { return List.of("holly"); }
	@Override protected List<String> biomes() { return List.of("woodland", "dense_forest"); }
	@Override protected String worldName() { return "Holly Inspection"; }
	@Override protected String exportName() { return "tree-batch-6b-preview"; }
	@Override protected String screenshotPrefix() { return "jugcraft_holly_"; }
	@Override protected BlockPos surveySpot(ServerLevel level,BlockPos first,ResourceKey<Biome> biome) {
		for(int[] offset:new int[][]{{0,0},{256,0},{-256,0},{0,256},{0,-256},{256,256},{-256,-256}}) {
			var at=offset[0]==0&&offset[1]==0?first:BiomeClientGameTests.surfaceSpot(level,first.offset(offset[0],0,offset[1]),biome);
			if(at==null) continue;
			var counts=survey(level,at,biome); Jugcraft.LOGGER.info("[holly natural sample] {} {} {}",biome.identifier(),at,counts);
			if(counts.getOrDefault("leaf:jugcraft:holly_leaves",0)>0) return at;
		}
		throw new AssertionError("No natural holly in surveyed "+biome.identifier());
	}
	@Override protected void validateSurvey(String biome,Map<String,Integer> counts) {
		if(counts.getOrDefault("leaf:jugcraft:holly_leaves",0)==0) throw new AssertionError("Missing holly in "+biome);
	}
	@Override protected void validateReopened(ServerLevel level) {
		saved.forEach((pos,state)-> {
			var now=level.getBlockState(pos);
			if(state.hasProperty(LeavesBlock.DISTANCE) && now.is(state.getBlock())) now=now.setValue(LeavesBlock.DISTANCE,state.getValue(LeavesBlock.DISTANCE));
			if(!now.equals(state)) throw new AssertionError("Holly/wreath state changed across save: "+pos+" expected "+state+" got "+now);
		});
	}
	@Override protected void extraDisplays(ClientGameTestContext context,TestSingleplayerContext world,List<String> tour) {
		var wood=GARDEN.offset(0,0,80); var wall=GARDEN.offset(0,0,130);
		world.getServer().runOnServer(server->{
			var level=server.overworld();
			for(var p:BlockPos.betweenClosed(wood.offset(-25,-1,-12),wall.offset(70,-1,25))) level.setBlock(p,Blocks.STONE.defaultBlockState(),Block.UPDATE_CLIENTS);
			PineWoodClientGameTests.comparisonRow(level,wood,0,new String[]{"jugcraft:holly","minecraft:birch","minecraft:pale_oak","jugcraft:pine","jugcraft:eucalyptus","jugcraft:palm"});
			for(int season=0;season<3;season++) for(boolean clipped:List.of(false,true)) {
				var foliage=SeasonalLeavesBlock.Foliage.values()[season]; var at=wall.offset(season*9,clipped?1:4,0);
				for(int x=0;x<4;x++) for(int y=0;y<2;y++) {
					var pos=at.offset(x,y,0); var state=JugcraftAgriculture.block("holly_leaves").defaultBlockState().setValue(LeavesBlock.PERSISTENT,true).setValue(SeasonalLeavesBlock.SEASON,foliage).setValue(HollyLeavesBlock.CLIPPED,clipped);
					level.setBlock(pos,state,Block.UPDATE_CLIENTS); level.setBlock(pos,state,Block.UPDATE_CLIENTS); saved.put(pos,state);
				}
				PineWoodClientGameTests.label(level,at.offset(1,-1,2),foliage.name(),clipped?"Clipped / regrowing":"Evergreen / berries","");
			}
			// One supported natural clipping demonstration, separate from placed decorative panels.
			var clip=wall.offset(32,1,0); level.setBlock(clip.below(),JugcraftAgriculture.block("holly_log").defaultBlockState(),Block.UPDATE_CLIENTS);
			var natural=JugcraftAgriculture.block("holly_leaves").defaultBlockState().setValue(LeavesBlock.DISTANCE,1);
			level.setBlock(clip,natural,Block.UPDATE_CLIENTS); saved.put(clip,level.getBlockState(clip));
			PineWoodClientGameTests.label(level,clip.offset(0,-1,2),"Clip with shears","One sprig / regrows","Random ticks paused");
			for(int x=0;x<9;x++) for(int y=0;y<5;y++) level.setBlock(wall.offset(40+x,y,0),JugcraftAgriculture.block("holly_planks").defaultBlockState(),Block.UPDATE_CLIENTS);
			var wreath=JugcraftAgriculture.block("holly_wreath").defaultBlockState().setValue(AutumnWreathBlock.FACING,Direction.SOUTH);
			var hung=wall.offset(42,3,1); level.setBlock(hung,wreath,Block.UPDATE_ALL); saved.put(hung,level.getBlockState(hung));
			var door=wall.offset(46,0,1); level.setBlock(door,Blocks.OAK_DOOR.defaultBlockState(),Block.UPDATE_ALL); level.setBlock(door.above(),Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),Block.UPDATE_ALL);
			var onDoor=door.above().south(); level.setBlock(onDoor,wreath,Block.UPDATE_ALL); saved.put(onDoor,level.getBlockState(onDoor));
			PineWoodClientGameTests.label(level,wall.offset(43,0,3),"Holly Wreath","8 sprigs + string","Walls and doors");
			var player=server.getPlayerList().getPlayers().getFirst();
			for(var stack:List.of(new ItemStack(Items.SHEARS),new ItemStack(JugcraftAgriculture.item("holly_sprig"),32),new ItemStack(JugcraftAgriculture.block("holly_wreath"),8),new ItemStack(JugcraftAgriculture.block("holly_sapling"),16))) player.getInventory().add(stack);
		});
		shoot(context,world,GARDEN.offset(8,5,11),GARDEN.above(3),screenshotPrefix()+"tree_close");
		shoot(context,world,wood.offset(2,7,32),wood.offset(2,2,0),screenshotPrefix()+"wood_comparison");
		shoot(context,world,wood.offset(-18,4,11),wood.offset(-18,2,0),screenshotPrefix()+"wood_close");
		for(boolean cutout:List.of(true,false)) {
			context.runOnClient(client->client.options.cutoutLeaves().set(cutout));
			shoot(context,world,wall.offset(10,5,19),wall.offset(10,3,0),screenshotPrefix()+(cutout?"foliage_cutout":"foliage_solid"));
		}
		context.runOnClient(client->client.options.cutoutLeaves().set(true));
		shoot(context,world,wall.offset(44,4,11),wall.offset(44,2,0),screenshotPrefix()+"wreaths");
		// A winter garden view keeps the same geometry, now with scarlet berry foliage.
		world.getServer().runOnServer(server->{
			var level=server.overworld();
			for(var p:BlockPos.betweenClosed(GARDEN.offset(-10,0,-90),GARDEN.offset(10,12,10))) {
				var state=level.getBlockState(p); if(state.is(JugcraftAgriculture.block("holly_leaves"))) {
					var winter=state.setValue(SeasonalLeavesBlock.SEASON,SeasonalLeavesBlock.Foliage.BARE); level.setBlock(p,winter,Block.UPDATE_CLIENTS); saved.put(p.immutable(),winter);
				}
			}
		});
		shoot(context,world,GARDEN.offset(8,5,11),GARDEN.above(3),screenshotPrefix()+"winter_tree");
		tour.add("Timber and native comparisons: /tp @s 1002 181 -889 180 0");
		tour.add("Foliage states and clipping: /tp @s 1010 181 -851 180 0");
		tour.add("Holly Wreaths on wall and door: /tp @s 1044 181 -859 180 0");
		tour.add("Shears, sprigs, wreaths and saplings are in your inventory. Right-click natural leaves with shears; placed leaves are decorative.");
		tour.add("Random ticks are frozen. /gamerule minecraft:random_tick_speed 3 restores sprig regrowth, sapling growth and leaf decay.");
		tour.add("Garden trees display winter berries; all foliage stays green. Natural biome stops were not planted. Batch 7 is unstarted.");
	}
}

package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.pharmacology.*;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Actual onset/folio/visibility boundaries and bounded owner-only cue data on the production query. */
public class PharmacologyDreamGameTests {
	private static final String ARENA="jugcraft-test:arms_arena";
	private static ServerPlayer actor(GameTestHelper h) {var p=TestPlayers.survivor(h);p.setNoGravity(true);p.setOnGround(true);p.getInventory().clearContent();p.setPos(h.absoluteVec(new Vec3(5.5,2,7.5)));return p;}
	private static void seconds(ServerPlayer p,int n) {for(int i=0;i<n;i++)Pharmacology.tickSecond(p);}
	@GameTest(structure=ARENA) public void sightCuesRequireOnsetFolioRecordedSiteAndLineOfSight(GameTestHelper h) {
		var p=actor(h);var pos=h.absolutePos(new BlockPos(5,2,5));h.getLevel().setBlockAndUpdate(pos,DreamStudies.PLINTH.defaultBlockState());
		h.assertTrue(DreamStudies.cue(p).equals(DreamCue.EMPTY),"Sober viewer receives no site data");
		new ItemStack(PsychedelicPharmacy.item("psilocybin")).finishUsingItem(p.level(),p);seconds(p,9);
		h.assertTrue(DreamStudies.cue(p).mode()==0,"Onset reveals no dream site data");seconds(p,1);
		var cue=DreamStudies.cue(p);h.assertTrue(cue.mode()==1 && !cue.folio() && cue.sites().isEmpty(),"Active dose explains a missing folio without revealing a site");
		p.getInventory().setItem(0,new ItemStack(DreamStudies.FOLIO));
		h.assertTrue(DreamStudies.cue(p).folio() && DreamStudies.cue(p).sites().isEmpty(),"Nearby unrecorded plinth is not discovered by the drug");
		h.assertTrue(DreamStudies.discover(p,pos) && DreamStudies.cue(p).sites().equals(List.of(pos)),"Physical discovery authorizes the visible site");
		var sober=actor(h);sober.getInventory().setItem(0,DreamStudies.folio(p).copy());
		h.assertTrue(DreamStudies.cue(sober).equals(DreamCue.EMPTY),"A nearby sober player carrying the same knowledge gets no drug cue");
		var wall=pos.above().south();h.getLevel().setBlockAndUpdate(wall,Blocks.STONE.defaultBlockState());
		h.assertTrue(DreamStudies.cue(p).sites().isEmpty(),"Opaque wall removes sight cues");h.getLevel().removeBlock(wall,false);
		p.setGameMode(GameType.SPECTATOR);h.assertTrue(DreamStudies.cue(p).mode()==0,"Spectator state removes authored cues");h.succeed();
	}
	@GameTest(structure=ARENA) public void cueSnapshotCapsVisibleSitesAndMescalineSelectsOnlyTheNearestRecordedSite(GameTestHelper h) {
		var p=actor(h);p.getInventory().setItem(0,new ItemStack(DreamStudies.FOLIO));
		for(int x=3;x<=7;x++) {var pos=h.absolutePos(new BlockPos(x,2,5));h.getLevel().setBlockAndUpdate(pos,DreamStudies.PLINTH.defaultBlockState());h.assertTrue(DreamStudies.discover(p,pos),"Each site is physically recorded");}
		Pharmacology.drink(p,PsychedelicPharmacy.PSILOCYBIN);seconds(p,10);
		var cue=DreamStudies.cue(p);h.assertTrue(cue.sites().size()==4 && cue.sites().getFirst().equals(h.absolutePos(new BlockPos(5,2,5))),"At most four visible recorded sites, nearest first");
		var body=p.position();p.setPos(body.add(16384,0,16384));int loaded=p.level().getChunkSource().getLoadedChunksCount();
		h.assertTrue(DreamStudies.cue(p).sites().isEmpty() && p.level().getChunkSource().getLoadedChunksCount()==loaded,"Unloaded recorded-site checks never force a chunk");p.setPos(body);
		Pharmacology.drink(p,PsychedelicPharmacy.MESCALINE);seconds(p,15);
		cue=DreamStudies.cue(p);h.assertTrue(cue.mode()==4 && cue.sites().equals(List.of(h.absolutePos(new BlockPos(5,2,5)))),"Resonance has one nearest recorded, loaded target");
		p.setPos(body.add(110,0,0));h.assertTrue(DreamStudies.cue(p).sites().isEmpty(),"96-block resonance range is enforced");
		p.getInventory().clearContent();h.assertTrue(!DreamStudies.cue(p).folio() && DreamStudies.cue(p).sites().isEmpty(),"Dropping the folio removes all target information");h.succeed();
	}
	@GameTest(structure=ARENA) public void higherStudyLayersKeepTheirOwnPostOnsetCueAndMilkClearsIt(GameTestHelper h) {
		for(var profile:List.of(PsychedelicPharmacy.LSD,PsychedelicPharmacy.DMT)) {
			var p=actor(h);p.getInventory().setItem(0,new ItemStack(DreamStudies.FOLIO));var pos=h.absolutePos(new BlockPos(5,2,5));h.getLevel().setBlockAndUpdate(pos,DreamStudies.PLINTH.defaultBlockState());DreamStudies.discover(p,pos);
			Pharmacology.drink(p,profile);h.assertTrue(DreamStudies.cue(p).mode()==0,"Higher-tier onset remains hidden");seconds(p,profile.onsetSeconds());
			h.assertTrue(DreamStudies.cue(p).mode()==(profile==PsychedelicPharmacy.LSD?2:3) && DreamStudies.cue(p).sites().equals(List.of(pos)),"Existing higher study tier keeps its authored cue");
			new ItemStack(net.minecraft.world.item.Items.MILK_BUCKET).finishUsingItem(p.level(),p);
			h.assertTrue(DreamStudies.cue(p).equals(DreamCue.EMPTY) && Pharmacology.state(p).strainTenths()>0,"Milk clears information authority without refunding strain");
		}
		h.succeed();
	}
	@GameTest public void dreamCueWireBoundsAndImmutablePositionsAreEnforced(GameTestHelper h) {
		var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),h.getLevel().registryAccess());
		try {
			var cue=new DreamCue(1,true,List.of(new BlockPos(1,2,3),new BlockPos(-4,5,6)));
			DreamCue.CODEC.encode(b,cue);h.assertTrue(DreamCue.CODEC.decode(b).equals(cue),"Private cue roundtrips through the native wire codec");
			b.clear();b.writeByte(1);b.writeBoolean(true);b.writeVarInt(5);boolean rejected=false;
			try {DreamCue.CODEC.decode(b);}catch(IllegalArgumentException expected){rejected=true;}
			h.assertTrue(rejected,"Oversized list is rejected before allocation or coordinate reads");
			rejected=false;try {new DreamCue(4,true,List.of(BlockPos.ZERO,BlockPos.ZERO.above()));}catch(IllegalArgumentException expected){rejected=true;}
			h.assertTrue(rejected,"Resonance cannot carry multiple targets");
			rejected=false;try {cue.sites().add(BlockPos.ZERO);}catch(UnsupportedOperationException expected){rejected=true;}
			h.assertTrue(rejected,"Cue site storage is immutable");h.succeed();
		} finally {b.release();}
	}
}

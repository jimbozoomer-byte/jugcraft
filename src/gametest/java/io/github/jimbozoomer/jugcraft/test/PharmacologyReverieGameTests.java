package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;

/** Revealed input hints must stay gated and lead to actual paid, server-validated advanced lessons. */
public class PharmacologyReverieGameTests {
	private static final String ARENA="jugcraft-test:arms_arena";
	private static ServerPlayer actor(GameTestHelper h,BlockPos at) {
		var p=TestPlayers.survivor(h);p.setNoGravity(true);p.setOnGround(true);p.getInventory().clearContent();
		p.setPos(Vec3.atCenterOf(at).add(0,0,2));p.getInventory().setItem(0,new ItemStack(DreamStudies.FOLIO));
		p.getInventory().setItem(1,new ItemStack(PsychedelicPharmacy.item("study_canvas"),2));
		h.getLevel().setBlockAndUpdate(at,DreamStudies.PLINTH.defaultBlockState());return p;
	}
	private static void seconds(ServerPlayer p,int n) {for(int i=0;i<n;i++)Pharmacology.tickSecond(p);}
	private static void press(ServerPlayer p,DreamStudyMenu m,int button) {RateGate.forget(p.getUUID());m.clickMenuButton(p,button);}
	private static DreamStudyMenu menu(ServerPlayer p,BlockPos at) {var m=new DreamStudyMenu(31,p.getInventory(),at);p.containerMenu=m;return m;}
	@GameTest(structure=ARENA) public void advancedInputRevealsRequireOnsetAndProduceExactlyOnePaidImprint(GameTestHelper h) {
		var at=h.absolutePos(new BlockPos(5,2,5));
		for(var profile:java.util.List.of(PsychedelicPharmacy.LSD,PsychedelicPharmacy.DMT)) {
			var p=actor(h,at);var m=menu(p,at);int lesson=profile==PsychedelicPharmacy.LSD?1:2;press(p,m,4+lesson);
			h.assertTrue(m.trace(0)==-1 && m.trace(1)==-1 && m.trace(2)==-1,"Sober advanced inputs are hidden");
			new ItemStack(PsychedelicPharmacy.item(profile.id())).finishUsingItem(p.level(),p);seconds(p,19);m.broadcastChanges();
			h.assertTrue(m.trace(0)==-1,"Pending formulation cannot reveal a required input");seconds(p,1);m.broadcastChanges();
			int[] source={m.clue(0),m.clue(1),m.clue(2)};
			int[] expected=lesson==1?new int[]{source[2],source[1],source[0]}:new int[]{(source[0]+1)%4,(source[1]+1)%4,(source[2]+1)%4};
			for(int i=0;i<3;i++)h.assertTrue(m.trace(i)==expected[i],"Synchronized reveal describes the actual advanced rule");
			for(int value:expected)press(p,m,value);
			var reward=PsychedelicPharmacy.item(lesson==1?"pattern_imprint":"oneiric_imprint");
			h.assertTrue(m.status()==2 && p.getInventory().countItem(reward)==1 && p.getInventory().countItem(PsychedelicPharmacy.item("study_canvas"))==1,"Following the hint pays exactly one canvas for the real imprint");
			press(p,m,expected[2]);h.assertTrue(p.getInventory().countItem(reward)==1 && m.trace(0)==-1,"Completion hides inputs and rejects duplicate rewards");
		}
		h.succeed();
	}
	@GameTest(structure=ARENA) public void higherLayerFolioAndCleansingGatesStillOverrideTheHint(GameTestHelper h) {
		var at=h.absolutePos(new BlockPos(5,2,5));var p=actor(h,at);var m=menu(p,at);
		new ItemStack(PsychedelicPharmacy.item("lsd")).finishUsingItem(p.level(),p);seconds(p,20);press(p,m,6);
		h.assertTrue(m.trace(0)==-1 && m.clue(0)==-1,"LSD cannot expose the DMT-only Reverie rule inputs");
		new ItemStack(PsychedelicPharmacy.item("dmt")).finishUsingItem(p.level(),p);seconds(p,20);m.broadcastChanges();
		h.assertTrue(m.trace(0)>=0 && m.trace(-1)==-1 && m.trace(3)==-1,"DMT reveals bounded input hints after its own onset");
		int old=m.trace(0);p.getInventory().setItem(0,ItemStack.EMPTY);m.broadcastChanges();press(p,m,old);
		h.assertTrue(m.trace(0)==-1 && p.getInventory().countItem(PsychedelicPharmacy.item("study_canvas"))==2,"Missing Folio hides hints and stale input cannot spend payment");
		p.getInventory().setItem(0,new ItemStack(DreamStudies.FOLIO));m.broadcastChanges();h.assertTrue(m.trace(0)>=0,"Authorized carried Folio restores hints");
		new ItemStack(Items.MILK_BUCKET).finishUsingItem(p.level(),p);m.broadcastChanges();press(p,m,old);
		h.assertTrue(m.trace(0)==-1 && p.getInventory().countItem(PsychedelicPharmacy.item("oneiric_imprint"))==0 && Pharmacology.strain(p)>0,"Cleansing removes input authority without an imprint or strain refund");
		h.succeed();
	}
}

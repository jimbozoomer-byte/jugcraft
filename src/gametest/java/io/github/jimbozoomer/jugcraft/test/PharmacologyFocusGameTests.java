package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.guns.*;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.TagValueOutput;

/** Real shot outcome, native hunger/rest hooks, onset and cleansing for both release forms. */
public class PharmacologyFocusGameTests {
	private static ServerPlayer player(GameTestHelper h) { var p=TestPlayers.survivor(h);p.setNoGravity(true);p.setOnGround(true);return p; }
	private static void seconds(ServerPlayer p,int n) { for(int i=0;i<n;i++)Pharmacology.tickSecond(p); }
	private static float exhaustion(ServerPlayer p) { var out=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);p.getFoodData().addAdditionalSaveData(out);return out.buildResult().getFloatOr("foodExhaustionLevel",-1); }
	@GameTest public void methylphenidateFocusStartsAfterOnsetAndNeverChangesHipFire(GameTestHelper h) {
		for(var profile:List.of(PrecisionPharmacy.METHYLPHENIDATE,ControlledRelease.METHYLPHENIDATE_XR)) {
			var p=player(h);Pharmacology.drink(p,profile);
			h.assertTrue(Pharmacology.aimSpreadMultiplier(p,true)==1,"Onset grants no accuracy");seconds(p,profile.onsetSeconds());
			h.assertTrue(Pharmacology.aimSpreadMultiplier(p,true)==.75F && Pharmacology.aimSpreadMultiplier(p,false)==1,"Both forms tighten only aimed spread by 25 percent");
			var s=Pharmacology.state(p);p.setAttached(Pharmacology.STATE,new DrugState(s.doses(),s.history(),2100));
			h.assertTrue(Math.abs(Pharmacology.aimSpreadMultiplier(p,true)-1.125F)<.00001,"Critical strain still outweighs focus; strongest drawback applies once");
			new ItemStack(Items.MILK_BUCKET).finishUsingItem(p.level(),p);
			h.assertTrue(Pharmacology.aimSpreadMultiplier(p,true)==1.5F,"Milk ends focus immediately but keeps critical strain");
		}
		h.succeed();
	}
	@GameTest public void modafinilBothFormsSaveRealActivityFoodAndHoldTheNativeRestCounter(GameTestHelper h) {
		for(var profile:List.of(PrecisionPharmacy.MODAFINIL,ControlledRelease.MODAFINIL_XR)) {
			var p=player(h);p.awardStat(Stats.TIME_SINCE_REST,72000);Pharmacology.drink(p,profile);
			h.assertTrue(p.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST))==72000,"Onset does not reset rest");
			seconds(p,profile.onsetSeconds());
			h.assertTrue(p.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST))==0,"Activation resets the native phantom counter");
			float before=exhaustion(p);p.causeFoodExhaustion(1);
			h.assertTrue(Math.abs(exhaustion(p)-before-.85F)<.00001,"Real activity exhaustion is 15 percent lower");
			p.awardStat(Stats.TIME_SINCE_REST,20);seconds(p,1);
			h.assertTrue(p.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST))==0,"Active pulse maintains rest rather than only resetting once");
			Pharmacology.drink(p,Pharmacology.REHYDRATION_SALTS);seconds(p,5);before=exhaustion(p);p.causeFoodExhaustion(1);
			h.assertTrue(Math.abs(exhaustion(p)-before-.8F)<.00001,"Better Salts discount wins without multiplying");
			new ItemStack(Items.MILK_BUCKET).finishUsingItem(p.level(),p);p.awardStat(Stats.TIME_SINCE_REST,20);seconds(p,1);
			h.assertTrue(p.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST))==20 && Pharmacology.activityExhaustion(p,1)==1,"Cleansing stops both work benefits without a new sleep debuff");
		}
		h.succeed();
	}
	@GameTest(structure="jugcraft-test:arms_arena")
	public void aRealAimedShotHitsWithFocusWhereTheSameSeedMissesWithoutIt(GameTestHelper h) {
		var p=player(h);var origin=h.absolutePos(new BlockPos(2,8,1));p.setPos(origin.getX()+.5,origin.getY(),origin.getZ()+.5);p.setYRot(0);p.setXRot(0);
		var pig=GunsGameTests.pig(h,new BlockPos(2,8,31));pig.setNoGravity(true);
		pig.setPos(p.getX(),p.getEyeY()-.45,p.getZ()+30);float health=pig.getHealth();
		// Fixed fixture: a near-edge spread sample beside a narrow target, 30 blocks along a clear sightline.
		long seed=0;
		for(;seed<100000;seed++) { var r=RandomSource.create(seed);if(r.nextDouble()>.98 && Math.abs(Math.cos(r.nextDouble()*Math.PI*2))>.99)break; }
		h.assertTrue(seed<100000,"Near-edge deterministic sample exists");
		var gun=new ItemStack(JugcraftGuns.GUNS.get("sprocket_gun"));GunItem.setLoaded(gun,4);p.setItemInHand(InteractionHand.MAIN_HAND,gun);p.startUsingItem(InteractionHand.MAIN_HAND);
		p.getRandom().setSeed(seed);h.assertTrue(GunShots.fire(p),"Unfocused native aimed shot fires");
		h.assertTrue(pig.getHealth()==health,"The near-edge native shot misses the narrow target without focus");
		Pharmacology.drink(p,PrecisionPharmacy.METHYLPHENIDATE);seconds(p,20);p.getRandom().setSeed(seed);
		h.assertTrue(GunShots.fire(p),"Focused native aimed shot fires");
		h.assertTrue(pig.getHealth()<health && GunItem.loaded(gun)==2,"Same aim/sample now hits through the real shot path and spends exactly two rounds total");
		h.succeed();
	}
}

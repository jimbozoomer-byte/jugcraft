package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.TagValueOutput;

/** Production accounting, real stat/food hooks, legacy saves and persistence under the new soft threshold. */
public class PharmacologyStrainGameTests {
	private static ServerPlayer player(GameTestHelper h) { var p=TestPlayers.survivor(h);p.setNoGravity(true);p.setOnGround(true);return p; }
	private static void seconds(ServerPlayer p,int count) { for(int i=0;i<count;i++)Pharmacology.tickSecond(p); }
	private static float exhaustion(ServerPlayer p) { var out=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);p.getFoodData().addAdditionalSaveData(out);return out.buildResult().getFloatOr("foodExhaustionLevel",-1); }
	@GameTest public void strainDecaysDuringOnsetAndCanClearBeforeBenefits(GameTestHelper h) {
		var p=player(h);Pharmacology.drink(p,Pharmacology.CAFFEINE);seconds(p,10);
		h.assertTrue(Pharmacology.state(p).strainTenths()==90 && p.hasEffect(MobEffects.HASTE),"Ten online seconds remove one point during onset");
		seconds(p,90);
		h.assertTrue(Pharmacology.strain(p)==0 && Pharmacology.activeProfile(p,Pharmacology.CAFFEINE),"Caffeine stays active after its strain clears");
		h.succeed();
	}
	@GameTest public void inactiveRecoveryDecaysTwiceAsFastWithoutLockingUse(GameTestHelper h) {
		var p=player(h);Pharmacology.drink(p,PrecisionPharmacy.METHYLPHENIDATE);seconds(p,10);
		Pharmacology.cancelFamily(p,"stimulant");int before=Pharmacology.state(p).strainTenths();seconds(p,10);
		h.assertTrue(before==340 && Pharmacology.state(p).strainTenths()==320 && Pharmacology.mayDrink(p,Pharmacology.CAFFEINE,false),"Recovery still exists, but ten inactive seconds remove two points and permit a new paid formulation");h.succeed();
	}
	@GameTest public void crossingTheThresholdConsumesTheRealItemAndImmediatelyAppliesPenalties(GameTestHelper h) {
		var p=player(h);p.setAttached(Pharmacology.STATE,new DrugState(List.of(),List.of(),1000));
		var stack=new ItemStack(PrecisionPharmacy.item("methylphenidate"),2);var result=stack.finishUsingItem(p.level(),p);
		h.assertTrue(result.getCount()==1 && Pharmacology.state(p).strainTenths()==1350,"A valid dose is consumed above the old cap");
		h.assertTrue(Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED)-.09)<.00001 && Math.abs(p.getAttributeValue(Attributes.BLOCK_BREAK_SPEED)-.9)<.00001,"Overloaded penalties apply immediately, even during onset");h.succeed();
	}
	@GameTest public void everyBoundaryChangesActualMovementMiningAttackFoodAndSpread(GameTestHelper h) {
		var p=player(h);int[] points={0,100,101,125,126,150,151,200,201};
		double[] move={1,1,1,1,.9,.9,.8,.8,.7}, mine={1,1,1,1,.9,.9,.75,.75,.6}, attack={1,1,1,1,1,1,.85,.85,.75};
		float[] food={1,1,1.15F,1.15F,1.25F,1.25F,1.4F,1.4F,1.6F}, spread={1,1,1.1F,1.1F,1.2F,1.2F,1.35F,1.35F,1.5F};
		for(int i=0;i<points.length;i++) {
			p.setAttached(Pharmacology.STATE,new DrugState(List.of(),List.of(),points[i]*10+2));Pharmacology.tickSecond(p);
			h.assertTrue(Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED)-.1*move[i])<.00001 && Math.abs(p.getAttributeValue(Attributes.BLOCK_BREAK_SPEED)-mine[i])<.00001 && Math.abs(p.getAttributeValue(Attributes.ATTACK_SPEED)-4*attack[i])<.00001,"Real attributes at strain "+points[i]);
			float before=exhaustion(p);p.causeFoodExhaustion(1);
			h.assertTrue(Math.abs(exhaustion(p)-before-food[i])<.00001 && Math.abs(Pharmacology.aimSpreadMultiplier(p)-spread[i])<.00001,"Actual exhaustion and server shot-cone multiplier at "+points[i]);
		}
		p.setAttached(Pharmacology.STATE,new DrugState(List.of(),List.of(),1251));Pharmacology.tickSecond(p);
		h.assertTrue(DrugStrain.band(Pharmacology.state(p).strainTenths())==DrugStrain.Band.STRAINED,"Decay steps down across a fractional threshold");h.succeed();
	}
	@GameTest public void strongerDrugPenaltiesWinWithoutMultiplyingStrainPenalties(GameTestHelper h) {
		var p=player(h);Pharmacology.drink(p,PrecisionPharmacy.FENTANYL);seconds(p,5);
		var s=Pharmacology.state(p);p.setAttached(Pharmacology.STATE,new DrugState(s.doses(),s.history(),1401));Pharmacology.tickSecond(p);
		h.assertTrue(Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED)-.08)<.00001 && Math.abs(p.getAttributeValue(Attributes.BLOCK_BREAK_SPEED)-.8)<.00001,"20-percent drowsiness wins over 10-percent overloaded penalties");
		s=Pharmacology.state(p);p.setAttached(Pharmacology.STATE,new DrugState(s.doses(),s.history(),2011));Pharmacology.tickSecond(p);
		h.assertTrue(Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED)-.07)<.00001 && Math.abs(p.getAttributeValue(Attributes.BLOCK_BREAK_SPEED)-.6)<.00001,"Critical strain wins over drowsiness without an extra multiplier");h.succeed();
	}
	@GameTest public void replacingFamiliesPaysAgainRestartsOnsetAndCannotBankDuration(GameTestHelper h) {
		var p=player(h);Pharmacology.drink(p,Pharmacology.CAFFEINE);seconds(p,10);Pharmacology.drink(p,PrecisionPharmacy.METHYLPHENIDATE);
		var s=Pharmacology.state(p);var dose=s.doses().getFirst();
		h.assertTrue(s.doses().size()==1 && s.strainTenths()==440 && dose.total()==900 && dose.onset()==20 && !p.hasEffect(Pharmacology.CAFFEINATED) && !p.hasEffect(MobEffects.HASTE),"Old benefits end and the new family formulation pays full cost with 75-percent duration");
		seconds(p,20);Pharmacology.drink(p,PrecisionPharmacy.METHYLPHENIDATE);s=Pharmacology.state(p);
		h.assertTrue(s.strainTenths()==770 && s.doses().getFirst().total()==600 && p.getEffect(PrecisionPharmacy.METHYLPHENIDATE.marker()).getDuration()==620*20,"A third dose has 50-percent duration and an exactly replaced native timer");h.succeed();
	}
	@GameTest public void allFamiliesFitAndThreeWayOverlapChargesOnlyOncePerNewDose(GameTestHelper h) {
		var p=player(h);var alcohol=((DrugDrinkItem)PrecisionPharmacy.item("moonshine")).profile();
		for(var profile:List.of(Pharmacology.CAFFEINE,Pharmacology.ASPIRIN,Pharmacology.PARACETAMOL,Pharmacology.REHYDRATION_SALTS,SterilePharmacy.LIDOCAINE,SterilePharmacy.ADRENALINE,SterilePharmacy.SALBUTAMOL,SterilePharmacy.MORPHINE,alcohol,PsychedelicPharmacy.THC,PsychedelicPharmacy.PSILOCYBIN)) h.assertTrue(Pharmacology.drink(p,profile),"Family accepted: "+profile.family());
		h.assertTrue(Pharmacology.doseCost(p,PrecisionPharmacy.KETAMINE)==65,"Two existing restricted families still add only one 25-point surcharge");
		h.assertTrue(Pharmacology.drink(p,PrecisionPharmacy.KETAMINE) && Pharmacology.state(p).doses().size()==12 && Pharmacology.strain(p)>200,"All twelve families fit above the old eight-entry and 100-strain limits");EchoView.cancel(p);h.succeed();
	}
	@GameTest public void cleansingAndDeathPreserveStrainAndToleranceButEndBenefits(GameTestHelper h) {
		var p=player(h);Pharmacology.drink(p,Pharmacology.CAFFEINE);seconds(p,10);var before=Pharmacology.state(p);
		new ItemStack(Items.MILK_BUCKET).finishUsingItem(p.level(),p);
		h.assertTrue(Pharmacology.state(p).strainTenths()==before.strainTenths() && Pharmacology.state(p).history().equals(before.history()) && !p.hasEffect(MobEffects.HASTE),"Milk does not erase the pool or tolerance");
		Pharmacology.drink(p,Pharmacology.CAFFEINE);seconds(p,10);before=Pharmacology.state(p);Pharmacology.onDeath(p);
		h.assertTrue(Pharmacology.state(p).strainTenths()==before.strainTenths() && Pharmacology.state(p).history().equals(before.history()) && !p.hasEffect(MobEffects.HASTE) && !Pharmacology.dosing(p),"Death preserves accounting and ends benefits");h.succeed();
	}
	@GameTest public void oldSavedReservationsMigrateExactlyOnceThenPersistFractionalDecay(GameTestHelper h) {
		var legacy=DrugState.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString("{\"doses\":[{\"drug\":\"caffeine\",\"total\":300,\"remaining\":0,\"onset\":0,\"recovery\":80}],\"history\":[]}")).getOrThrow();
		var p=player(h);p.setAttached(Pharmacology.STATE,legacy);
		h.assertTrue(Pharmacology.state(p).strainTenths()==100,"An old recovery reservation seeds the pool once");seconds(p,7);
		var state=Pharmacology.state(p);var encoded=DrugState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();p.setAttached(Pharmacology.STATE,DrugState.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow());
		h.assertTrue(Pharmacology.state(p).strainTenths()==86,"Reloading fractional strain does not recharge the old reservation");h.succeed();
	}
	@GameTest public void realLogoutAndRejoinPauseThePoolWithoutResettingTolerance(GameTestHelper h) {
		var p=player(h);Pharmacology.drink(p,Pharmacology.CAFFEINE);seconds(p,7);var before=Pharmacology.state(p);var id=p.getUUID();TestPlayers.leave(p);var joined=TestPlayers.again(h,id);
		h.assertTrue(Pharmacology.state(joined).equals(before),"Native player persistence retains strain fractions and dose/tolerance clocks without offline decay");h.succeed();
	}
	@GameTest public void replacementDoesNotRemoveStrongerOrdinaryPotions(GameTestHelper h) {
		var p=player(h);p.addEffect(new MobEffectInstance(MobEffects.HASTE,12000,2));p.addEffect(new MobEffectInstance(MobEffects.SPEED,12000,2));
		Pharmacology.drink(p,Pharmacology.CAFFEINE);seconds(p,10);Pharmacology.drink(p,PrecisionPharmacy.METHYLPHENIDATE);
		h.assertTrue(p.getEffect(MobEffects.HASTE).getAmplifier()==2 && p.getEffect(MobEffects.SPEED).getAmplifier()==2,"Family replacement leaves independent stronger potions intact");h.succeed();
	}
	@GameTest public void realDamageDoesNotCreateStrainAndDeathKeepsOnlineDecay(GameTestHelper h) {
		var p=player(h);p.hasChangedDimension();p.connection.handleAcceptPlayerLoad(new net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket());
		p.hurtServer(p.level(),p.damageSources().generic(),1);
		h.assertTrue(p.getHealth()<20 && !p.hasAttached(Pharmacology.STATE),"An actual injury does not create pharmacy strain or an injury ledger");
		Pharmacology.drink(p,Pharmacology.CAFFEINE);int before=Pharmacology.state(p).strainTenths();
		p.setInvulnerableTime(0);p.damageCooldownTime=0;p.hurtServer(p.level(),p.damageSources().genericKill(),1000);
		h.assertTrue(!p.isAlive() && !p.hasEffect(Pharmacology.CAFFEINATED) && Pharmacology.state(p).strainTenths()==before,"The real death event ends benefits without erasing the pool");
		Pharmacology.tickSecond(p);
		h.assertTrue(Pharmacology.state(p).strainTenths()==before-2,"Online time on the death screen uses the inactive decay rate");h.succeed();
	}
}

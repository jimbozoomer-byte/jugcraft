package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.pharmacology.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.*;

/** Damage spends the upgraded shield; the larger scout remains time-bound and accounting survives. */
public class PharmacologyCalmGameTests {
	@GameTest public void savedPartlySpentLidocaineDoesNotRefillOnRejoin(GameTestHelper h) {
		var p=TestPlayers.survivor(h);Pharmacology.drink(p,SterilePharmacy.LIDOCAINE);
		for(int i=0;i<5;i++)Pharmacology.tickSecond(p);p.setAbsorptionAmount(5);
		var before=Pharmacology.state(p);var id=p.getUUID();TestPlayers.leave(p);var joined=TestPlayers.again(h,id);
		h.assertTrue(joined.getAbsorptionAmount()==5 && Pharmacology.state(joined).equals(before),"Native save/rejoin preserves a partly spent shield and dose accounting");
		Pharmacology.tickSecond(joined);h.assertTrue(joined.getAbsorptionAmount()==5,"Resumed benefits cannot create a fresh shield grant");h.succeed();
	}
	@GameTest public void fourHeartShieldAbsorbsRealDamageAndNeverRefillsFromPulses(GameTestHelper h) {
		var p=TestPlayers.survivor(h);p.setNoGravity(true);p.setOnGround(true);p.hasChangedDimension();
		p.connection.handleAcceptPlayerLoad(new net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket());
		var result=new ItemStack(SterilePharmacy.item("lidocaine")).finishUsingItem(p.level(),p);
		h.assertTrue(result.is(Items.GLASS_BOTTLE) && p.getAbsorptionAmount()==0,"Actual vial starts onset and returns one bottle");
		for(int i=0;i<5;i++)Pharmacology.tickSecond(p);
		h.assertTrue(p.getAbsorptionAmount()==8,"Four hearts are eight real absorption health points");
		p.setHealth(20);p.setInvulnerableTime(0);p.damageCooldownTime=0;p.hurtServer(p.level(),p.damageSources().fall(),3);
		h.assertTrue(p.getHealth()==20 && p.getAbsorptionAmount()==5,"Actual damage consumes three shield points before health");
		for(int i=0;i<50;i++)Pharmacology.tickSecond(p);
		h.assertTrue(p.getAbsorptionAmount()==5,"Recurring benefit pulses do not refill a partially spent shield");
		new ItemStack(Items.MILK_BUCKET).finishUsingItem(p.level(),p);Pharmacology.tickSecond(p);
		h.runAfterDelay(2,() -> {h.assertTrue(p.getAbsorptionAmount()==0 && !p.hasEffect(SterilePharmacy.ABSORPTION) && Pharmacology.state(p).strainTenths()>0,"Milk removes shield through native attribute cleanup without refunding strain");h.succeed();});
	}
	@GameTest(maxTicks=640) public void widerEchoStillExpiresAfterThirtyNativeSeconds(GameTestHelper h) {
		var p=TestPlayers.survivor(h);p.setNoGravity(true);p.setOnGround(true);p.getAbilities().invulnerable=true;
		var body=p.position();new ItemStack(PrecisionPharmacy.item("ketamine")).finishUsingItem(p.level(),p);
		h.assertTrue(EchoView.active(p),"Actual Ketamine vial opens a session");
		h.runAfterDelay(580,() -> h.assertTrue(EchoView.active(p),"View remains usable shortly before the original thirty-second deadline"));
		h.runAfterDelay(610,() -> {
			h.assertTrue(!EchoView.active(p) && !p.hasEffect(PrecisionPharmacy.KETAMINE.marker()) && p.position().distanceToSqr(body)<.001,"Original deadline restores the view without moving the body");
			h.assertTrue(Pharmacology.state(p).strainTenths()>0 && !Pharmacology.state(p).history().isEmpty(),"Expiry retains decaying strain and tolerance");h.succeed();
		});
	}
}

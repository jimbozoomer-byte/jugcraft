package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.guns.GunShots;
import io.github.jimbozoomer.jugcraft.guns.JugcraftGuns;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * In-game tests for heavy bullets (docs/features/guns.md, the knockback and armour-piercing follow-up): the Jawbreaker's
 * bullets knock back what they hurt further than a hit's own push, and pass through 4.5 points of its armour, as the
 * owner's Uppercut data has them; and (the older guns follow-up) the Caisson Pistol's arc passes through 1 point. Shooters
 * face south (+z).
 */
public class HeavyBulletsGameTests {
	/**
	 * The heavy bullets are the owner's, at the owner's numbers: the Jawbreaker's (the Uppercut's) knock back 0.6 further
	 * and pierce 4.5 points of armour; slice 15C's Impetus Shotgun's (the Inertial's) knock back 0.65 further, and the
	 * Hawker Rifle's and the Quill Rifle's (the MAS Peddler's and the Zilk 45's) pierce 1 and 1.5 points; slice 15A's
	 * Vesper Rifle's and Onrush SMG's (the MAS 55's and the Prush Gun's) pierce 4.5 and 3, slice 15B's Kreuzer Pistol's
	 * and Psalm SMG's (the Krauser's and the Soul Drummer's) 1 each; slice 15D's Auger Shotgun's (the Drill Conversion's)
	 * knock back 0.65 further and the Tumbler Rifle's (the Lockewood's) pierce 3 points; slice 15E's Geode Cutter's beam
	 * (the Shard Culler's) pierces 4, slice 16A's Highball Repeater's (the Saketini Ironport's) 2, and slice 16B's Halibut
	 * Gun's (the Floundergat's) knock back 0.65 further. The older guns follow-up gives 24 of the guns before slice 15 the
	 * owner's knockback and 27 the owner's armour piercing, the Caisson Pistol's on its arc. The Gallant Rifle's (the Valora's) are plain,
	 * and so is the Stoker's flame, whose owner's knockback is left out.
	 */
	@GameTest
	public void heavyBulletsAreTheOwners(GameTestHelper helper) {
		helper.assertTrue(JugcraftGuns.KNOCKBACK.equals(Map.ofEntries(Map.entry("jawbreaker", 0.6F), Map.entry("impetus_shotgun", 0.65F), Map.entry("auger_shotgun", 0.65F),
				Map.entry("thunderpipe", 0.75F), Map.entry("haymaker", 0.6F), Map.entry("coach_gun", 0.75F), Map.entry("bellmouth", 0.8F),
				Map.entry("bulldog_pistol", 0.85F), Map.entry("breacher", 0.65F), Map.entry("rattler_pistol", 0.8F), Map.entry("bronco_smg", 0.85F),
				Map.entry("sledge", 0.65F), Map.entry("mule", 0.65F), Map.entry("fowler", 0.8F), Map.entry("culverin", 0.8F),
				Map.entry("reverb", 0.65F), Map.entry("tusker", 0.65F), Map.entry("crackling", 0.65F), Map.entry("goshawk_revolver", 3.0F),
				Map.entry("waystation_shotgun", 0.7F), Map.entry("flatboat_gun", 8.0F), Map.entry("holler_pistol", 0.65F),
				Map.entry("minuet_pistol", 0.55F), Map.entry("half_crown_revolver", 0.55F), Map.entry("holler_carbine", 0.65F),
				Map.entry("navvy_shotgun", 0.85F), Map.entry("freeholder_rifle", 0.4F), Map.entry("halibut_gun", 0.65F))),
				"The guns that knock back further are not the owner's: " + JugcraftGuns.KNOCKBACK);
		helper.assertTrue(JugcraftGuns.PIERCING.equals(Map.ofEntries(Map.entry("vesper_rifle", 4.5F), Map.entry("onrush_smg", 3.0F), Map.entry("kreuzer_pistol", 1.0F), Map.entry("psalm_smg", 1.0F),
				Map.entry("jawbreaker", 4.5F), Map.entry("hawker_rifle", 1.0F), Map.entry("quill_rifle", 1.5F), Map.entry("tumbler_rifle", 3.0F),
				Map.entry("geode_cutter", 4.0F), Map.entry("highball_repeater", 2.0F), Map.entry("patchwork_carbine", 2.0F), Map.entry("warden_pistol", 1.0F),
				Map.entry("riveter_smg", 1.0F), Map.entry("longhorn_rifle", 3.5F), Map.entry("sapper_revolver", 2.5F),
				Map.entry("garrison_rifle", 2.0F), Map.entry("thresher", 1.0F), Map.entry("kestrel_rifle", 3.0F), Map.entry("caisson_pistol", 1.0F),
				Map.entry("highwayman", 2.5F), Map.entry("undertone_rifle", 6.5F), Map.entry("murmur_smg", 7.0F), Map.entry("reverb", 7.0F),
				Map.entry("cinder_repeater", 2.0F), Map.entry("ashfall_pistol", 2.0F), Map.entry("conductor_carbine", 2.5F),
				Map.entry("lament_rifle", 7.0F), Map.entry("ammonite_carbine", 2.0F), Map.entry("highball_rifle", 2.5F),
				Map.entry("flatboat_gun", 2.0F), Map.entry("millstone_rifle", 1.5F), Map.entry("holler_pistol", 5.0F),
				Map.entry("half_crown_revolver", 4.0F), Map.entry("holler_carbine", 5.0F), Map.entry("grange_carbine", 2.0F),
				Map.entry("sprocket_gun", 3.0F), Map.entry("old_grange_carbine", 2.5F))),
				"The guns that pierce armour are not the owner's: " + JugcraftGuns.PIERCING);
		helper.assertTrue(JugcraftGuns.knockback(JugcraftGuns.GUNS.get("gallant_rifle")) == 0.0F
				&& JugcraftGuns.piercing(JugcraftGuns.GUNS.get("gallant_rifle")) == 0.0F, "The Gallant Rifle's bullets are heavy too");
		helper.assertTrue(JugcraftGuns.knockback(JugcraftGuns.GUNS.get("stoker")) == 0.0F, "The Stoker's flame knocks back");
		helper.succeed();
	}

	/**
	 * Three pigs in armour of 10 points that a hit can push. One is shot by the Jawbreaker and one by the Kreuzer Pistol,
	 * side by side: the Jawbreaker's is knocked away faster. The third takes the Jawbreaker's damage as a plain bullet
	 * hit: the Jawbreaker's own hit took more from its pig, through the armour, and no more than its damage.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 40)
	public void jawbreakerKnocksBackAndPiercesArmour(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		Mob struck = armouredPig(helper, new BlockPos(1, 2, 4));
		Mob light = armouredPig(helper, new BlockPos(5, 2, 4));
		Mob plain = armouredPig(helper, new BlockPos(9, 2, 4));
		ServerPlayer jaw = NetherGunsGameTests.shooter(helper, "jawbreaker", new BlockPos(1, 2, 1), struck);
		ServerPlayer kreuzer = NetherGunsGameTests.shooter(helper, "kreuzer_pistol", new BlockPos(5, 2, 1), light);
		helper.assertTrue(GunShots.fire(jaw) && GunShots.fire(kreuzer), "The Jawbreaker or the Kreuzer Pistol did not fire");
		double pushed = struck.getDeltaMovement().horizontalDistance();
		double nudged = light.getDeltaMovement().horizontalDistance();
		helper.assertTrue(pushed > nudged + 0.2, "The Jawbreaker's hit did not knock its pig away faster: " + pushed + " against " + nudged);
		float damage = JugcraftGuns.SPECS.get("jawbreaker").damage();
		DamageSource bullet = new DamageSource(helper.getLevel().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
				.getOrThrow(JugcraftGuns.BULLET), jaw, jaw);
		plain.hurtServer(helper.getLevel(), bullet, damage);
		float pierced = 200.0F - struck.getHealth();
		float stopped = 200.0F - plain.getHealth();
		helper.assertTrue(pierced > stopped + 0.5F && pierced <= damage + 1.0E-3F,
				"Through 10 points of armour the Jawbreaker took " + pierced + ", a plain bullet hit of " + damage + " took " + stopped);
		helper.succeed();
	}

	/**
	 * The Caisson Pistol's arc (the older guns follow-up) strikes its pig in 10 points of armour through 1 point less: the
	 * pig takes more than a second pig takes from a plain zap of the pistol's damage, and no more than that damage. The
	 * pigs stand six blocks apart, out of the arc's reach of each other.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 40)
	public void caissonArcPiercesArmour(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		Mob struck = armouredPig(helper, new BlockPos(1, 2, 4));
		Mob plain = armouredPig(helper, new BlockPos(7, 2, 4));
		ServerPlayer shooter = NetherGunsGameTests.shooter(helper, "caisson_pistol", new BlockPos(1, 2, 1), struck);
		helper.assertTrue(GunShots.fire(shooter), "The Caisson Pistol did not fire");
		float damage = JugcraftGuns.SPECS.get("caisson_pistol").damage();
		DamageSource zap = new DamageSource(helper.getLevel().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
				.getOrThrow(JugcraftGuns.ZAP_DAMAGE), shooter, shooter);
		plain.hurtServer(helper.getLevel(), zap, damage);
		float pierced = 200.0F - struck.getHealth();
		float stopped = 200.0F - plain.getHealth();
		helper.assertTrue(pierced > stopped + 0.1F && pierced <= damage + 1.0E-3F,
				"Through 10 points of armour the Caisson Pistol's arc took " + pierced + ", a plain zap of " + damage + " took " + stopped);
		helper.succeed();
	}

	/** A sturdy pig of 200 health in 10 points of armour, which a hit can push. */
	private static Mob armouredPig(GameTestHelper helper, BlockPos at) {
		Mob pig = NetherGunsGameTests.sturdyPig(helper, at);
		pig.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.0);
		pig.getAttribute(Attributes.ARMOR).setBaseValue(10.0);
		return pig;
	}
}

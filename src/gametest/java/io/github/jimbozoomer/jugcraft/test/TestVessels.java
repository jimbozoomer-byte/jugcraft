package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.menagerie.CrewedVessel;
import io.github.jimbozoomer.jugcraft.menagerie.PirateCrew;
import java.util.List;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A test-only stand-in for the pirate ship (docs/features/pirate-ship-crews.md), registered by the test mod and never
 * shipped: {@code jugcraft-test:stub_vessel} holds still, uses the standard ten stations and deck, seats its crew
 * through {@link PirateCrew} exactly as the real ship's overrides do, and saves its heave-to flag. Crew tests use it so
 * they do not depend on the ship's movement.
 */
public final class TestVessels implements ModInitializer {
	public static EntityType<StubVessel> STUB;

	@Override
	public void onInitialize() {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, TestForms.id("stub_vessel"));
		STUB = Registry.register(BuiltInRegistries.ENTITY_TYPE, key, EntityType.Builder.<StubVessel>of(StubVessel::new,
				MobCategory.MISC).sized(6.0F, 2.0F).clientTrackingRange(10).build(key));
	}

	/** The stand-in ship: still, unbreakable, seats crew at {@link PirateCrew#STATIONS}. */
	public static final class StubVessel extends Entity implements CrewedVessel {
		private boolean heaveTo;
		/** Set by a test to sink it. */
		public boolean sunk;

		public StubVessel(EntityType<? extends StubVessel> type, Level level) {
			super(type, level);
			noPhysics = true;
			setNoGravity(true);
		}

		@Override
		public List<Station> stations() {
			return PirateCrew.STATIONS;
		}

		@Override
		public AABB deckBounds() {
			return PirateCrew.deckBounds(this);
		}

		@Override
		public boolean sunk() {
			return sunk;
		}

		@Override
		public boolean heaveTo() {
			return heaveTo;
		}

		@Override
		public void setHeaveTo(boolean stop) {
			heaveTo = stop;
		}

		@Override
		protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
			Vec3 seat = PirateCrew.seat(this, passenger);
			return seat != null ? seat : super.getPassengerAttachmentPoint(passenger, dimensions, scale);
		}

		@Override
		protected boolean canAddPassenger(Entity passenger) {
			return PirateCrew.mayBoard(this, passenger);
		}

		@Override
		protected void defineSynchedData(SynchedEntityData.Builder builder) {
		}

		@Override
		public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
			return false;
		}

		@Override
		protected void readAdditionalSaveData(ValueInput input) {
			heaveTo = input.getBooleanOr("HeaveTo", false);
		}

		@Override
		protected void addAdditionalSaveData(ValueOutput output) {
			output.putBoolean("HeaveTo", heaveTo);
		}
	}
}

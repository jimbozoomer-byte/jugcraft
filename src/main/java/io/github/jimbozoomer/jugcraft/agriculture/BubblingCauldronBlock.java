package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Bubbling Cauldron: a witch's iron pot on three legs. A water bucket fills it; a brew ingredient then turns the
 * water to a brew ({@link Brew}: green from spider eyes and slime, purple from nether wart and chorus, orange from
 * glowstone and blaze powder), which glows (light {@value #BREW_LIGHT}). An empty bucket pours the water out again
 * (brews are only for show; pouring one gives the water back too). Over a fire, campfire, magma or lava (the Cooking
 * Pot's heat sources) it bubbles and steams, drawn by clients only.
 */
public class BubblingCauldronBlock extends Block {
	public static final int BREW_LIGHT = 7;
	public static final EnumProperty<Brew> CONTENTS = EnumProperty.create("contents", Brew.class);
	private static final VoxelShape SHAPE = Shapes.or(Block.box(2.0, 2.0, 2.0, 14.0, 13.0, 14.0), Block.box(3.0, 0.0, 3.0, 13.0, 2.0, 13.0));

	/** What is in the cauldron, and for a brew the item tag that makes it. */
	public enum Brew implements StringRepresentable {
		EMPTY, WATER, GREEN, PURPLE, ORANGE;

		/** The items that turn water into this brew ({@code jugcraft:brew/<colour>}), or null for empty and water. */
		public TagKey<Item> ingredients() {
			return ordinal() < GREEN.ordinal() ? null : TagKey.create(Registries.ITEM, Jugcraft.id("brew/" + getSerializedName()));
		}

		public boolean isBrew() {
			return ordinal() >= GREEN.ordinal();
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	public BubblingCauldronBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(CONTENTS, Brew.EMPTY));
	}

	public static int light(BlockState state) {
		return state.getValue(CONTENTS).isBrew() ? BREW_LIGHT : 0;
	}

	/** The brew {@code stack} turns water into, or null if it is no brew ingredient. */
	public static Brew brewFor(ItemStack stack) {
		for (Brew brew : Brew.values()) {
			if (brew.isBrew() && stack.is(brew.ingredients())) {
				return brew;
			}
		}
		return null;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		Brew contents = state.getValue(CONTENTS);
		if (stack.is(Items.WATER_BUCKET) && contents == Brew.EMPTY) {
			if (!level.isClientSide()) {
				player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
				change(level, pos, state, Brew.WATER, player);
				level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
			}
			return InteractionResult.SUCCESS;
		}
		if (stack.is(Items.BUCKET) && contents != Brew.EMPTY) {
			if (!level.isClientSide()) {
				player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.WATER_BUCKET)));
				change(level, pos, state, Brew.EMPTY, player);
				level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
			}
			return InteractionResult.SUCCESS;
		}
		Brew brew = brewFor(stack);
		if (brew != null && contents != Brew.EMPTY && contents != brew) {
			if (!level.isClientSide()) {
				stack.consume(1, player);
				change(level, pos, state, brew, player);
				level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 0.8F);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	private static void change(Level level, BlockPos pos, BlockState state, Brew contents, Player player) {
		level.setBlock(pos, state.setValue(CONTENTS, contents), Block.UPDATE_ALL);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
	}

	/** Over a heat source, its brew or water bubbles; a brew gives off witchy sparkles too. */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		Brew contents = state.getValue(CONTENTS);
		if (contents == Brew.EMPTY || !CookingPotBlockEntity.isHeated(level, pos)) {
			return;
		}
		double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
		double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
		double y = pos.getY() + 12.2 / 16.0;
		level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0.0, 0.02, 0.0);
		if (random.nextInt(3) == 0) {
			level.addParticle(contents.isBrew() ? ParticleTypes.WITCH : ParticleTypes.CLOUD, x, y + 0.1, z, 0.0, 0.03, 0.0);
		}
		if (random.nextInt(12) == 0) {
			level.playLocalSound(x, y, z, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS, 0.6F, 0.8F + random.nextFloat() * 0.4F, false);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(CONTENTS);
	}
}

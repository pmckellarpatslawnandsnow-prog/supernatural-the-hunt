package com.fanmods.snhunt.block;

import com.fanmods.snhunt.registry.ModItems;
import com.fanmods.snhunt.story.Chapter;
import com.fanmods.snhunt.story.StoryManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/** A spirit's remains. Salt them (Rock Salt), then burn them (Flint and Steel or a Fire Charge). */
public class GraveBlock extends Block {
    public static final BooleanProperty SALTED = BooleanProperty.of("salted");

    public GraveBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(SALTED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SALTED);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
                                             PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isOf(ModItems.SALT)) {
            if (state.get(SALTED)) {
                if (!world.isClient) {
                    player.sendMessage(Text.translatable("message.snhunt.grave_already_salted").formatted(Formatting.GRAY), true);
                }
                return ItemActionResult.SUCCESS;
            }
            if (!world.isClient) {
                world.setBlockState(pos, state.with(SALTED, true));
                stack.decrementUnlessCreative(1, player);
                ((ServerWorld) world).spawnParticles(ParticleTypes.WHITE_ASH, pos.getX() + 0.5, pos.getY() + 1.05,
                        pos.getZ() + 0.5, 40, 0.35, 0.05, 0.35, 0.01);
                world.playSound(null, pos, SoundEvents.BLOCK_SAND_PLACE, SoundCategory.BLOCKS, 1.0f, 1.2f);
                player.sendMessage(Text.translatable("message.snhunt.grave_salted").formatted(Formatting.WHITE), true);
            }
            return ItemActionResult.success(world.isClient);
        }
        if (stack.isOf(Items.FLINT_AND_STEEL) || stack.isOf(Items.FIRE_CHARGE)) {
            if (!state.get(SALTED)) {
                if (!world.isClient) {
                    player.sendMessage(Text.translatable("message.snhunt.grave_needs_salt").formatted(Formatting.YELLOW), true);
                }
                return ItemActionResult.SUCCESS;
            }
            if (!world.isClient) {
                if (stack.isOf(Items.FLINT_AND_STEEL)) {
                    stack.damage(1, player, LivingEntity.getSlotForHand(hand));
                } else {
                    stack.decrementUnlessCreative(1, player);
                }
                burn((ServerWorld) world, pos, player);
            }
            return ItemActionResult.success(world.isClient);
        }
        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            String key = state.get(SALTED) ? "message.snhunt.grave_hint_burn" : "message.snhunt.grave_hint";
            player.sendMessage(Text.translatable(key).formatted(Formatting.GRAY), true);
        }
        return ActionResult.SUCCESS;
    }

    private static void burn(ServerWorld world, BlockPos pos, PlayerEntity player) {
        double x = pos.getX() + 0.5, y = pos.getY() + 1.0, z = pos.getZ() + 0.5;
        world.spawnParticles(ParticleTypes.FLAME, x, y, z, 80, 0.4, 0.5, 0.4, 0.05);
        world.spawnParticles(ParticleTypes.LAVA, x, y, z, 15, 0.3, 0.2, 0.3, 0.0);
        world.spawnParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.5, z, 30, 0.3, 0.8, 0.3, 0.02);
        world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0f, 0.8f);
        world.playSound(null, pos, SoundEvents.ENTITY_GHAST_SCREAM, SoundCategory.HOSTILE, 0.7f, 0.6f);
        world.setBlockState(pos, Blocks.COARSE_DIRT.getDefaultState());
        BlockPos above = pos.up();
        if (world.getBlockState(above).isAir()) {
            world.setBlockState(above, Blocks.FIRE.getDefaultState());
        }
        player.sendMessage(Text.translatable("message.snhunt.grave_burned").formatted(Formatting.GOLD), true);
        StoryManager.onAnchorDestroyed(world, pos, Chapter.Objective.SALT_BURN);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(3) == 0) {
            world.addParticle(ParticleTypes.SOUL, pos.getX() + random.nextDouble(), pos.getY() + 1.05,
                    pos.getZ() + random.nextDouble(), 0, 0.02, 0);
        }
    }
}

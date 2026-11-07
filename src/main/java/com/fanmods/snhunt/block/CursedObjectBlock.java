package com.fanmods.snhunt.block;

import com.fanmods.snhunt.story.Chapter;
import com.fanmods.snhunt.story.StoryManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * The source of a monster's power: a haunted mirror, a sacred tree, a binding altar, a sigil.
 * Break it and whatever is bound to it goes too.
 */
public class CursedObjectBlock extends Block {
    public CursedObjectBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    60, 0.4, 0.4, 0.4, 0.2);
            sw.playSound(null, pos, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1.2f, 0.5f);
            sw.playSound(null, pos, SoundEvents.ENTITY_GHAST_SCREAM, SoundCategory.HOSTILE, 0.7f, 0.5f);
            StoryManager.onAnchorDestroyed(sw, pos, Chapter.Objective.BREAK_CURSED);
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        for (int i = 0; i < 2; i++) {
            world.addParticle(ParticleTypes.PORTAL, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() + 0.5,
                    pos.getZ() + random.nextDouble(), (random.nextDouble() - 0.5) * 0.5, -random.nextDouble(),
                    (random.nextDouble() - 0.5) * 0.5);
        }
    }
}

package com.fanmods.snhunt.block;

import com.fanmods.snhunt.entity.HuntMonsterEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Spirits and demons can't cross a line of salt. */
public class SaltLineBlock extends ThinPaintBlock {
    public SaltLineBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && entity instanceof HuntMonsterEntity monster && monster.isRepelledBySalt()) {
            monster.repelFrom(pos);
        }
    }
}

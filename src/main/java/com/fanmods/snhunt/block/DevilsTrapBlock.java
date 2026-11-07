package com.fanmods.snhunt.block;

import com.fanmods.snhunt.entity.DemonEntity;
import com.fanmods.snhunt.entity.GreaterDemonEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** A demon that steps on this is held in place and can be exorcised. */
public class DevilsTrapBlock extends ThinPaintBlock {
    public DevilsTrapBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient) {
            return;
        }
        if (entity instanceof DemonEntity demon) {
            demon.trap();
        } else if (entity instanceof GreaterDemonEntity boss) {
            boss.strainAgainstTrap(world, pos);
        }
    }
}

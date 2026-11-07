package com.fanmods.snhunt.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/** Shoots a burning flare that sets its target alight. The wendigo's worst nightmare. */
public class FlareGunItem extends Item {
    public FlareGunItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack gun = user.getStackInHand(hand);
        if (!world.isClient) {
            ServerWorld sw = (ServerWorld) world;
            HunterUtil.Shot shot = HunterUtil.trace(user, 30);
            HunterUtil.trail(sw, shot.start(), shot.end(), ParticleTypes.FLAME);
            sw.spawnParticles(ParticleTypes.LAVA, shot.end().x, shot.end().y, shot.end().z, 6, 0.2, 0.2, 0.2, 0.0);
            LivingEntity target = shot.target();
            if (target != null) {
                target.setOnFireFor(8);
                target.damage(world.getDamageSources().inFire(), 4.0f);
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_FIRECHARGE_USE,
                    SoundCategory.PLAYERS, 1.0f, 1.3f);
            gun.damage(1, user, LivingEntity.getSlotForHand(hand));
        }
        user.getItemCooldownManager().set(this, 30);
        return TypedActionResult.success(gun, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        HunterUtil.tooltip(this, tooltip);
    }
}

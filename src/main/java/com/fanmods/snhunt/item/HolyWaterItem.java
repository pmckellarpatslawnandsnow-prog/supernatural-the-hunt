package com.fanmods.snhunt.item;

import com.fanmods.snhunt.entity.HolyWaterVulnerable;
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

/** Splash it on a demon (up to 8 blocks) and watch it burn. */
public class HolyWaterItem extends Item {
    public HolyWaterItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            ServerWorld sw = (ServerWorld) world;
            HunterUtil.Shot shot = HunterUtil.trace(user, 8);
            HunterUtil.trail(sw, shot.start(), shot.end(), ParticleTypes.SPLASH);
            sw.spawnParticles(ParticleTypes.SPLASH, shot.end().x, shot.end().y, shot.end().z, 25, 0.3, 0.3, 0.3, 0.1);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SPLASH_POTION_BREAK,
                    SoundCategory.PLAYERS, 0.8f, 1.2f);
            if (shot.target() instanceof HolyWaterVulnerable demon) {
                demon.onHolyWater(user);
            }
            stack.decrementUnlessCreative(1, user);
        }
        user.getItemCooldownManager().set(this, 10);
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        HunterUtil.tooltip(this, tooltip);
    }
}

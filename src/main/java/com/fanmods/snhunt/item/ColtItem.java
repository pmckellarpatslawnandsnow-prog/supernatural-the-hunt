package com.fanmods.snhunt.item;

import com.fanmods.snhunt.entity.HuntMonsterEntity;
import com.fanmods.snhunt.registry.ModItems;
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
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/** The gun that can kill (almost) anything. Each shot uses one Colt Round. */
public class ColtItem extends Item {
    public ColtItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack gun = user.getStackInHand(hand);
        boolean creative = user.getAbilities().creativeMode;
        ItemStack ammo = HunterUtil.findAmmo(user, ModItems.COLT_ROUND);
        if (ammo.isEmpty() && !creative) {
            if (!world.isClient) {
                user.sendMessage(Text.translatable("message.snhunt.no_colt_rounds").formatted(Formatting.RED), true);
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_DISPENSER_FAIL,
                        SoundCategory.PLAYERS, 0.8f, 0.8f);
            }
            return TypedActionResult.fail(gun);
        }
        if (!world.isClient) {
            ServerWorld sw = (ServerWorld) world;
            HunterUtil.Shot shot = HunterUtil.trace(user, 48);
            HunterUtil.trail(sw, shot.start(), shot.end(), ParticleTypes.CRIT);
            sw.spawnParticles(ParticleTypes.SMOKE, shot.start().x, shot.start().y - 0.2, shot.start().z, 6, 0.05, 0.05, 0.05, 0.01);
            LivingEntity target = shot.target();
            if (target instanceof HuntMonsterEntity monster) {
                monster.onColtHit(user);
            } else if (target != null) {
                target.damage(world.getDamageSources().playerAttack(user), 100.0f);
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST,
                    SoundCategory.PLAYERS, 1.6f, 0.45f);
            if (!creative) {
                ammo.decrement(1);
            }
        }
        user.getItemCooldownManager().set(this, 30);
        return TypedActionResult.success(gun, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        HunterUtil.tooltip(this, tooltip);
    }
}

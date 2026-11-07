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

/** Fires rock salt. Disperses spirits, burns demons, stings everything else. Uses Rock Salt as ammo. */
public class SaltShotgunItem extends Item {
    public SaltShotgunItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack gun = user.getStackInHand(hand);
        boolean creative = user.getAbilities().creativeMode;
        ItemStack ammo = HunterUtil.findAmmo(user, ModItems.SALT);
        if (ammo.isEmpty() && !creative) {
            if (!world.isClient) {
                user.sendMessage(Text.translatable("message.snhunt.no_salt").formatted(Formatting.RED), true);
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_DISPENSER_FAIL,
                        SoundCategory.PLAYERS, 0.8f, 1.2f);
            }
            return TypedActionResult.fail(gun);
        }

        if (!world.isClient) {
            ServerWorld sw = (ServerWorld) world;
            HunterUtil.Shot shot = HunterUtil.trace(user, 22);
            HunterUtil.trail(sw, shot.start(), shot.end(), ParticleTypes.WHITE_ASH);
            sw.spawnParticles(ParticleTypes.CLOUD, shot.end().x, shot.end().y, shot.end().z, 8, 0.2, 0.2, 0.2, 0.02);
            LivingEntity target = shot.target();
            if (target instanceof HuntMonsterEntity monster) {
                monster.onSaltHit(user);
            } else if (target != null) {
                target.damage(world.getDamageSources().playerAttack(user), 3.0f);
            }
            if (target != null) {
                target.takeKnockback(0.7, user.getX() - target.getX(), user.getZ() - target.getZ());
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST,
                    SoundCategory.PLAYERS, 1.3f, 0.55f);
            if (!creative) {
                ammo.decrement(1);
            }
            gun.damage(1, user, LivingEntity.getSlotForHand(hand));
        }
        user.getItemCooldownManager().set(this, 18);
        return TypedActionResult.success(gun, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        HunterUtil.tooltip(this, tooltip);
    }
}

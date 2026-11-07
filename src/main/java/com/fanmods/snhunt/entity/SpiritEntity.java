package com.fanmods.snhunt.entity;

import com.fanmods.snhunt.item.HunterUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * Vengeful spirit. Ordinary weapons pass through it; iron and salt disperse it for a few seconds.
 * Bound spirits are only destroyed by salting and burning their remains.
 */
public class SpiritEntity extends HuntMonsterEntity {
    private int dispersedTicks;
    private int flickerTimer = 100;

    public SpiritEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createSpiritAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.27)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0);
    }

    @Override
    public boolean isRepelledBySalt() {
        return true;
    }

    @Override
    protected float adjustDamage(DamageSource source, float amount) {
        ItemStack weapon = HunterUtil.meleeWeapon(source);
        if (HunterUtil.isIron(weapon)) {
            disperse();
            return isBound() ? 0.0f : amount;
        }
        if (source.getAttacker() instanceof PlayerEntity && !isBound() && this.random.nextInt(4) == 0) {
            disperse();
        }
        return 0.0f;
    }

    @Override
    protected Text immuneHint() {
        return isBound() ? Text.translatable("hint.snhunt.spirit_bound") : Text.translatable("hint.snhunt.spirit");
    }

    @Override
    public void onSaltHit(PlayerEntity shooter) {
        disperse();
        if (!isBound()) {
            forceDamage(this.getDamageSources().playerAttack(shooter), 6.0f);
        } else {
            showHint(shooter);
        }
    }

    /** Vanish in a puff of cold air and reappear somewhere nearby. */
    public void disperse() {
        if (!(this.getWorld() instanceof ServerWorld sw) || dispersedTicks > 0) {
            return;
        }
        sw.spawnParticles(ParticleTypes.POOF, getX(), getBodyY(0.5), getZ(), 25, 0.3, 0.6, 0.3, 0.05);
        sw.spawnParticles(ParticleTypes.SNOWFLAKE, getX(), getBodyY(0.5), getZ(), 20, 0.3, 0.6, 0.3, 0.05);
        sw.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_VEX_DEATH, SoundCategory.HOSTILE, 0.8f, 0.7f);
        for (int i = 0; i < 12; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double dist = 8 + this.random.nextDouble() * 8;
            double x = getX() + Math.cos(angle) * dist;
            double z = getZ() + Math.sin(angle) * dist;
            if (this.teleport(x, getY() + this.random.nextInt(5) - 2, z, false)) {
                break;
            }
        }
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 60, 0, false, false));
        dispersedTicks = 60;
    }

    @Override
    public boolean tryAttack(Entity target) {
        if (dispersedTicks > 0) {
            return false;
        }
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living) {
            // A spirit's touch is freezing cold.
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 50, 1));
            living.setFrozenTicks(Math.min(living.getMinFreezeDamageTicks() + 40, living.getFrozenTicks() + 60));
        }
        return hit;
    }

    @Override
    public void tick() {
        super.tick();
        World world = this.getWorld();
        if (world.isClient) {
            if (this.random.nextInt(3) == 0) {
                world.addParticle(ParticleTypes.SNOWFLAKE, getParticleX(0.5), getRandomBodyY(), getParticleZ(0.5), 0, 0.01, 0);
            }
            return;
        }
        if (dispersedTicks > 0) {
            dispersedTicks--;
        }
        // Flicker in and out of sight.
        if (--flickerTimer <= 0) {
            flickerTimer = 80 + this.random.nextInt(80);
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 12, 0, false, false));
        }
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    /** Burned away when the bones go up in flames. */
    public void burnAway() {
        banish();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_VEX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_VEX_DEATH;
    }
}

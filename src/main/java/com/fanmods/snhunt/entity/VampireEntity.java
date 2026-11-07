package com.fanmods.snhunt.entity;

import com.fanmods.snhunt.item.HunterUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/** Heals constantly and only a beheading kills it. Dead man's blood stops the healing. */
public class VampireEntity extends HuntMonsterEntity {
    private int poisonedTicks;

    public VampireEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 15;
    }

    public static DefaultAttributeContainer.Builder createVampireAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 36.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0);
    }

    public boolean isPoisoned() {
        return poisonedTicks > 0;
    }

    public void poison() {
        poisonedTicks = 600;
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 600, 1));
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 1));
        if (this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, getX(), getBodyY(0.6), getZ(), 10, 0.3, 0.4, 0.3, 0.1);
        }
    }

    @Override
    protected float adjustDamage(DamageSource source, float amount) {
        boolean beheader = HunterUtil.isBeheader(HunterUtil.meleeWeapon(source));
        float adjusted = beheader ? amount * 1.5f : amount * (isPoisoned() ? 1.0f : 0.7f);
        if (!beheader && this.getHealth() - adjusted < 1.0f) {
            adjusted = Math.max(0.0f, this.getHealth() - 1.0f);
            if (source.getAttacker() instanceof PlayerEntity player) {
                showHint(player);
            }
        }
        return adjusted;
    }

    @Override
    protected Text immuneHint() {
        return Text.translatable("hint.snhunt.vampire");
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && !isPoisoned()) {
            this.heal(2.0f); // feeding
        }
        return hit;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) {
            return;
        }
        if (poisonedTicks > 0) {
            poisonedTicks--;
        } else if (this.age % 20 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0f);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Poisoned", poisonedTicks);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        poisonedTicks = nbt.getInt("Poisoned");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_CAT_HISS;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_WITCH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WITCH_DEATH;
    }

    @Override
    public float getSoundPitch() {
        return 0.6f + this.random.nextFloat() * 0.2f;
    }
}

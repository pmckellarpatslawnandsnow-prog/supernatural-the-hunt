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
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/** A witch that feeds on life force. Only consecrated iron hurts it: iron weapons and salt rounds. */
public class ShtrigaEntity extends HuntMonsterEntity {
    public ShtrigaEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 20;
    }

    public static DefaultAttributeContainer.Builder createShtrigaAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0);
    }

    @Override
    protected float adjustDamage(DamageSource source, float amount) {
        return HunterUtil.isIron(HunterUtil.meleeWeapon(source)) ? amount * 1.2f : 0.0f;
    }

    @Override
    protected Text immuneHint() {
        return Text.translatable("hint.snhunt.shtriga");
    }

    @Override
    public void onSaltHit(PlayerEntity shooter) {
        forceDamage(this.getDamageSources().playerAttack(shooter), 5.0f);
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 120, 0));
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 120, 1));
            this.heal(3.0f);
        }
        return hit;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient && this.random.nextInt(3) == 0) {
            this.getWorld().addParticle(ParticleTypes.WITCH, getParticleX(0.5), getRandomBodyY(), getParticleZ(0.5), 0, 0, 0);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_WITCH_CELEBRATE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_WITCH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WITCH_DEATH;
    }
}

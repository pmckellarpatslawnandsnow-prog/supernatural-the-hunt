package com.fanmods.snhunt.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/** Fast, strong, and nearly untouchable — unless it's burning. */
public class WendigoEntity extends HuntMonsterEntity {
    public WendigoEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 25;
    }

    public static DefaultAttributeContainer.Builder createWendigoAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.33)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.3);
    }

    @Override
    protected float adjustDamage(DamageSource source, float amount) {
        if (source.isIn(DamageTypeTags.IS_FIRE)) {
            return amount * 3.0f;
        }
        if (this.isOnFire()) {
            return amount * 1.25f;
        }
        return amount * 0.15f;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean result = super.damage(source, amount);
        if (!this.getWorld().isClient && !this.isOnFire() && !source.isIn(DamageTypeTags.IS_FIRE)
                && source.getAttacker() instanceof net.minecraft.entity.player.PlayerEntity player) {
            showHint(player);
        }
        return result;
    }

    @Override
    protected Text immuneHint() {
        return Text.translatable("hint.snhunt.wendigo");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_FOX_SCREECH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_RAVAGER_DEATH;
    }

    @Override
    public float getSoundPitch() {
        return 0.7f + this.random.nextFloat() * 0.2f;
    }
}

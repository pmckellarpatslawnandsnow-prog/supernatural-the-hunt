package com.fanmods.snhunt.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/** Shadow demon. In the dark it's invisible and almost untouchable; in the light it's weak. */
public class DaevaEntity extends HuntMonsterEntity {
    public DaevaEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createDaevaAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 22.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.36)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    public boolean isInDarkness() {
        return this.getWorld().getLightLevel(this.getBlockPos()) < 8;
    }

    @Override
    protected float adjustDamage(DamageSource source, float amount) {
        if (isInDarkness()) {
            if (source.getAttacker() instanceof PlayerEntity player) {
                showHint(player);
            }
            return amount * 0.25f;
        }
        return amount * 2.0f;
    }

    @Override
    protected Text immuneHint() {
        return Text.translatable("hint.snhunt.daeva");
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient && this.age % 10 == 0 && isInDarkness()) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 15, 0, false, false));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_PHANTOM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PHANTOM_DEATH;
    }
}

package com.fanmods.snhunt.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * A pagan harvest god living in a scarecrow. Stands still in daylight. While bound to its sacred tree it
 * can't be hurt at all; wild ones burn well.
 */
public class ScarecrowEntity extends HuntMonsterEntity {
    public ScarecrowEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 25;
    }

    public static DefaultAttributeContainer.Builder createScarecrowAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    protected float adjustDamage(DamageSource source, float amount) {
        if (isBound()) {
            return 0.0f;
        }
        return source.isIn(DamageTypeTags.IS_FIRE) ? amount * 2.0f : amount * 0.6f;
    }

    @Override
    protected Text immuneHint() {
        return Text.translatable("hint.snhunt.scarecrow");
    }

    @Override
    public void tick() {
        super.tick();
        World world = this.getWorld();
        if (!world.isClient && this.age % 10 == 0 && world.isDay() && world.isSkyVisible(this.getBlockPos())) {
            // In daylight it's just a scarecrow on a post.
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 15, 6, false, false));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_HUSK_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLOCK_GRASS_BREAK;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_HUSK_DEATH;
    }
}

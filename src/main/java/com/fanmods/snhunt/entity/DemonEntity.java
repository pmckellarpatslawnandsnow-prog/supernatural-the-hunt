package com.fanmods.snhunt.entity;

import com.fanmods.snhunt.story.StoryManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * A black-eyed demon riding a human host. Ordinary weapons can't finish it (the host would die first),
 * so you trap it in a Devil's Trap and exorcise it, or burn it out with holy water.
 */
public class DemonEntity extends HuntMonsterEntity implements HolyWaterVulnerable {
    private int trappedTicks;
    private int stunnedTicks;

    public DemonEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 20;
    }

    public static DefaultAttributeContainer.Builder createDemonAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.2);
    }

    @Override
    public boolean isRepelledBySalt() {
        return true;
    }

    public boolean isTrapped() {
        return trappedTicks > 0;
    }

    /** Called every tick the demon stands in a Devil's Trap. */
    public void trap() {
        if (trappedTicks <= 0 && this.getWorld() instanceof ServerWorld sw) {
            sw.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_ENDERMAN_SCREAM, SoundCategory.HOSTILE, 0.8f, 0.6f);
        }
        trappedTicks = 10;
    }

    @Override
    protected float adjustDamage(DamageSource source, float amount) {
        float adjusted = amount * 0.6f;
        if (this.getHealth() - adjusted < 1.0f) {
            adjusted = Math.max(0.0f, this.getHealth() - 1.0f);
            stunnedTicks = 40;
            if (source.getAttacker() instanceof PlayerEntity player) {
                showHint(player);
            }
        }
        return adjusted;
    }

    @Override
    protected Text immuneHint() {
        return Text.translatable("hint.snhunt.demon");
    }

    @Override
    public void onSaltHit(PlayerEntity shooter) {
        this.forceDamage(this.getDamageSources().playerAttack(shooter), 0.5f);
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1));
    }

    @Override
    public void onHolyWater(PlayerEntity thrower) {
        if (this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.LARGE_SMOKE, getX(), getBodyY(0.6), getZ(), 20, 0.3, 0.4, 0.3, 0.02);
            sw.playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.HOSTILE, 1.0f, 0.7f);
        }
        this.forceDamage(this.getDamageSources().indirectMagic(thrower, thrower), 10.0f);
        stunnedTicks = 30;
    }

    /** Heard the rite without being trapped: it hurts, but it holds on. */
    public void onRiteHeard(PlayerEntity reader) {
        this.forceDamage(this.getDamageSources().indirectMagic(reader, reader), 2.0f);
        this.takeKnockback(0.8, reader.getX() - getX(), reader.getZ() - getZ());
    }

    /** Cast the demon out. The host is freed (a villager is left behind). */
    public void exorcise(PlayerEntity reader) {
        if (!(this.getWorld() instanceof ServerWorld sw) || this.isRemoved()) {
            return;
        }
        for (int i = 0; i < 6; i++) {
            sw.spawnParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.4 * i, getZ(), 15, 0.25, 0.3, 0.25, 0.05);
        }
        sw.spawnParticles(ParticleTypes.SQUID_INK, getX(), getBodyY(0.8), getZ(), 40, 0.3, 1.5, 0.3, 0.1);
        sw.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 1.0f, 0.6f);

        VillagerEntity host = EntityType.VILLAGER.create(sw);
        if (host != null) {
            host.refreshPositionAndAngles(getX(), getY(), getZ(), getYaw(), 0);
            host.setCustomName(Text.translatable("entity.snhunt.freed_host"));
            sw.spawnEntity(host);
        }
        StoryManager.onResolved(this);
        this.discard();
    }

    @Override
    public boolean tryAttack(Entity target) {
        if (isTrapped() || stunnedTicks > 0) {
            return false;
        }
        return super.tryAttack(target);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) {
            return;
        }
        if (stunnedTicks > 0) {
            stunnedTicks--;
        }
        if (trappedTicks > 0) {
            trappedTicks--;
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 10, 6, false, false));
            this.getNavigation().stop();
            if (this.age % 5 == 0 && this.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.SMOKE, getX(), getY() + 0.1, getZ(), 6, 0.4, 0.05, 0.4, 0.0);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_EVOKER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_EVOKER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_EVOKER_DEATH;
    }
}

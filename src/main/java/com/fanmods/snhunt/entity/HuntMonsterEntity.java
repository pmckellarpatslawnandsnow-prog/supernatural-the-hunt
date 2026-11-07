package com.fanmods.snhunt.entity;

import com.fanmods.snhunt.story.StoryManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for every monster in the mod. Each subclass decides which weapons actually hurt it.
 */
public abstract class HuntMonsterEntity extends HostileEntity {
    private int hintCooldown;
    private int repelCooldown;

    protected HuntMonsterEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 10;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, VillagerEntity.class, false));
    }

    /** Bound monsters are tied to a grave or cursed object and can't be destroyed any other way. */
    public boolean isBound() {
        return this.getCommandTags().contains(StoryManager.TAG_BOUND);
    }

    public boolean isRepelledBySalt() {
        return false;
    }

    /** Change incoming damage. Return 0 (or less) to ignore the hit entirely. */
    protected float adjustDamage(DamageSource source, float amount) {
        return amount;
    }

    /** Shown on the action bar when a weapon doesn't work, so players learn the rules. */
    @Nullable
    protected Text immuneHint() {
        return null;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.damage(source, amount);
        }
        float adjusted = adjustDamage(source, amount);
        if (adjusted <= 0.0f) {
            if (!this.getWorld().isClient && source.getAttacker() instanceof PlayerEntity player) {
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_SHIELD_BLOCK,
                        SoundCategory.HOSTILE, 0.6f, 1.4f);
                showHint(player);
            }
            return false;
        }
        return super.damage(source, adjusted);
    }

    /** Damage that skips this monster's special rules. */
    protected boolean forceDamage(DamageSource source, float amount) {
        return super.damage(source, amount);
    }

    protected void showHint(PlayerEntity player) {
        Text hint = immuneHint();
        if (hint != null && hintCooldown <= 0) {
            player.sendMessage(hint.copy().formatted(Formatting.YELLOW), true);
            hintCooldown = 60;
        }
    }

    /** Hit by a rock-salt round. */
    public void onSaltHit(PlayerEntity shooter) {
        this.damage(this.getDamageSources().playerAttack(shooter), 2.0f);
    }

    /** Hit by the Colt. It kills anything. */
    public void onColtHit(PlayerEntity shooter) {
        if (this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.LARGE_SMOKE, getX(), getBodyY(0.5), getZ(), 20, 0.3, 0.6, 0.3, 0.02);
        }
        forceDamage(this.getDamageSources().playerAttack(shooter), 1000.0f);
    }

    /** Pushed out of a salt line. */
    public void repelFrom(BlockPos pos) {
        Vec3d away = this.getPos().subtract(Vec3d.ofBottomCenter(pos));
        away = new Vec3d(away.x, 0, away.z);
        if (away.lengthSquared() < 1.0e-4) {
            away = this.getVelocity().multiply(-1, 0, -1);
        }
        if (away.lengthSquared() < 1.0e-4) {
            away = new Vec3d(1, 0, 0);
        }
        away = away.normalize().multiply(0.7);
        this.setVelocity(away.x, 0.3, away.z);
        this.velocityModified = true;
        if (repelCooldown <= 0 && this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.WHITE_ASH, getX(), getY() + 0.3, getZ(), 15, 0.3, 0.2, 0.3, 0.02);
            sw.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_GENERIC_BURN, SoundCategory.HOSTILE, 0.5f, 1.6f);
            repelCooldown = 10;
        }
    }

    /** Destroyed when its anchor (grave or cursed object) is destroyed. */
    public void banish() {
        if (this.isRemoved()) {
            return;
        }
        if (this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.FLAME, getX(), getBodyY(0.5), getZ(), 60, 0.4, 0.9, 0.4, 0.05);
            sw.spawnParticles(ParticleTypes.LARGE_SMOKE, getX(), getBodyY(0.5), getZ(), 30, 0.4, 0.9, 0.4, 0.03);
            sw.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_GHAST_HURT, SoundCategory.HOSTILE, 1.0f, 0.6f);
        }
        StoryManager.onResolved(this);
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (hintCooldown > 0) {
            hintCooldown--;
        }
        if (repelCooldown > 0) {
            repelCooldown--;
        }
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason reason) {
        if (reason == SpawnReason.NATURAL || reason == SpawnReason.CHUNK_GENERATION) {
            if (!(world instanceof ServerWorldAccess serverWorld)) {
                return false;
            }
            if (world.getDifficulty() == Difficulty.PEACEFUL) {
                return false;
            }
            BlockPos pos = this.getBlockPos();
            if (!world.getFluidState(pos).isEmpty()) {
                return false;
            }
            if (!world.getBlockState(pos.down()).isSolidBlock(world, pos.down())) {
                return false;
            }
            if (!HostileEntity.isSpawnDark(serverWorld, pos, this.getRandom())) {
                return false;
            }
        }
        return super.canSpawn(world, reason);
    }
}

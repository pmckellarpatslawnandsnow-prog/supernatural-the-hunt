package com.fanmods.snhunt.entity;

import com.fanmods.snhunt.story.StoryManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Season 1 boss. Throws hunters around with its mind. Ordinary weapons can't push it below half health —
 * only the Colt can wound it badly enough to make it flee.
 */
public class GreaterDemonEntity extends HuntMonsterEntity implements HolyWaterVulnerable {
    private final ServerBossBar bossBar = new ServerBossBar(this.getDisplayName(), BossBar.Color.RED, BossBar.Style.NOTCHED_10);
    private boolean coltWounded;
    private boolean fled;
    private int throwCooldown = 80;
    private int trapStrain;

    public GreaterDemonEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 100;
    }

    public static DefaultAttributeContainer.Builder createGreaterDemonAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 220.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 11.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.8)
                .add(EntityAttributes.GENERIC_ARMOR, 6.0);
    }

    private float halfHealth() {
        return this.getMaxHealth() * 0.5f;
    }

    private float fleeHealth() {
        return this.getMaxHealth() * 0.2f;
    }

    @Override
    protected float adjustDamage(DamageSource source, float amount) {
        float adjusted = amount * 0.5f;
        float after = this.getHealth() - adjusted;
        if (!coltWounded && after < halfHealth()) {
            adjusted = Math.max(0.0f, this.getHealth() - halfHealth());
            if (source.getAttacker() instanceof PlayerEntity player) {
                showHint(player);
            }
        } else if (after <= fleeHealth()) {
            flee();
            return 0.0f;
        }
        return adjusted;
    }

    @Override
    protected Text immuneHint() {
        return Text.translatable("hint.snhunt.greater_demon");
    }

    @Override
    public void onColtHit(PlayerEntity shooter) {
        coltWounded = true;
        if (this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.LAVA, getX(), getBodyY(0.6), getZ(), 20, 0.3, 0.5, 0.3, 0.1);
            sw.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_WITHER_HURT, SoundCategory.HOSTILE, 1.5f, 0.5f);
        }
        shooter.sendMessage(Text.translatable("message.snhunt.colt_wounds").formatted(Formatting.GOLD), true);
        if (this.getHealth() - 60.0f <= fleeHealth()) {
            flee();
        } else {
            forceDamage(this.getDamageSources().playerAttack(shooter), 60.0f);
        }
    }

    @Override
    public void onSaltHit(PlayerEntity shooter) {
        showHint(shooter);
    }

    @Override
    public void onHolyWater(PlayerEntity thrower) {
        if (this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.LARGE_SMOKE, getX(), getBodyY(0.6), getZ(), 15, 0.3, 0.4, 0.3, 0.02);
            sw.playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.HOSTILE, 1.0f, 0.5f);
        }
        this.damage(this.getDamageSources().indirectMagic(thrower, thrower), 8.0f);
    }

    /** Devil's traps only hold it for a moment before it breaks the paint. */
    public void strainAgainstTrap(World world, BlockPos pos) {
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 10, 4, false, false));
        if (++trapStrain > 50) {
            trapStrain = 0;
            world.breakBlock(pos, false);
            world.playSound(null, pos, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, SoundCategory.HOSTILE, 1.0f, 0.6f);
        }
    }

    /** Smoke out of the host and escape. Counts as beating the boss. */
    public void flee() {
        if (fled || !(this.getWorld() instanceof ServerWorld sw)) {
            return;
        }
        fled = true;
        for (int i = 0; i < 10; i++) {
            sw.spawnParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.5 * i, getZ(), 25, 0.3, 0.3, 0.3, 0.08);
        }
        sw.spawnParticles(ParticleTypes.SQUID_INK, getX(), getBodyY(0.8), getZ(), 80, 0.4, 2.0, 0.4, 0.2);
        sw.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_WITHER_DEATH, SoundCategory.HOSTILE, 1.0f, 0.7f);
        for (ServerPlayerEntity player : sw.getPlayers(p -> p.squaredDistanceTo(this) < 64 * 64)) {
            player.sendMessage(Text.translatable("message.snhunt.demon_fled").formatted(Formatting.DARK_RED, Formatting.BOLD), false);
        }
        bossBar.clearPlayers();
        StoryManager.onResolved(this);
        this.discard();
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        bossBar.setPercent(this.getHealth() / this.getMaxHealth());
        if (throwCooldown > 0) {
            throwCooldown--;
            return;
        }
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && this.squaredDistanceTo(target) < 12 * 12 && this.canSee(target)) {
            // Telekinetic throw.
            Vec3d dir = target.getPos().subtract(this.getPos());
            dir = new Vec3d(dir.x, 0, dir.z);
            dir = dir.lengthSquared() < 1.0e-4 ? new Vec3d(1, 0, 0) : dir.normalize();
            target.setVelocity(dir.x * 1.8, 0.75, dir.z * 1.8);
            target.velocityModified = true;
            target.damage(this.getDamageSources().indirectMagic(this, this), 4.0f);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 2));
            if (this.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getBodyY(0.5), target.getZ(), 30, 0.4, 0.6, 0.4, 0.1);
                sw.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.HOSTILE, 1.0f, 0.6f);
            }
            throwCooldown = 120 + this.random.nextInt(60);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient && this.random.nextInt(2) == 0) {
            this.getWorld().addParticle(ParticleTypes.SMOKE, getParticleX(0.6), getRandomBodyY(), getParticleZ(0.6), 0, 0.02, 0);
        }
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        bossBar.removePlayer(player);
    }

    @Override
    public void setCustomName(@Nullable Text name) {
        super.setCustomName(name);
        bossBar.setName(this.getDisplayName());
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("ColtWounded", coltWounded);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        coltWounded = nbt.getBoolean("ColtWounded");
        if (this.hasCustomName()) {
            bossBar.setName(this.getDisplayName());
        }
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_WITHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WITHER_DEATH;
    }
}

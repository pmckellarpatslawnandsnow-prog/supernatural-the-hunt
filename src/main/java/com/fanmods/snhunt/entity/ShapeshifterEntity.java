package com.fanmods.snhunt.entity;

import com.fanmods.snhunt.item.HunterUtil;
import com.fanmods.snhunt.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/** Wears other people's faces. Only silver really hurts it. Sheds its skin (leather) when it dies. */
public class ShapeshifterEntity extends HuntMonsterEntity {
    public ShapeshifterEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createShapeshifterAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    @Override
    protected float adjustDamage(DamageSource source, float amount) {
        ItemStack weapon = HunterUtil.meleeWeapon(source);
        if (weapon.isOf(ModItems.SILVER_KNIFE)) {
            return amount * 3.0f;
        }
        if (source.getAttacker() instanceof net.minecraft.entity.player.PlayerEntity player) {
            showHint(player);
        }
        return amount * 0.3f;
    }

    @Override
    protected Text immuneHint() {
        return Text.translatable("hint.snhunt.shapeshifter");
    }

    @Override
    protected void dropLoot(DamageSource damageSource, boolean causedByPlayer) {
        super.dropLoot(damageSource, causedByPlayer);
        this.dropStack(new ItemStack(Items.LEATHER, 1 + this.random.nextInt(2)));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ZOMBIE_VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIE_VILLAGER_DEATH;
    }
}

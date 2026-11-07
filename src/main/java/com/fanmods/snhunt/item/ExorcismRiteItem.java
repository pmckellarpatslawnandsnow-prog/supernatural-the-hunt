package com.fanmods.snhunt.item;

import com.fanmods.snhunt.entity.DemonEntity;
import com.fanmods.snhunt.entity.GreaterDemonEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/** Read aloud near a demon held in a Devil's Trap to cast it out. */
public class ExorcismRiteItem extends Item {
    public ExorcismRiteItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            ServerWorld sw = (ServerWorld) world;
            sw.spawnParticles(ParticleTypes.ENCHANT, user.getX(), user.getY() + 1.2, user.getZ(), 40, 0.6, 0.6, 0.6, 0.5);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON,
                    SoundCategory.PLAYERS, 0.9f, 0.8f);

            int cast = 0;
            int resisting = 0;
            for (DemonEntity demon : world.getEntitiesByClass(DemonEntity.class, user.getBoundingBox().expand(12), Entity::isAlive)) {
                if (demon.isTrapped()) {
                    demon.exorcise(user);
                    cast++;
                } else {
                    demon.onRiteHeard(user);
                    resisting++;
                }
            }
            boolean greater = !world.getEntitiesByClass(GreaterDemonEntity.class, user.getBoundingBox().expand(12), Entity::isAlive).isEmpty();

            String key;
            if (cast > 0) {
                key = "message.snhunt.rite_success";
            } else if (greater) {
                key = "message.snhunt.rite_greater";
            } else if (resisting > 0) {
                key = "message.snhunt.rite_untrapped";
            } else {
                key = "message.snhunt.rite_nothing";
            }
            user.sendMessage(Text.translatable(key).formatted(cast > 0 ? Formatting.GOLD : Formatting.GRAY), true);
        }
        user.getItemCooldownManager().set(this, 40);
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        HunterUtil.tooltip(this, tooltip);
    }
}

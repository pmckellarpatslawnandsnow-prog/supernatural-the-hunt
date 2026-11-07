package com.fanmods.snhunt.item;

import com.fanmods.snhunt.entity.VampireEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

import java.util.List;

/** Right-click a vampire to poison it: no healing, weak and slow for 30 seconds. */
public class DeadMansBloodItem extends Item {
    public DeadMansBloodItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof VampireEntity vampire)) {
            if (!user.getWorld().isClient) {
                user.sendMessage(Text.translatable("message.snhunt.blood_wasted").formatted(Formatting.GRAY), true);
            }
            return ActionResult.PASS;
        }
        if (!user.getWorld().isClient) {
            vampire.poison();
            stack.decrementUnlessCreative(1, user);
            user.sendMessage(Text.translatable("message.snhunt.blood_poisoned").formatted(Formatting.DARK_RED), true);
        }
        return ActionResult.success(user.getWorld().isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        HunterUtil.tooltip(this, tooltip);
    }
}

package com.fanmods.snhunt.item;

import com.fanmods.snhunt.entity.SpiritEntity;
import com.fanmods.snhunt.story.StoryManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/** Hold it to sense spirits (and your story grave or cursed object) within 48 blocks. */
public class EmfMeterItem extends Item {
    private static final Formatting[] COLORS = {Formatting.GREEN, Formatting.GREEN, Formatting.YELLOW, Formatting.GOLD, Formatting.RED};

    public EmfMeterItem(Settings settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient || !(entity instanceof ServerPlayerEntity player)) {
            return;
        }
        boolean held = selected || player.getOffHandStack() == stack;
        if (!held || world.getTime() % 10 != 0) {
            return;
        }
        double best = Double.MAX_VALUE;
        for (SpiritEntity spirit : world.getEntitiesByClass(SpiritEntity.class, player.getBoundingBox().expand(48), Entity::isAlive)) {
            best = Math.min(best, spirit.distanceTo(player));
        }
        BlockPos anchor = StoryManager.anchorFor(player);
        if (anchor != null) {
            best = Math.min(best, Math.sqrt(player.squaredDistanceTo(Vec3d.ofCenter(anchor))));
        }
        int level = best < 8 ? 5 : best < 16 ? 4 : best < 24 ? 3 : best < 36 ? 2 : best < 48 ? 1 : 0;

        MutableText bar = Text.literal("EMF ").formatted(Formatting.GRAY);
        for (int i = 0; i < 5; i++) {
            bar.append(Text.literal(i < level ? "▮" : "▯").formatted(i < level ? COLORS[i] : Formatting.DARK_GRAY));
        }
        bar.append(Text.literal(level == 0 ? "  quiet" : level >= 4 ? "  IT'S CLOSE" : "  activity").formatted(Formatting.GRAY));
        player.sendMessage(bar, true);
        if (level > 0) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_COMPARATOR_CLICK,
                    SoundCategory.PLAYERS, 0.3f + level * 0.1f, 0.6f + level * 0.25f);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        HunterUtil.tooltip(this, tooltip);
    }
}

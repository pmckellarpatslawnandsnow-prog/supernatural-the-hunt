package com.fanmods.snhunt.item;

import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

/** Silver knife and machete. Their special effects live in the monsters' damage rules. */
public class HunterBladeItem extends SwordItem {
    public HunterBladeItem(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        HunterUtil.tooltip(this, tooltip);
    }
}

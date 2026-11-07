package com.fanmods.snhunt.registry;

import com.fanmods.snhunt.SNHunt;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ModItemGroup {
    public static final ItemGroup HUNTERS = Registry.register(Registries.ITEM_GROUP, SNHunt.id("hunters"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.HUNTERS_JOURNAL))
                    .displayName(Text.translatable("itemGroup.snhunt.hunters"))
                    .entries((context, entries) -> {
                        for (Item item : ModItems.ALL) {
                            entries.add(item);
                        }
                    })
                    .build());

    public static void init() {
    }

    private ModItemGroup() {
    }
}

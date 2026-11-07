package com.fanmods.snhunt.registry;

import com.fanmods.snhunt.SNHunt;
import com.fanmods.snhunt.item.ColtItem;
import com.fanmods.snhunt.item.DeadMansBloodItem;
import com.fanmods.snhunt.item.EmfMeterItem;
import com.fanmods.snhunt.item.ExorcismRiteItem;
import com.fanmods.snhunt.item.FlareGunItem;
import com.fanmods.snhunt.item.HolyWaterItem;
import com.fanmods.snhunt.item.HunterBladeItem;
import com.fanmods.snhunt.item.HuntersJournalItem;
import com.fanmods.snhunt.item.SaltShotgunItem;
import com.fanmods.snhunt.item.TipBlockItem;
import com.fanmods.snhunt.item.TipItem;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

import java.util.ArrayList;
import java.util.List;

public final class ModItems {
    /** Every item, in creative-tab order. */
    public static final List<Item> ALL = new ArrayList<>();

    // Hunter gear
    public static final Item HUNTERS_JOURNAL = register("hunters_journal", new HuntersJournalItem(new Item.Settings().maxCount(1)));
    public static final Item SALT = register("salt", new TipBlockItem(ModBlocks.SALT_LINE, new Item.Settings()));
    public static final Item SALT_SHOTGUN = register("salt_shotgun", new SaltShotgunItem(new Item.Settings().maxDamage(250)));
    public static final Item EMF_METER = register("emf_meter", new EmfMeterItem(new Item.Settings().maxCount(1)));
    public static final Item FLARE_GUN = register("flare_gun", new FlareGunItem(new Item.Settings().maxDamage(40)));
    public static final Item HOLY_WATER = register("holy_water", new HolyWaterItem(new Item.Settings().maxCount(16)));
    public static final Item EXORCISM_RITE = register("exorcism_rite", new ExorcismRiteItem(new Item.Settings().maxCount(1)));
    public static final Item DEVILS_TRAP = register("devils_trap", new TipBlockItem(ModBlocks.DEVILS_TRAP, new Item.Settings()));
    public static final Item SILVER_KNIFE = register("silver_knife", new HunterBladeItem(ToolMaterials.IRON,
            new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 1, -1.8f))));
    public static final Item MACHETE = register("machete", new HunterBladeItem(ToolMaterials.IRON,
            new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 4, -2.6f))));
    public static final Item DEAD_MANS_BLOOD = register("dead_mans_blood", new DeadMansBloodItem(new Item.Settings().maxCount(16)));
    public static final Item THE_COLT = register("the_colt", new ColtItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC).fireproof()));
    public static final Item COLT_ROUND = register("colt_round", new TipItem(new Item.Settings().rarity(Rarity.RARE)));

    // Story blocks (creative only)
    public static final Item GRAVE = register("grave", new TipBlockItem(ModBlocks.GRAVE, new Item.Settings()));
    public static final Item CURSED_OBJECT = register("cursed_object", new TipBlockItem(ModBlocks.CURSED_OBJECT, new Item.Settings()));

    // Spawn eggs
    public static final Item SPIRIT_SPAWN_EGG = register("spirit_spawn_egg", new SpawnEggItem(ModEntities.SPIRIT, 0xDCE6F2, 0x6F8299, new Item.Settings()));
    public static final Item WENDIGO_SPAWN_EGG = register("wendigo_spawn_egg", new SpawnEggItem(ModEntities.WENDIGO, 0x6B5E52, 0xC9D3D8, new Item.Settings()));
    public static final Item SHAPESHIFTER_SPAWN_EGG = register("shapeshifter_spawn_egg", new SpawnEggItem(ModEntities.SHAPESHIFTER, 0xC79A7E, 0x8A2E2E, new Item.Settings()));
    public static final Item DEMON_SPAWN_EGG = register("demon_spawn_egg", new SpawnEggItem(ModEntities.DEMON, 0x2B2B2B, 0x000000, new Item.Settings()));
    public static final Item VAMPIRE_SPAWN_EGG = register("vampire_spawn_egg", new SpawnEggItem(ModEntities.VAMPIRE, 0xE8E0D8, 0x7A0F12, new Item.Settings()));
    public static final Item SHTRIGA_SPAWN_EGG = register("shtriga_spawn_egg", new SpawnEggItem(ModEntities.SHTRIGA, 0x55584E, 0xC8D64A, new Item.Settings()));
    public static final Item DAEVA_SPAWN_EGG = register("daeva_spawn_egg", new SpawnEggItem(ModEntities.DAEVA, 0x0B0B0F, 0xC81E1E, new Item.Settings()));
    public static final Item SCARECROW_SPAWN_EGG = register("scarecrow_spawn_egg", new SpawnEggItem(ModEntities.SCARECROW, 0xC2A060, 0x9A2A2A, new Item.Settings()));
    public static final Item GREATER_DEMON_SPAWN_EGG = register("greater_demon_spawn_egg", new SpawnEggItem(ModEntities.GREATER_DEMON, 0x161616, 0xF08A1C, new Item.Settings()));

    private static Item register(String name, Item item) {
        Item registered = Registry.register(Registries.ITEM, SNHunt.id(name), item);
        ALL.add(registered);
        return registered;
    }

    public static void init() {
    }

    private ModItems() {
    }
}

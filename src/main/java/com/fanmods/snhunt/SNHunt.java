package com.fanmods.snhunt;

import com.fanmods.snhunt.registry.ModBlocks;
import com.fanmods.snhunt.registry.ModEntities;
import com.fanmods.snhunt.registry.ModItemGroup;
import com.fanmods.snhunt.registry.ModItems;
import com.fanmods.snhunt.story.StoryManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supernatural: The Hunt — an unofficial fan mod.
 */
public class SNHunt implements ModInitializer {
    public static final String MOD_ID = "snhunt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModBlocks.init();
        ModEntities.init();
        ModItems.init();
        ModItemGroup.init();
        StoryManager.init();
        LOGGER.info("Supernatural: The Hunt loaded. Saving people, one block at a time.");
    }
}

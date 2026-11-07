package com.fanmods.snhunt.registry;

import com.fanmods.snhunt.SNHunt;
import com.fanmods.snhunt.block.CursedObjectBlock;
import com.fanmods.snhunt.block.DevilsTrapBlock;
import com.fanmods.snhunt.block.GraveBlock;
import com.fanmods.snhunt.block.SaltLineBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public final class ModBlocks {
    public static final Block SALT_LINE = register("salt_line", new SaltLineBlock(AbstractBlock.Settings.create()
            .noCollision().nonOpaque().strength(0.1f).sounds(BlockSoundGroup.SAND)
            .pistonBehavior(PistonBehavior.DESTROY)));

    public static final Block DEVILS_TRAP = register("devils_trap", new DevilsTrapBlock(AbstractBlock.Settings.create()
            .noCollision().nonOpaque().strength(0.3f).sounds(BlockSoundGroup.WOOL)
            .pistonBehavior(PistonBehavior.DESTROY)));

    public static final Block GRAVE = register("grave", new GraveBlock(AbstractBlock.Settings.create()
            .strength(2.0f, 6.0f).sounds(BlockSoundGroup.GRAVEL)));

    public static final Block CURSED_OBJECT = register("cursed_object", new CursedObjectBlock(AbstractBlock.Settings.create()
            .strength(3.0f, 6.0f).sounds(BlockSoundGroup.STONE).luminance(state -> 5)));

    private static Block register(String name, Block block) {
        return Registry.register(Registries.BLOCK, SNHunt.id(name), block);
    }

    public static void init() {
    }

    private ModBlocks() {
    }
}

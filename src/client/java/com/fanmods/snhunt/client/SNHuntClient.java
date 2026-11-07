package com.fanmods.snhunt.client;

import com.fanmods.snhunt.registry.ModBlocks;
import com.fanmods.snhunt.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;

public class SNHuntClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        //                                                             texture          arms   scale  ghost  eyes
        EntityRendererRegistry.register(ModEntities.SPIRIT, ctx -> new HuntMonsterRenderer<>(ctx, "spirit", true, 1.0f, true, null));
        EntityRendererRegistry.register(ModEntities.WENDIGO, ctx -> new HuntMonsterRenderer<>(ctx, "wendigo", true, 1.3f, false, "wendigo_eyes"));
        EntityRendererRegistry.register(ModEntities.SHAPESHIFTER, ctx -> new HuntMonsterRenderer<>(ctx, "shapeshifter", false, 1.0f, false, null));
        EntityRendererRegistry.register(ModEntities.DEMON, ctx -> new HuntMonsterRenderer<>(ctx, "demon", false, 1.0f, false, null));
        EntityRendererRegistry.register(ModEntities.VAMPIRE, ctx -> new HuntMonsterRenderer<>(ctx, "vampire", false, 1.0f, false, null));
        EntityRendererRegistry.register(ModEntities.SHTRIGA, ctx -> new HuntMonsterRenderer<>(ctx, "shtriga", true, 1.0f, false, "shtriga_eyes"));
        EntityRendererRegistry.register(ModEntities.DAEVA, ctx -> new HuntMonsterRenderer<>(ctx, "daeva", true, 1.0f, true, "daeva_eyes"));
        EntityRendererRegistry.register(ModEntities.SCARECROW, ctx -> new HuntMonsterRenderer<>(ctx, "scarecrow", false, 1.12f, false, "scarecrow_eyes"));
        EntityRendererRegistry.register(ModEntities.GREATER_DEMON, ctx -> new HuntMonsterRenderer<>(ctx, "greater_demon", false, 1.1f, false, "greater_demon_eyes"));

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), ModBlocks.SALT_LINE, ModBlocks.DEVILS_TRAP);
    }
}

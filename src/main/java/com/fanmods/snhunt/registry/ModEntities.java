package com.fanmods.snhunt.registry;

import com.fanmods.snhunt.SNHunt;
import com.fanmods.snhunt.entity.DaevaEntity;
import com.fanmods.snhunt.entity.DemonEntity;
import com.fanmods.snhunt.entity.GreaterDemonEntity;
import com.fanmods.snhunt.entity.ScarecrowEntity;
import com.fanmods.snhunt.entity.ShapeshifterEntity;
import com.fanmods.snhunt.entity.ShtrigaEntity;
import com.fanmods.snhunt.entity.SpiritEntity;
import com.fanmods.snhunt.entity.VampireEntity;
import com.fanmods.snhunt.entity.WendigoEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.biome.BiomeKeys;

public final class ModEntities {
    public static final EntityType<SpiritEntity> SPIRIT = register("spirit",
            EntityType.Builder.create(SpiritEntity::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.95f).maxTrackingRange(8));
    public static final EntityType<WendigoEntity> WENDIGO = register("wendigo",
            EntityType.Builder.create(WendigoEntity::new, SpawnGroup.MONSTER).dimensions(0.8f, 2.55f).maxTrackingRange(10));
    public static final EntityType<ShapeshifterEntity> SHAPESHIFTER = register("shapeshifter",
            EntityType.Builder.create(ShapeshifterEntity::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.95f).maxTrackingRange(8));
    public static final EntityType<DemonEntity> DEMON = register("demon",
            EntityType.Builder.create(DemonEntity::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.95f).maxTrackingRange(8));
    public static final EntityType<VampireEntity> VAMPIRE = register("vampire",
            EntityType.Builder.create(VampireEntity::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.95f).maxTrackingRange(8));
    public static final EntityType<ShtrigaEntity> SHTRIGA = register("shtriga",
            EntityType.Builder.create(ShtrigaEntity::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.95f).maxTrackingRange(8));
    public static final EntityType<DaevaEntity> DAEVA = register("daeva",
            EntityType.Builder.create(DaevaEntity::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.95f).maxTrackingRange(8));
    public static final EntityType<ScarecrowEntity> SCARECROW = register("scarecrow",
            EntityType.Builder.create(ScarecrowEntity::new, SpawnGroup.MONSTER).dimensions(0.7f, 2.2f).maxTrackingRange(8));
    public static final EntityType<GreaterDemonEntity> GREATER_DEMON = register("greater_demon",
            EntityType.Builder.create(GreaterDemonEntity::new, SpawnGroup.MONSTER).dimensions(0.66f, 2.15f)
                    .makeFireImmune().maxTrackingRange(12));

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        return Registry.register(Registries.ENTITY_TYPE, SNHunt.id(name), builder.build(name));
    }

    public static void init() {
        FabricDefaultAttributeRegistry.register(SPIRIT, SpiritEntity.createSpiritAttributes());
        FabricDefaultAttributeRegistry.register(WENDIGO, WendigoEntity.createWendigoAttributes());
        FabricDefaultAttributeRegistry.register(SHAPESHIFTER, ShapeshifterEntity.createShapeshifterAttributes());
        FabricDefaultAttributeRegistry.register(DEMON, DemonEntity.createDemonAttributes());
        FabricDefaultAttributeRegistry.register(VAMPIRE, VampireEntity.createVampireAttributes());
        FabricDefaultAttributeRegistry.register(SHTRIGA, ShtrigaEntity.createShtrigaAttributes());
        FabricDefaultAttributeRegistry.register(DAEVA, DaevaEntity.createDaevaAttributes());
        FabricDefaultAttributeRegistry.register(SCARECROW, ScarecrowEntity.createScarecrowAttributes());
        FabricDefaultAttributeRegistry.register(GREATER_DEMON, GreaterDemonEntity.createGreaterDemonAttributes());

        // Free-roam spawning (night / darkness only; see HuntMonsterEntity#canSpawn).
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, SPIRIT, 8, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_TAIGA), SpawnGroup.MONSTER, WENDIGO, 12, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, SHAPESHIFTER, 4, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, DEMON, 3, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, VAMPIRE, 4, 1, 3);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, DAEVA, 3, 1, 2);
        BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_FOREST), SpawnGroup.MONSTER, SHTRIGA, 2, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.PLAINS, BiomeKeys.SUNFLOWER_PLAINS),
                SpawnGroup.MONSTER, SCARECROW, 5, 1, 1);
    }

    private ModEntities() {
    }
}

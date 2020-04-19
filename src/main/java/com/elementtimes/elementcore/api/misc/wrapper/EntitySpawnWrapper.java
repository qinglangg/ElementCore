package com.elementtimes.elementcore.api.misc.wrapper;

import com.elementtimes.elementcore.api.ECModElements;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 用于注册实体生成
 * @author luqin2007
 */
public class EntitySpawnWrapper {

    private final Biome.SpawnListEntry mSpawnListEntry;
    private final Supplier<Optional<Biome>> mBiome;
    private final EntityClassification mClassification;

    public EntitySpawnWrapper(Biome.SpawnListEntry entry, Supplier<Optional<Biome>> biome, EntityClassification classification) {
        mSpawnListEntry = entry;
        mBiome = biome;
        mClassification = classification;
    }

    public void apply(Logger logger) {
        mBiome.get().ifPresent(biome -> {
            logger.warn("    {} at {}, count: {}-{}, weight: {}", mSpawnListEntry.entityType.getName(), biome, mSpawnListEntry.minGroupCount, mSpawnListEntry.maxGroupCount, mSpawnListEntry.itemWeight);
            biome.getSpawns(mClassification).add(mSpawnListEntry);
        });
    }

    public EntityClassification getClassification() {
        return mClassification;
    }

    public Biome.SpawnListEntry getSpawnListEntry() {
        return mSpawnListEntry;
    }

    public static void registerAll(ECModElements elements) {
        List<EntitySpawnWrapper> spawns = elements.entitySpawns;
        elements.warn("  Entity Spawn({})", spawns.size());
        spawns.forEach(e -> e.apply(elements));
    }
}

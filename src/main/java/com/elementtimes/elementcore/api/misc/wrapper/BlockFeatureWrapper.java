package com.elementtimes.elementcore.api.misc.wrapper;

import com.elementtimes.elementcore.api.ECModElements;
import net.minecraft.block.Block;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockFeatureWrapper {

    private final Function<Block, Optional<Biome>> mBiome;
    private final Function<Block, ConfiguredFeature> mFeature;
    private final Supplier<Collection<Block>> mBlocks;
    private final GenerationStage.Decoration mDecoration;

    public BlockFeatureWrapper(GenerationStage.Decoration decoration, Function<Block, Optional<Biome>> biome, Function<Block, ConfiguredFeature> feature, Supplier<Collection<Block>> blocks) {
        mBiome = biome;
        mFeature = feature;
        mBlocks = blocks;
        mDecoration = decoration;
    }

    public void apply(Logger logger) {
        mBlocks.get().forEach(block -> {
            mBiome.apply(block).ifPresent(biome -> {
                logger.warn("    biome = {}, block = {}", biome, block);
                biome.addFeature(mDecoration, mFeature.apply(block));
            });
        });
        logger.warn("    ==========");
    }

    public static void registerAll(ECModElements elements) {
        List<BlockFeatureWrapper> features = elements.features;
        elements.warn("  Register Feature({} groups)", features.size());
        features.forEach(f -> f.apply(elements));
    }
}

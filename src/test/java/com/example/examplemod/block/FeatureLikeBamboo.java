package com.example.examplemod.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.BambooFeature;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraft.world.gen.placement.HeightWithChanceConfig;
import net.minecraft.world.gen.placement.Placement;

import java.util.Random;

public class FeatureLikeBamboo extends BambooFeature {

    private BlockState state;

    public FeatureLikeBamboo() {
        this(TestModBlockFeature.blockFeature0.getDefaultState());
    }

    public FeatureLikeBamboo(BlockState state) {
        super(ProbabilityConfig::deserialize);
        this.state = state;
    }

    @Override
    public boolean place(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, ProbabilityConfig config) {
        int i = 0;
        BlockPos.MutableBlockPos pos0 = new BlockPos.MutableBlockPos(pos);
        BlockPos.MutableBlockPos pos1 = new BlockPos.MutableBlockPos(pos);
        if (worldIn.isAirBlock(pos0)) {
            if (state.isValidPosition(worldIn, pos0)) {
                int j = rand.nextInt(12) + 5;
                if (rand.nextFloat() < config.probability) {
                    int k = rand.nextInt(4) + 1;

                    for(int l = pos.getX() - k; l <= pos.getX() + k; ++l) {
                        for(int i1 = pos.getZ() - k; i1 <= pos.getZ() + k; ++i1) {
                            int j1 = l - pos.getX();
                            int k1 = i1 - pos.getZ();
                            if (j1 * j1 + k1 * k1 <= k * k) {
                                pos1.setPos(l, worldIn.getHeight(Heightmap.Type.WORLD_SURFACE, l, i1) - 1, i1);
                                if (worldIn.getBlockState(pos1).getBlock().isIn(BlockTags.DIRT_LIKE)) {
                                    worldIn.setBlockState(pos1, net.minecraft.block.Blocks.PODZOL.getDefaultState(), 2);
                                }
                            }
                        }
                    }
                }

                for(int l1 = 0; l1 < j && worldIn.isAirBlock(pos0); ++l1) {
                    worldIn.setBlockState(pos0, state, 2);
                    pos0.move(Direction.UP, 1);
                }

                if (pos0.getY() - pos.getY() >= 3) {
                    worldIn.setBlockState(pos0, state, 2);
                    worldIn.setBlockState(pos0.move(Direction.DOWN, 1), state, 2);
                    worldIn.setBlockState(pos0.move(Direction.DOWN, 1), state, 2);
                }
            }

            ++i;
        }

        return i > 0;
    }

    public static ConfiguredFeature<?> toFeature(Block block) {
        return Biome.createDecoratedFeature(new FeatureLikeBamboo(block.getDefaultState()), new ProbabilityConfig(.3f), Placement.COUNT_CHANCE_HEIGHTMAP, new HeightWithChanceConfig(10, .3f));
    }

    public static ProbabilityConfig config(Block block, Feature<ProbabilityConfig> feature) {
        return new ProbabilityConfig(.3f);
    }

    public static HeightWithChanceConfig placement(Block block, Placement<HeightWithChanceConfig> placement) {
        return new HeightWithChanceConfig(10, .3f);
    }

    public static ConfiguredFeature<?> feature() {
        return Biome.createDecoratedFeature(new FeatureLikeBamboo(TestModBlockFeature.blockFeatureObject.getDefaultState()), new ProbabilityConfig(.3f), Placement.COUNT_CHANCE_HEIGHTMAP, new HeightWithChanceConfig(10, .3f));
    }
}

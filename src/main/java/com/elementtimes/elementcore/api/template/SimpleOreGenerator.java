package com.elementtimes.elementcore.api.template;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * 用于矿物生成
 * @author luqin2007
 */
public class SimpleOreGenerator extends WorldGenerator {
    private final int yRange;
    private final int yMin;
    private final int times;
    private final float probability;
    private final int[] dimBlackList;
    private final int[] dimWhiteList;

    private final WorldGenMinable mWorldGenerator;

    public SimpleOreGenerator(int yRange, int yMin, int count, int times, float probability, int[] dimBlackList, int[] dimWhiteList, IBlockState block) {
        this.yRange = yRange;
        this.yMin = yMin;
        this.times = times;
        this.probability = probability;
        this.dimBlackList = dimBlackList;
        this.dimWhiteList = dimWhiteList;
        this.mWorldGenerator = new WorldGenMinable(block, count);
    }

    @Override
    public boolean generate(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos position) {
        if (canGenerator(worldIn.provider.getDimension())) {
            for (int i = 0; i < times; i++) {
                int x = position.getX() + 8;
                int y = yMin + rand.nextInt(yRange);
                int z = position.getZ() + 8;
                // Fixme
                // TODO: 随机 x z 生成 ？？
//                int x = rand.nextInt(16) + position.getX() - 8;
//                int y = yMin + rand.nextInt(yRange);
//                int z = rand.nextInt(16) + position.getZ() - 8;
                if (rand.nextFloat() <= probability) {
                    mWorldGenerator.generate(worldIn, rand, new BlockPos(x, y, z));
                }
            }
        }
        return true;
    }

    private boolean canGenerator(int dimId) {
        boolean canGenerator = true;

        if (dimWhiteList.length > 0) {
            canGenerator = ArrayUtils.contains(dimWhiteList, dimId);
        }

        if (canGenerator && dimBlackList.length > 0) {
            canGenerator = !ArrayUtils.contains(dimBlackList, dimId);
        }

        return canGenerator;
    }
}

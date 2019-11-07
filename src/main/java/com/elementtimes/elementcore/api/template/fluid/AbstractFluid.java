package com.elementtimes.elementcore.api.template.fluid;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.Item;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;

/**
 * 原版流体封装
 * @author luqin2007
 */
public abstract class AbstractFluid extends FlowingFluid {

    AbstractFluid mFlowingFluid, mStillFluid;
    private FlowingFluidBlock mFluidBlock;
    private Item mFilledBucket;

    public AbstractFluid(@Nullable FlowingFluidBlock block, Item filledBucket) {
        mFluidBlock = block;
        mFilledBucket = filledBucket;
    }

    /**
     * 获取流动的流体
     * @return 流动中的流体
     */
    @Override
    @Nonnull
    public FlowingFluid getFlowingFluid() {
        return mFlowingFluid;
    }

    /**
     * 获取水源
     * @return 水源
     */
    @Override
    @Nonnull
    public FlowingFluid getStillFluid() {
        return mStillFluid;
    }

    /**
     * 判断流体是否可造成无限流体
     * @return 可否无限流体
     */
    @Override
    public boolean canSourcesMultiply() {
        return mMultiply;
    }

    /**
     * 在流体替换其他方块之前调用
     * 在 flowInto 方法中，方块状态 isAir = false 时调用
     * 默认 进行方块掉落
     * @param worldIn 所在世界
     * @param pos 所处位置
     * @param state 方块状态
     */
    @Override
    protected void beforeReplacingBlock(IWorld worldIn, @Nonnull BlockPos pos, BlockState state) {
        TileEntity tileentity = state.getBlock().hasTileEntity(state) ? worldIn.getTileEntity(pos) : null;
        Block.spawnDrops(state, worldIn.getWorld(), pos, tileentity);
    }

    /**
     * 梅格方块减少的流体高度
     * @param worldIn 所在世界
     * @return 减少状态
     */
    @Override
    protected int getLevelDecreasePerBlock(@Nonnull IWorldReader worldIn) {
        return 1;
    }

    /**
     * 流体渲染类型。
     * 一般选择 BlockRenderLayer.TRANSLUCENT 即可
     * @return BlockRenderLayer.TRANSLUCENT，透明
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    /**
     * 每 tick 播放的流体动画
     *  1/64 概率播放 mAnimalSound（默认 SoundEvents.BLOCK_WATER_AMBIENT）的流体
     *  1/10 概率播放 ParticleTypes.UNDERWATER 粒子效果
     * @param worldIn 所在世界
     * @param pos 所处位置
     * @param state 流体状态
     * @param random 随机数对象
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    protected void animateTick(World worldIn, BlockPos pos, IFluidState state, Random random) {
        if (!state.isSource() && !state.get(FALLING)) {
            if (random.nextInt(64) == 0) {
                worldIn.playSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D,
                        mAnimalSound, SoundCategory.BLOCKS, random.nextFloat() * 0.25F + 0.75F,
                        random.nextFloat() + 0.5F, false);
            }
        } else if (random.nextInt(10) == 0) {
            worldIn.addParticle(ParticleTypes.UNDERWATER,
                    (float) pos.getX() + random.nextFloat(),
                    (float) pos.getY() + random.nextFloat(),
                    (float) pos.getZ() + random.nextFloat(),
                    0.0D, 0.0D, 0.0D);
        }
    }

    /**
     * 待检查流体是否与当前流体等效
     * @param fluidIn 待检查流体
     * @return 是否等效
     */
    @Override
    public boolean isEquivalentTo(Fluid fluidIn) {
        return fluidIn == this || fluidIn == mFlowingFluid || fluidIn == mStillFluid;
    }

    /**
     * 获取填充满后的流体容器
     * 水为水桶，熔岩为熔岩桶
     * @return 满的流体
     */
    @Override
    @Nonnull
    public Item getFilledBucket() {
        return mFilledBucket;
    }

    /**
     * 获取爆炸阻力，水和熔岩都为 100
     * 爆炸时 使用 f -= (resistance + 0.3F) * 0.3F 运算
     * @return 爆炸阻力
     */
    @Override
    public float getExplosionResistance() {
        return mExplosionResistance;
    }

    /**
     * 获取对应流体方块状态
     * @param stateIn 流体状态
     * @return 流体方块状态
     */
    @Override
    @Nonnull
    protected BlockState getBlockState(@Nonnull IFluidState stateIn) {
        return mFluidBlock.getDefaultState().with(FlowingFluidBlock.LEVEL, getLevelFromState(stateIn));
    }

    /**
     * 未知
     * 仅在 canFlow 方法中调用，影响流体流动？
     * 水为比较方向+相同 Tag
     * @param stateIn 流体状态
     * @param worldIn 所在世界
     * @param posIn 所处位置
     * @param fluidIn 流体？
     * @param directionIn 方向？
     * @return 未知值
     */
    @Override
    protected boolean func_215665_a(@Nonnull IFluidState stateIn, @Nonnull IBlockReader worldIn,
                                    @Nonnull BlockPos posIn, @Nonnull Fluid fluidIn, @Nonnull Direction directionIn) {
        if (directionIn == Direction.DOWN) {
            if (isEquivalentTo(fluidIn)) {
                return true;
            }
            Collection<ResourceLocation> tags = FluidTags.getCollection().getOwningTags(getStillFluid());
            if (!tags.isEmpty()) {
                return tags.stream().anyMatch(tag -> fluidIn.isIn(Objects.requireNonNull(FluidTags.getCollection().get(tag))));
            }
        }
        return false;
    }

    /**
     * 未知
     * 直译 Tick 比率？
     * 感觉像流动速度
     * 水 5，熔岩 10/30
     * @param worldIn 所在世界
     * @return tick
     */
    @Override
    public int getTickRate(@Nonnull IWorldReader worldIn) {
        return 5;
    }

    /**
     * 未知
     * 直译斜坡寻找距离？
     * 水为 4，熔岩为 4 or 2
     * @param worldIn 所在世界
     * @return 距离
     */
    @Override
    public int getSlopeFindDistance(@Nonnull IWorldReader worldIn) {
        return mSlopeFindDistance;
    }

    /**
     * 未知
     * 可能是某种粒子有关
     * @return 未知
     */
    @Nullable
    @Override
    protected IParticleData getDripParticleData() {
        return ParticleTypes.DRIPPING_WATER;
    }

    // property

    private SoundEvent mAnimalSound = SoundEvents.BLOCK_WATER_AMBIENT;
    private boolean mMultiply = false;
    private int mSlopeFindDistance = 4;
    private float mExplosionResistance = 100;
    private int mMaxLevel = 8;
    private int mFilledBucketAmount = 1000;

    public int getFilledBucketAmount() {
        return mFilledBucketAmount;
    }

    public void setFilledBucketAmount(int filledBucketAmount) {
        mFilledBucketAmount = filledBucketAmount;
    }

    public void setFluidBlock(FlowingFluidBlock fluidBlock) {
        setFluidBlockInternal(fluidBlock);
        getOther().setFluidBlockInternal(fluidBlock);
    }

    private void setFluidBlockInternal(FlowingFluidBlock fluidBlock) {
        mFluidBlock = fluidBlock;
    }

    public SoundEvent getAnimalSound() {
        return mAnimalSound;
    }

    public void setAnimalSound(SoundEvent animalSound) {
        setAnimalSoundInternal(animalSound);
        getOther().setAnimalSoundInternal(animalSound);
    }

    private void setAnimalSoundInternal(SoundEvent animalSound) {
        mAnimalSound = animalSound;
    }

    public void setSourcesMultiply(boolean multiply) {
        setSourcesMultiplyInternal(multiply);
        getOther().setSourcesMultiplyInternal(multiply);
    }

    private void setSourcesMultiplyInternal(boolean multiply) {
        mMultiply = multiply;
    }

    public void setSlopeFindDistance(int distance) {
        setSlopeFindDistanceInternal(distance);
        getOther().setSlopeFindDistanceInternal(distance);
    }

    private void setSlopeFindDistanceInternal(int distance) {
        mSlopeFindDistance = distance;
    }

    public void setExplosionResistance(float explosionResistance) {
        setExplosionResistanceInternal(explosionResistance);
        getOther().setExplosionResistanceInternal(explosionResistance);
    }

    private void setExplosionResistanceInternal(float explosionResistance) {
        mExplosionResistance = explosionResistance;
    }

    public void setFilledBucket(Item filledBucket) {
        setFilledBucketInternal(filledBucket);
        getOther().setFilledBucketInternal(filledBucket);
    }

    private void setFilledBucketInternal(Item filledBucket) {
        mFilledBucket = filledBucket;
    }

    public int getMaxLevel() {
        return mMaxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        setMaxLevelInternal(maxLevel);
        getOther().setMaxLevelInternal(maxLevel);
    }

    private void setMaxLevelInternal(int maxLevel) {
        mMaxLevel = maxLevel;
    }

    abstract protected void setOther(AbstractFluid other);

    abstract protected AbstractFluid getOther();

    public static class Flowing extends AbstractFluid {

        public Flowing(FlowingFluidBlock block, Item filledBucket) {
            super(block, filledBucket);
            mFlowingFluid = this;
        }

        @Override
        protected void setOther(AbstractFluid other) {
            mStillFluid = other;
        }

        @Override
        protected AbstractFluid getOther() {
            return mStillFluid;
        }

        @Override
        public boolean isSource(@Nonnull IFluidState state) {
            return false;
        }

        @Override
        public int getLevel(@Nonnull IFluidState state) {
            return state.get(LEVEL_1_8);
        }
    }

    public static class Source extends AbstractFluid {

        public Source(FlowingFluidBlock block, Item filledBucket) {
            super(block, filledBucket);
            mStillFluid = this;
        }

        @Override
        protected void setOther(AbstractFluid other) {
            mFlowingFluid = other;
        }

        @Override
        protected AbstractFluid getOther() {
            return mFlowingFluid;
        }

        @Override
        public boolean isSource(@Nonnull IFluidState state) {
            return true;
        }

        @Override
        public int getLevel(@Nonnull IFluidState state) {
            return getMaxLevel();
        }
    }
}

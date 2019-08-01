package com.elementtimes.elementcore.fluid;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.item.Item;
import net.minecraft.particles.IParticleData;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * 流体
 * @author luqin2007
 */
public abstract class Fluid extends WaterFluid {

    private IParticleData mParticle, mDripParticle;
    private SoundEvent mSound;
    private boolean mCanSourcesMultiply;
    private FluidResult mModFluidResult;

    public Fluid(IParticleData particle, IParticleData dripParticle, SoundEvent sound, boolean canSourcesMultiply, FluidResult result) {
        mParticle = particle;
        mDripParticle = dripParticle;
        mSound = sound;
        mCanSourcesMultiply = canSourcesMultiply;
        mModFluidResult = result;
    }

    @Override
    public net.minecraft.fluid.Fluid getFlowingFluid() {
        return mModFluidResult.flowingFluid;
    }

    @Override
    public net.minecraft.fluid.Fluid getStillFluid() {
        return mModFluidResult.stillFluid;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public Item getFilledBucket() {
        return ForgeMod.getInstance().universalBucket;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(World worldIn, BlockPos pos, IFluidState state, Random random) {
        if (!state.isSource() && !state.get(FALLING)) {
            if (random.nextInt(64) == 0) {
                worldIn.playSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, mSound, SoundCategory.BLOCKS, random.nextFloat() * 0.25F + 0.75F, random.nextFloat() + 0.5F, false);
            }
        } else if (random.nextInt(10) == 0) {
            worldIn.addParticle(mParticle, (float)pos.getX() + random.nextFloat(), (float)pos.getY() + random.nextFloat(), (float)pos.getZ() + random.nextFloat(), 0.0D, 0.0D, 0.0D);
        }

    }

    @Nullable
    @Override
    public IParticleData getDripParticleData() {
        return mDripParticle;
    }

    @Override
    protected boolean canSourcesMultiply() {
        return mCanSourcesMultiply;
    }

    @Override
    public IFluidState getStillFluidState(boolean p_207204_1_) {
        return super.getStillFluidState(p_207204_1_);
    }

    @Override
    public BlockState getBlockState(IFluidState fluidState) {
        return mModFluidResult.block.getDefaultState().with(FlowingFluidBlock.LEVEL, getLevelFromState(fluidState));
    }

    @Override
    public boolean isEquivalentTo(net.minecraft.fluid.Fluid fluid) {
        return fluid == mModFluidResult.flowingFluid || fluid == mModFluidResult.stillFluid;
    }

    public static class Flowing extends Fluid {

        public Flowing(IParticleData particle, IParticleData dripParticle, SoundEvent sound, boolean canSourcesMultiply, FluidResult result) {
            super(particle, dripParticle, sound, canSourcesMultiply, result);
        }

        @Override
        protected void fillStateContainer(StateContainer.Builder<net.minecraft.fluid.Fluid, IFluidState> builder) {
            super.fillStateContainer(builder);
            builder.add(LEVEL_1_8);
        }

        @Override
        public int getLevel(IFluidState fluidState) {
            return fluidState.get(LEVEL_1_8);
        }

        @Override
        public boolean isSource(IFluidState state) {
            return false;
        }
    }

    public static class Source extends Fluid {

        public Source(IParticleData particle, IParticleData dripParticle, SoundEvent sound, boolean canSourcesMultiply, FluidResult result) {
            super(particle, dripParticle, sound, canSourcesMultiply, result);
        }

        @Override
        public int getLevel(IFluidState fluidState) {
            return 8;
        }

        @Override
        public boolean isSource(IFluidState state) {
            return true;
        }
    }

    public static class FluidResult {
        public FlowingFluid flowingFluid = null;
        public FlowingFluid stillFluid = null;
        public Block block = null;
    }

    public static FluidResult create(
            // Block
            Material material, int light,
            // Fluid
            IParticleData particle, IParticleData dripParticle, SoundEvent sound, boolean canSourcesMultiply) {
        FluidResult result = new FluidResult();
        result.flowingFluid = new Flowing(particle, dripParticle, sound, canSourcesMultiply, result);
        result.stillFluid = new Source(particle, dripParticle, sound, canSourcesMultiply, result);
        Block.Properties properties = Block.Properties.create(material)
                .doesNotBlockMovement().tickRandomly().hardnessAndResistance(100.0F).lightValue(light).noDrops();
        result.block = new FlowingFluidBlock(result.flowingFluid, properties) {};
        return result;
    }
}

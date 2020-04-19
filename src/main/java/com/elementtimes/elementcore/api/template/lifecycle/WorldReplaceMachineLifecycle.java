package com.elementtimes.elementcore.api.template.lifecycle;

import com.elementtimes.elementcore.api.interfaces.block.IMachineLifecycle;
import com.elementtimes.elementcore.api.interfaces.block.IMachineRecipe;
import com.elementtimes.elementcore.api.recipe.MachineRecipeCapture;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 用于替换世界中的方块的机器生命周期
 * @deprecated 该类有问题，且依赖于 MachineRecipe，已启用
 *             使用 {@link WorldReplaceLifecycle} 替代
 * @author luqin2007
 */
public class WorldReplaceMachineLifecycle implements IMachineLifecycle {

    private TileEntity mMachine;
    private Function<BlockState, BlockState> mReplacer;
    private Function<BlockState, ImmutablePair<Integer, Object>> mCollector;
    private int mWorkCycle;
    private int mRadius, mDepth;
    private List<BlockPos> mBlockPos = null;

    private int mIntervalCount = 0;
    private int mFindPtr = 0;

    private BlockPos mReplacePos = null;
    private BlockState mReplaceBs = null;
    private Pair<Integer, Object> mReplaceCollect = null;

    /**
     * 替换方块
     * @param machine 机器 TestTileEntity
     * @param replacer 获取替换的方块。返回 IBlockState 本身或 null 都不会替换
     * @param collector 根据替换的 IBlockState 收集产物。
     *                  该函数返回一个 Pair，left[Integer] 代表输出物品/流体槽位，right[Object]代表输出类型。
     *                  Object 为 Item/Block/ItemStack 时，代表输出到物品槽，数量为 1 或 ItemStack 的数量
     *                  Object 为 Fluid/FluidStack 时，代表输出到流体槽，数量为 Fluid.BUCKET 或 FluidStack 的数量
     *                  Object 为其他类型或 null，或该函数直接返回 null 代表不输出
     * @param workCycle 工作周期。每个工作周期只有第一 tick 检查并标记流体，其他时间空闲，基本能保证每个周期最多抽取一格流体
     * @param radius 扫面范围的半径，实际扫描区域截面为边长为 2r+1 的正方形
     * @param depth 扫描的深度，扫描深度范围为从机器下一格算起，到机器 pos.y-depth 的位置
     */
    public WorldReplaceMachineLifecycle(TileEntity machine,
                                        Function<BlockState, BlockState> replacer,
                                        Function<BlockState, ImmutablePair<Integer, Object>> collector,
                                        int workCycle,
                                        int radius, int depth) {
        mMachine = machine;
        mReplacer = replacer;
        mCollector = collector;
        mWorkCycle = workCycle;
        mRadius = radius;
        mDepth = depth;
    }

    @Override
    public void onTickStart() {
        if (mBlockPos == null) {
            mBlockPos = new ArrayList<>();
            BlockPos start = mMachine.getPos().down().east(mRadius).north(mRadius);
            BlockPos end = mMachine.getPos().down(mDepth).west(mRadius).south(mRadius);
            BlockPos.getAllInBox(start, end).forEach(mBlockPos::add);
        }

        if (mIntervalCount >= mWorkCycle) {
            mIntervalCount = 0;
        }

        if (mFindPtr >= mBlockPos.size()) {
            mFindPtr = 0;
        }
    }

    @Override
    public boolean onCheckStart() {
        if (mIntervalCount == 0) {
            World world = mMachine.getWorld();
            for (; mFindPtr < mBlockPos.size(); mFindPtr++) {
                mReplacePos = mBlockPos.get(mFindPtr);
                BlockState replaceBsOri = world.getBlockState(mReplacePos);
                mReplaceBs = mReplacer.apply(replaceBsOri);
                if (mReplaceBs != null && mReplaceBs != replaceBsOri) {
                    mReplaceCollect = mCollector.apply(replaceBsOri);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onStart() {
        // 更新合成表
        if (mReplaceCollect != null) {
            Object right = mReplaceCollect.getRight();
            if (right != null) {
                if (right instanceof Fluid) {
                    markOutput(new FluidStack((Fluid) right, 1000), mReplaceCollect.getLeft());
                } else if (right instanceof FluidStack) {
                    markOutput((FluidStack) right, mReplaceCollect.getLeft());
                } else if (right instanceof IItemProvider) {
                    markOutput(new ItemStack((IItemProvider) right), mReplaceCollect.getLeft());
                } else if (right instanceof ItemStack) {
                    markOutput((ItemStack) right, mReplaceCollect.getLeft());
                }
            }
        }
        // 替换
        Objects.requireNonNull(mMachine.getWorld()).setBlockState(mReplacePos, mReplaceBs, 3);
    }

    private void markOutput(FluidStack fluid, int slot) {
        if (mMachine instanceof IMachineRecipe) {
            MachineRecipeCapture capture = ((IMachineRecipe) mMachine).getWorkingRecipe();
            assert capture != null;
            int size = capture.fluidOutputs.size();
            if (size <= slot) {
                for (; size <= slot; size++) {
                    capture.fluidOutputs.add(size, null);
                }
            }
            capture.fluidOutputs.set(slot, fluid);
        }
    }

    private void markOutput(ItemStack item, int slot) {
        if (mMachine instanceof IMachineRecipe) {
            MachineRecipeCapture capture = ((IMachineRecipe) mMachine).getWorkingRecipe();
            assert capture != null;
            int size = capture.outputs.size();
            if (size <= slot) {
                for (; size <= slot; size++) {
                    capture.outputs.add(size, ItemStack.EMPTY);
                }
            }
            capture.outputs.set(slot, item);
        }
    }

    @Override
    public void onTickFinish() {
        mIntervalCount++;
    }
}

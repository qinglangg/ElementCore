package com.elementtimes.elementcore.api.template.capability.fluid.vanilla.interfaces;

import com.elementtimes.elementcore.api.annotation.ModCapability;
import com.elementtimes.elementcore.api.template.capability.fluid.FluidTankInfo;
import com.elementtimes.elementcore.api.template.capability.fluid.vanilla.CapabilityFluidHandler;
import com.elementtimes.elementcore.api.template.capability.fluid.vanilla.FluidHandler;
import com.elementtimes.elementcore.api.template.fluid.FluidStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

@ModCapability(CapabilityFluidHandler.DefaultFluidHandlerStorage.class)
public interface IFluidHandler extends INBTSerializable<CompoundNBT> {

    /**
     * 获取容器整体信息
     * @return 流体信息
     */
    FluidTankInfo[] getTankInfos();

    /**
     * 直接获取流体类型
     * @param slot 位置
     * @return 流体
     */
    Fluid getFluid(int slot);

    /**
     * 直接获取流体
     * @param slot 位置
     * @return 流体
     */
    FluidStack getFluidStack(int slot);

    /**
     * 直接设置某位置的流体
     * @param slot 位置
     * @param stack 流体
     */
    default void setFluid(int slot, FluidStack stack) {
        setFluid(slot, stack.getFluid(), stack.getAmount());
    }

    /**
     * 像容器中充入流体
     * 注意。不管 doFill 为 true 还是 false，传入的 resource 是不会修改的，只会修改容器本身
     * @param resource 待充入流体
     * @param doFill 是否充入
     * @return 实际充入（doFill=false 将充入）的流体
     */
    int fill(FluidStack resource, boolean doFill);

    /**
     * 直接设置某位置的流体
     * @param slot 位置
     * @param fluid 流体类型
     * @param amount 流体量
     */
    void setFluid(int slot, Fluid fluid, int amount);

    /**
     * 获取容器位置个数
     * @return 个数
     */
    int getSize();

    /**
     * 某一位置是否已满
     * @param slot 位置
     * @return 满
     */
    boolean isFull(int slot);

    /**
     * 容器是否已满
     * @return 满
     */
    boolean isFull();

    /**
     * 某一位置是否空
     * @param slot 位置
     * @return 空
     */
    boolean isEmpty(int slot);

    /**
     * 容器是否空
     * @return 空
     */
    boolean isEmpty();

    /**
     * 获取某一位置的容量
     * @param slot 位置
     * @return 容量
     */
    int getCapacity(int slot);

    /**
     * 像容器中充入流体
     * 注意。不管 doFill 为 true 还是 false，传入的 resource 是不会修改的，只会修改容器本身
     * @param slot 待充入位置
     * @param resource 待充入流体
     * @param doFill 是否充入
     * @return 实际充入（doFill=false 将充入）的流体
     */
    int fill(int slot, FluidStack resource, boolean doFill);

    /**
     * 从容器中提取流体
     * 注意。不管 doDrain 为 true 还是 false，传入的 resource 是不会修改的，只会修改容器本身
     * @param resource 待提取流体
     * @param doDrain 是否提取
     * @return 实际取出（doFill=false 将取出）的流体
     */
    FluidStack drain(FluidStack resource, boolean doDrain);

    /**
     * 从容器中提取流体
     * 注意。不管 doDrain 为 true 还是 false，传入的 resource 是不会修改的，只会修改容器本身
     * @param slot 待提取位置
     * @param resource 待提取流体
     * @param doDrain 是否提取
     * @return 实际取出（doFill=false 将取出）的流体
     */
    FluidStack drain(int slot, FluidStack resource, boolean doDrain);

    /**
     * 从容器中提取流体
     * 注意。不管 doDrain 为 true 还是 false，传入的 resource 是不会修改的，只会修改容器本身
     * @param slot 待提取位置
     * @param maxDrain 待提取流体量
     * @param doDrain 是否提取
     * @return 实际取出（doFill=false 将取出）的流体
     */
    FluidStack drain(int slot, int maxDrain, boolean doDrain);

    /**
     * 从容器中提取流体
     * 注意。不管 doDrain 为 true 还是 false，传入的 resource 是不会修改的，只会修改容器本身
     * @param maxDrain 待提取流体量
     * @param doDrain 是否提取
     * @return 实际取出（doFill=false 将取出）的流体
     */
    FluidStack drain(int maxDrain, boolean doDrain);

    /**
     * 向 NBT 中写入数据
     * @param compound 存储 NBT
     * @return NBT
     */
    @Nonnull
    CompoundNBT write(@Nonnull CompoundNBT compound);

    FluidStack drainNotFilter(int slot, Fluid fluid, int amount, boolean doDrain);

    void setNotFilter(int slot, Fluid fluid, int amount);

    int fillNotFilter(int slot, Fluid fluid, int amount, boolean doDrain);

    default boolean isFillValid(int slot, Fluid fluid, int amount) {
        return true;
    }

    default boolean isDrainValid(int slot, Fluid fluid, int amount) {
        return true;
    }

    default boolean isSetValid(int slot, Fluid fluid, int amount) {
        return true;
    }

    static IFluidHandler factory() {
        return new FluidHandler(1, net.minecraftforge.fluids.Fluid.BUCKET_VOLUME);
    }
}

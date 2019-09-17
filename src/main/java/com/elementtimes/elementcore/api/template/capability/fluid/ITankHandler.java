package com.elementtimes.elementcore.api.template.capability.fluid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.List;

public interface ITankHandler extends INBTSerializable<NBTTagCompound>, IFluidHandler {

    /**
     * 向某一位置填充流体
     * @param slot 槽位
     * @param resource 流体
     * @param doFill 是否填充
     * @return 充入流体量
     */
    int fill(int slot, FluidStack resource, boolean doFill);

    /**
     * 不经检查，向某一位置填充流体
     * @param slot 槽位
     * @param resource 流体
     * @param doFill 是否填充
     * @return 充入流体量
     */
    int fillIgnoreCheck(int slot, FluidStack resource, boolean doFill);

    /**
     * 从某一位置提取流体
     * @param slot 槽位
     * @param resource 流体
     * @param doDrain 是否提取
     * @return 提取流体
     */
    FluidStack drain(int slot, FluidStack resource, boolean doDrain);

    /**
     * 不经检查，从某一位置提取流体
     * @param slot 槽位
     * @param resource 流体
     * @param doDrain 是否提取
     * @return 提取流体
     */
    FluidStack drainIgnoreCheck(int slot, FluidStack resource, boolean doDrain);

    /**
     * 从某一位置提取流体
     * @param slot 槽位
     * @param maxDrain 提取流体量
     * @param doDrain 是否提取
     * @return 提取流体
     */
    FluidStack drain(int slot, int maxDrain, boolean doDrain);

    /**
     * 不经检查，从某一位置提取流体
     * @param slot 槽位
     * @param maxDrain 提取流体量
     * @param doDrain 是否提取
     * @return 提取流体
     */
    FluidStack drainIgnoreCheck(int slot, int maxDrain, boolean doDrain);

    /**
     * 获取容器槽个数
     * @return 槽个数
     */
    int size();

    /**
     * 获取某一槽位的流体
     * @param slot 槽位
     * @param copy 是否返回流体的副本
     * @return 流体
     */
    FluidStack getFluid(int slot, boolean copy);

    /**
     * 获取某一槽位的流体的副本
     * @param slot 槽位
     * @return 流体
     */
    default FluidStack getFluid(int slot) {
        return getFluid(slot, true);
    }

    /**
     * 获取第一个槽位的流体的副本
     * @return 流体
     */
    default FluidStack getFluidFirst() {
        return getFluid(0);
    }

    /**
     * 获取某一槽位可存储流体量
     * @param slot 槽位
     * @return 流体上限
     */
    int getCapacity(int slot);
}

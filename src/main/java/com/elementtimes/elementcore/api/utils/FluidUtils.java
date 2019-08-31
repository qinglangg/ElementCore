package com.elementtimes.elementcore.api.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 流体工具类
 * @author luqin2007
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class FluidUtils {

    public static final FluidStack EMPTY = new FluidStack(FluidRegistry.WATER, 0);

    private static FluidUtils u = null;
    public static FluidUtils getInstance() {
        if (u == null) {
            u = new FluidUtils();
        }
        return u;
    }

    /**
     * 将流体列表转化为 NBT 列表
     * @param fluids 流体列表
     * @return NBT 列表
     */
    public NBTTagList toNbtList(List<FluidStack> fluids) {
        NBTTagList list = new NBTTagList();
        fluids.forEach(fluid -> list.appendTag(fluid.writeToNBT(new NBTTagCompound())));
        return list;
    }

    /**
     * 从 NBT 列表读取流体
     * @param list NBT 列表
     * @return 流体
     */
    public List<FluidStack> fromNbtList(NBTTagList list) {
        int count = list.tagCount();
        List<FluidStack> fluidStacks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            fluidStacks.add(FluidStack.loadFluidStackFromNBT(list.getCompoundTagAt(i)));
        }
        return fluidStacks;
    }

    /**
     * 无流体
     * @param properties IFluidTankProperties
     * @return 不包含流体槽位
     */
    public boolean isNoCapability(IFluidTankProperties[] properties) {
        return properties.length == 0 || Arrays.stream(properties).allMatch(p -> p.getCapacity() <= 0);
    }

    /**
     * 流体容器非空
     * @param properties IFluidTankProperties
     * @return 流体容器非空
     */
    public boolean isEmpty(IFluidTankProperties[] properties) {
        for (IFluidTankProperties property : properties) {
            if (property.getContents() != null && property.getContents().amount > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 流体容器某一槽位非空
     * @param properties IFluidTankProperties
     * @param slot 槽位
     * @return 非空
     */
    public boolean isEmpty(IFluidTankProperties[] properties, int slot) {
        if (slot < properties.length) {
            IFluidTankProperties property = properties[slot];
            return property.getContents() == null || property.getContents().amount == 0;
        }
        return true;
    }

    /**
     * 流体容器满
     * @param properties IFluidTankProperties
     * @return 流体容器满，。
     */
    public boolean isFull(IFluidTankProperties[] properties) {
        for (IFluidTankProperties property : properties) {
            int capacity = property.getCapacity();
            if (capacity > 0) {
                if (property.getContents() == null || property.getContents().amount < capacity) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 某槽位是否满
     * @param properties IFluidTankProperties
     * @param slot 槽位
     * @return 是否满
     */
    public boolean isFull(IFluidTankProperties[] properties, int slot) {
        if (slot < properties.length) {
            IFluidTankProperties property = properties[slot];
            int capacity = property.getCapacity();
            return capacity <= 0 || (property.getContents() != null && property.getContents().amount >= capacity);
        }
        return false;
    }

    /**
     * 将某流体容器的流体转化为列表
     * @param properties IFluidTankProperties
     * @return 列表
     */
    public List<FluidStack> toList(IFluidTankProperties[] properties) {
        System.out.println(Arrays.toString(properties));
        return Arrays.stream(properties)
                .map(IFluidTankProperties::getContents)
                .collect(Collectors.toList());
    }

    /**
     * 将某流体容器的流体转化为列表，列表非空
     * @param properties IFluidTankProperties
     * @return 列表
     */
    public List<FluidStack> toListNotNull(IFluidTankProperties[] properties) {
        return Arrays.stream(properties)
                .map(IFluidTankProperties::getContents)
                .map(f -> (f != null && f.amount > 0) ? f : EMPTY)
                .collect(Collectors.toList());
    }

    /**
     * 从物品中获取流体
     * @param itemStack 物品栈
     * @return 流体
     */
    public FluidStack getFluid(ItemStack itemStack) {
        FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(itemStack.getTagCompound());
        if (fluidStack == null) {
            IFluidHandlerItem capability = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (capability != null) {
                if (capability instanceof FluidBucketWrapper) {
                    fluidStack = ((FluidBucketWrapper) capability).getFluid();
                } else if (capability instanceof FluidHandlerItemStack) {
                    fluidStack = ((FluidHandlerItemStack) capability).getFluid();
                } else if (capability instanceof FluidHandlerItemStackSimple) {
                    fluidStack = ((FluidHandlerItemStackSimple) capability).getFluid();
                } else {
                    fluidStack = capability.getTankProperties()[0].getContents();
                }
            }
        }
        return fluidStack;
    }

    /**
     * 从物品中获取流体，非空
     * @param itemStack 物品栈
     * @return 流体
     */
    public FluidStack getFluidNotNull(ItemStack itemStack) {
        FluidStack fluidStack = getFluid(itemStack);
        return fluidStack == null ? EMPTY : fluidStack;
    }
}

package com.elementtimes.elementcore.api.utils;

import com.elementtimes.elementcore.api.template.capability.fluid.ITankHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 流体工具类
 * @author luqin2007
 */
@SuppressWarnings({"unused"})
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
    public NBTTagList saveToNbt(List<FluidStack> fluids) {
        NBTTagList list = new NBTTagList();
        for (FluidStack fluid : fluids) {
            if (fluid != null) {
                list.appendTag(fluid.writeToNBT(new NBTTagCompound()));
            } else {
                list.appendTag(new NBTTagCompound());
            }
        }
        return list;
    }

    /**
     * 从 NBT 列表读取流体
     * @param list NBT 列表
     * @return 流体
     */
    public List<FluidStack> readFromNbt(NBTTagList list) {
        int count = list.tagCount();
        List<FluidStack> fluidStacks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            NBTTagCompound compound = list.getCompoundTagAt(i);
            if (compound.hasNoTags()) {
                fluidStacks.add(null);
            } else {
                fluidStacks.add(FluidStack.loadFluidStackFromNBT(compound));
            }
        }
        return fluidStacks;
    }

    /**
     * 流体容器非空
     * @param properties IFluidTankProperties[]
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
     * 流体容器非空
     * @param handler 流体容器
     * @return 流体容器非空
     */
    public boolean isEmpty(IFluidHandler handler) {
        if (handler instanceof IFluidTank) {
            return ((IFluidTank) handler).getFluidAmount() == 0 && ((IFluidTank) handler).getCapacity() > 0;
        } else if (handler instanceof ITankHandler) {
            ITankHandler tank = (ITankHandler) handler;
            for (int i = 0; i < tank.size(); i++) {
                FluidStack fluid = tank.getFluid(i, false);
                if (fluid != null && fluid.amount > 0) {
                    return false;
                }
            }
            return true;
        } else if (handler instanceof FluidBucketWrapper) {
            FluidStack stack = ((FluidBucketWrapper) handler).getFluid();
            return stack == null || stack.amount == 0;
        } else if (handler instanceof FluidHandlerItemStackSimple) {
            FluidStack stack = ((FluidHandlerItemStackSimple) handler).getFluid();
            return stack == null || stack.amount == 0;
        } else if (handler instanceof FluidHandlerItemStack) {
            FluidStack stack = ((FluidHandlerItemStack) handler).getFluid();
            return stack == null || stack.amount == 0;
        } else if (handler != null) {
            FluidStack stack = handler.drain(1, false);
            return stack != null && stack.amount > 0;
        } else {
            return false;
        }
    }

    /**
     * 流体容器某一槽位空
     * @param properties IFluidTankProperties
     * @param slot 槽位
     * @return 空
     */
    public boolean isEmpty(IFluidTankProperties[] properties, int slot) {
        if (slot < properties.length) {
            IFluidTankProperties property = properties[slot];
            return property.getContents() == null || property.getContents().amount == 0;
        }
        return false;
    }

    /**
     * 流体容器某一槽位空
     * @param handler 流体容器
     * @param slot 槽位
     * @return 空
     */
    public boolean isEmpty(IFluidHandler handler, int slot) {
        if (handler instanceof ITankHandler) {
            ITankHandler tank = (ITankHandler) handler;
            if (slot < tank.size()) {
                FluidStack fluid = tank.getFluid(slot, false);
                return fluid == null || fluid.amount == 0;
            }
            return false;
        } else if (handler instanceof IFluidTank) {
            return slot == 0 && ((IFluidTank) handler).getFluidAmount() == 0 && ((IFluidTank) handler).getCapacity() > 0;
        } else if (handler instanceof FluidBucketWrapper) {
            FluidStack stack = ((FluidBucketWrapper) handler).getFluid();
            return slot == 0 && (stack == null || stack.amount == 0);
        } else if (handler instanceof FluidHandlerItemStackSimple) {
            FluidStack stack = ((FluidHandlerItemStackSimple) handler).getFluid();
            return slot == 0 && (stack == null || stack.amount == 0);
        } else if (handler instanceof FluidHandlerItemStack) {
            FluidStack stack = ((FluidHandlerItemStack) handler).getFluid();
            return slot == 0 && (stack == null || stack.amount == 0);
        } else {
            return handler != null && isEmpty(handler.getTankProperties(), slot);
        }
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
     * 流体容器满
     * @param handler 流体容器
     * @return 流体容器满，。
     */
    public boolean isFull(IFluidHandler handler) {
        if (handler instanceof ITankHandler) {
            ITankHandler tank = (ITankHandler) handler;
            for (int i = 0; i < tank.size(); i++) {
                if (!isFull(tank, i)) {
                    return false;
                }
            }
            return true;
        } else if (handler instanceof IFluidTank) {
            return ((IFluidTank) handler).getFluidAmount() >= ((IFluidTank) handler).getCapacity();
        } else if (handler instanceof FluidBucketWrapper) {
            FluidStack stack = ((FluidBucketWrapper) handler).getFluid();
            return stack != null && stack.amount >= Fluid.BUCKET_VOLUME;
        } else if (handler instanceof FluidHandlerItemStackSimple) {
            FluidStack stack = ((FluidHandlerItemStackSimple) handler).getFluid();
            return stack != null && stack.amount >= Fluid.BUCKET_VOLUME;
        } else if (handler instanceof FluidHandlerItemStack) {
            FluidStack stack = ((FluidHandlerItemStack) handler).getFluid();
            return stack != null && stack.amount >= Fluid.BUCKET_VOLUME;
        } else {
            return handler != null && isFull(handler.getTankProperties());
        }
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
     * 某槽位是否满
     * @param handler 流体容器
     * @param slot 槽位
     * @return 是否满
     */
    public boolean isFull(IFluidHandler handler, int slot) {
        if (handler instanceof ITankHandler) {
            ITankHandler tank = (ITankHandler) handler;
            if (tank.size() > slot) {
                FluidStack fluid = tank.getFluid(slot, false);
                int capacity = tank.getCapacity(slot);
                return capacity < 0 || (fluid != null && fluid.amount >= capacity);
            }
            return false;
        } else if (handler instanceof IFluidTank) {
            return slot == 0 && ((IFluidTank) handler).getFluidAmount() >= ((IFluidTank) handler).getCapacity();
        } else if (handler instanceof FluidBucketWrapper) {
            FluidStack stack = ((FluidBucketWrapper) handler).getFluid();
            return slot == 0 && stack != null && stack.amount >= Fluid.BUCKET_VOLUME;
        } else {
            return handler != null && isFull(handler.getTankProperties(), slot);
        }
    }

    /**
     * 将某流体容器的流体转化为列表
     * @param properties IFluidTankProperties
     * @return 列表
     */
    public List<FluidStack> toListIndexed(IFluidTankProperties[] properties, @Nullable FluidStack nullStack) {
        return Arrays.stream(properties)
                .map(IFluidTankProperties::getContents)
                .map(f -> (f != null && f.amount > 0) ? f : nullStack)
                .collect(Collectors.toList());
    }

    /**
     * 将某流体容器的流体转化为列表
     * @param handler 流体容器
     * @return 列表
     */
    public List<FluidStack> toListIndexed(IFluidHandler handler, @Nullable FluidStack nullStack) {
        ArrayList<FluidStack> fluidStacks = new ArrayList<>();
        if (handler instanceof ITankHandler) {
            ITankHandler tank = (ITankHandler) handler;
            int size = tank.size();
            for (int i = 0; i < size; i++) {
                FluidStack fluid = tank.getFluid(i);
                if (fluid == null) {
                    fluid = nullStack;
                }
                fluidStacks.add(i, fluid);
            }
        } else if (handler instanceof IFluidTank) {
            fluidStacks.add(nullStack);
        } else if (handler instanceof FluidBucketWrapper) {
            FluidStack stack = ((FluidBucketWrapper) handler).getFluid();
            fluidStacks.add(stack == null ? nullStack : stack);
        } else if (handler instanceof FluidHandlerItemStack) {
            FluidStack stack = ((FluidHandlerItemStack) handler).getFluid();
            fluidStacks.add(stack == null ? nullStack : stack);
        } else if (handler instanceof FluidHandlerItemStackSimple) {
            FluidStack stack = ((FluidHandlerItemStackSimple) handler).getFluid();
            fluidStacks.add(stack == null ? nullStack : stack);
        } else if (handler != null) {
            return toListIndexed(handler.getTankProperties(), nullStack);
        }
        return fluidStacks;
    }

    /**
     * 将某流体容器的流体转化为列表
     * @param properties IFluidTankProperties
     * @return 列表
     */
    public List<FluidStack> toListIndexed(IFluidTankProperties[] properties) {
        return toListIndexed(properties, null);
    }

    /**
     * 将某流体容器的流体转化为列表
     * @param handler 流体容器
     * @return 列表
     */
    public List<FluidStack> toListIndexed(IFluidHandler handler) {
        return toListIndexed(handler, null);
    }

    /**
     * 将某流体容器的流体转化为列表
     * @param properties IFluidTankProperties
     * @return 列表
     */
    public List<FluidStack> toList(IFluidTankProperties[] properties) {
        return Arrays.stream(properties)
                .map(IFluidTankProperties::getContents)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 将某流体容器的流体转化为列表
     * @param handler 流体容器
     * @return 列表
     */
    public List<FluidStack> toList(IFluidHandler handler) {
        ArrayList<FluidStack> fluidStacks = new ArrayList<>();
        if (handler instanceof ITankHandler) {
            ITankHandler tank = (ITankHandler) handler;
            int size = tank.size();
            for (int i = 0; i < size; i++) {
                FluidStack fluid = tank.getFluid(i);
                if (fluid == null) {
                    continue;
                }
                fluidStacks.add(i, fluid);
            }
        } else if (handler instanceof IFluidTank) {
            FluidStack stack = ((IFluidTank) handler).getFluid();
            if (stack != null) {
                fluidStacks.add(stack);
            }
        } else if (handler instanceof FluidBucketWrapper) {
            FluidStack stack = ((FluidBucketWrapper) handler).getFluid();
            if (stack != null) {
                fluidStacks.add(stack);
            }
        } else if (handler instanceof FluidHandlerItemStack) {
            FluidStack stack = ((FluidHandlerItemStack) handler).getFluid();
            if (stack != null) {
                fluidStacks.add(stack);
            }
        } else if (handler instanceof FluidHandlerItemStackSimple) {
            FluidStack stack = ((FluidHandlerItemStackSimple) handler).getFluid();
            if (stack != null) {
                fluidStacks.add(stack);
            }
        } else if (handler != null) {
            return toList(handler.getTankProperties());
        }
        return fluidStacks;
    }

    /**
     * 从物品中获取流体
     * @param itemStack 物品栈
     * @return 流体
     */
    public FluidStack getFluid(ItemStack itemStack, @Nullable FluidStack stack) {
        IFluidHandlerItem handler = FluidUtil.getFluidHandler(itemStack);
        FluidStack result;
        if (handler instanceof ITankHandler) {
            result = ((ITankHandler) handler).getFluid(0, true);
        } else {
            result = FluidUtil.getFluidContained(itemStack);
        }
        return result == null ? stack : result;
    }

    /**
     * 从物品中获取流体
     * @param itemStack 物品栈
     * @return 流体
     */
    public FluidStack getFluid(ItemStack itemStack) {
        return getFluid(itemStack, null);
    }

    public boolean hasFluid(IFluidHandler handler, Fluid fluid) {
        if (handler == null || fluid == null) {
            return false;
        }
        IFluidTankProperties[] properties = handler.getTankProperties();
        for (int i = 0; i < properties.length; i++) {
            FluidStack contents = properties[i].getContents();
            if (contents != null && contents.getFluid() == fluid) {
                return true;
            }
        }
        return false;
    }

    public NBTTagCompound writeToNBT(IFluidHandler handler, String key, NBTTagCompound nbtTagCompound) {
        if (handler instanceof INBTSerializable) {
            nbtTagCompound.setTag(key, ((INBTSerializable<? extends NBTBase>) handler).serializeNBT());
        } else {
            NBTTagList list = new NBTTagList();
            for (FluidStack fluid : toListIndexed(handler)) {
                if (fluid == null) {
                    list.appendTag(new NBTTagCompound());
                } else {
                    list.appendTag(fluid.writeToNBT(new NBTTagCompound()));
                }
            }
            nbtTagCompound.setTag(key, list);
        }
        return nbtTagCompound;
    }

    public void readFromNBT(IFluidHandler handler, String key, NBTTagCompound nbtTagCompound) {
        if (handler instanceof INBTSerializable) {
            //noinspection unchecked
            ((INBTSerializable) handler).deserializeNBT(nbtTagCompound.getTag(key));
        } else {
            NBTTagList list = (NBTTagList) nbtTagCompound.getTag(key);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound compound = list.getCompoundTagAt(i);
                if (!compound.hasNoTags()) {
                    handler.fill(FluidStack.loadFluidStackFromNBT(compound), true);
                }
            }
        }
    }
}

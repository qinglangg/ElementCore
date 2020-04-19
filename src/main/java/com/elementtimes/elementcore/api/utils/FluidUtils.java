package com.elementtimes.elementcore.api.utils;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.*;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 流体工具类
 * @author luqin2007
 */
@SuppressWarnings({"unused"})
public class FluidUtils {

    /**
     * 将流体列表转化为 NBT 列表
     * @param fluids 流体列表
     * @return NBT 列表
     */
    public static ListNBT saveToNbt(List<FluidStack> fluids) {
        return fluids.stream().map(f -> f.writeToNBT(new CompoundNBT())).collect(CollectUtils.toNbtList());
    }

    /**
     * 从 NBT 列表读取流体
     * @param list NBT 列表
     * @return 流体
     */
    public static List<FluidStack> readFromNbt(ListNBT list) {
        return list.stream()
                .map(nbt -> (CompoundNBT) nbt)
                .map(FluidStack::loadFluidStackFromNBT)
                .collect(Collectors.toList());
    }

    /**
     * 流体容器非空
     * @param handler 流体容器
     * @return 流体容器非空
     */
    public static boolean isEmpty(IFluidHandler handler) {
        if (handler instanceof FluidBucketWrapper) {
            return ((FluidBucketWrapper) handler).getFluid().isEmpty();
        } else if (handler instanceof FluidHandlerItemStackSimple) {
            return ((FluidHandlerItemStackSimple) handler).getFluid().isEmpty();
        } else if (handler instanceof FluidHandlerItemStack) {
            return ((FluidHandlerItemStack) handler).getFluid().isEmpty();
        } else if (handler instanceof EmptyFluidHandler || handler instanceof VoidFluidHandler) {
            return true;
        } else if (handler instanceof IFluidTank) {
            IFluidTank tank = (IFluidTank) handler;
            return tank.getFluidAmount() == 0 && tank.getCapacity() > 0;
        } else if (handler != null) {
            for (int i = 0; i < handler.getTanks(); i++) {
                if (!handler.getFluidInTank(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 流体容器某一槽位空
     * @param handler 流体容器
     * @param slot 槽位
     * @return 空
     */
    public static boolean isEmpty(IFluidHandler handler, int slot) {
        if (handler instanceof IFluidTank) {
            return slot == 0 && ((IFluidTank) handler).getFluid().isEmpty();
        } else if (handler instanceof FluidBucketWrapper) {
            return slot == 0 && ((FluidBucketWrapper) handler).getFluid().isEmpty();
        } else if (handler instanceof FluidHandlerItemStackSimple) {
            return slot == 0 && ((FluidHandlerItemStackSimple) handler).getFluid().isEmpty();
        } else if (handler instanceof FluidHandlerItemStack) {
            return slot == 0 && ((FluidHandlerItemStack) handler).getFluid().isEmpty();
        } else if (handler instanceof EmptyFluidHandler || handler instanceof VoidFluidHandler) {
            return true;
        } else {
            return handler != null && handler.getFluidInTank(slot).isEmpty();
        }
    }

    /**
     * 流体容器满
     * @param handler 流体容器
     * @return 流体容器满，。
     */
    public static boolean isFull(IFluidHandler handler) {
        if (handler instanceof IFluidTank) {
            return ((IFluidTank) handler).getFluidAmount() >= ((IFluidTank) handler).getCapacity();
        } else if (handler instanceof FluidBucketWrapper) {
            return !((FluidBucketWrapper) handler).getFluid().isEmpty();
        } else if (handler instanceof FluidHandlerItemStackSimple) {
            return ((FluidHandlerItemStackSimple) handler).getFluid().getAmount() >= handler.getTankCapacity(0);
        } else if (handler instanceof FluidHandlerItemStack) {
            return ((FluidHandlerItemStack) handler).getFluid().getAmount() >= handler.getTankCapacity(0);
        } else if (handler != null) {
            for (int i = 0; i < handler.getTanks(); i++) {
                if (handler.getFluidInTank(i).getAmount() < handler.getTankCapacity(i)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 某槽位是否满
     * @param handler 流体容器
     * @param slot 槽位
     * @return 是否满
     */
    public static boolean isFull(IFluidHandler handler, int slot) {
        if (handler instanceof IFluidTank) {
            return slot == 0 && ((IFluidTank) handler).getFluidAmount() >= ((IFluidTank) handler).getCapacity();
        } else if (handler instanceof FluidBucketWrapper) {
            FluidStack stack = ((FluidBucketWrapper) handler).getFluid();
            return slot == 0 && stack.getAmount() >= handler.getTankCapacity(slot);
        } else {
            return handler != null && handler.getFluidInTank(slot).getAmount() >= handler.getTankCapacity(slot);
        }
    }

    /**
     * 将某流体容器的流体转化为列表
     * @param handler 流体容器
     * @return 列表
     */
    public static List<FluidStack> toList(IFluidHandler handler) {
        ArrayList<FluidStack> list = new ArrayList<>();
        if (handler != null) {
            for (int i = 0; i < handler.getTanks(); i++) {
                list.add(handler.getFluidInTank(i).copy());
            }
        }
        return list;
    }

    /**
     * 将某流体容器的流体转化为列表
     * @param handlerOpt 流体容器
     * @return 列表
     */
    public static List<FluidStack> toList(LazyOptional<IFluidHandler> handlerOpt) {
        return toList(handlerOpt.orElse(null));
    }

    /**
     * 将某流体容器的流体转化为列表
     * @param handlerStack 流体容器
     * @return 列表
     */
    public static List<FluidStack> toList(ItemStack handlerStack) {
        return toList(FluidUtil.getFluidHandler(handlerStack).orElse(null));
    }

    /**
     * 从物品中获取流体
     * @param itemStack 物品栈
     * @return 流体
     */
    public static FluidStack getFluid(ItemStack itemStack) {
        FluidStack[] result = new FluidStack[] { FluidStack.EMPTY };
        FluidUtil.getFluidContained(itemStack).ifPresent(is -> result[0] = is);
        return result[0];
    }

    /**
     * 判断一个容器是否具有某种流体
     * @param handler 容器
     * @param fluid 待检查流体
     * @return 是否具有流体
     */
    public static boolean hasFluid(IFluidHandler handler, Fluid fluid) {
        if (handler == null || fluid == null || handler instanceof EmptyFluidHandler || handler instanceof VoidFluidHandler) {
            return false;
        }
        for (int i = 0; i < handler.getTanks(); i++) {
            if (handler.getFluidInTank(i).getRawFluid() == fluid) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断一个容器是否具有某种流体
     * @param handler 容器
     * @param fluid 待检查流体
     * @return 是否具有流体
     */
    public static boolean hasFluid(ItemStack handler, Fluid fluid) {
        LazyOptional<IFluidHandlerItem> fluidHandler = FluidUtil.getFluidHandler(handler);
        return fluidHandler.isPresent() && hasFluid(fluidHandler.orElse(null), fluid);
    }

    /**
     * 将一个流体容器写入 NBT 数据中
     * @param handler 容器
     * @param key 写入 NBT 的 key
     * @param nbt nbt
     * @return nbt
     */
    public static CompoundNBT writeToNBT(IFluidHandler handler, String key, CompoundNBT nbt) {
        if (handler instanceof INBTSerializable) {
            nbt.put(key, ((INBTSerializable<? extends INBT>) handler).serializeNBT());
        } else {
            nbt.put(key, saveToNbt(toList(handler)));
        }
        return nbt;
    }

    /**
     * 从一个 NBT 数据中取出流体
     * @param handler 容器
     * @param key NBT 的 key
     * @param nbt nbt
     */
    public static void readFromNBT(IFluidHandler handler, String key, CompoundNBT nbt) {
        if (handler instanceof INBTSerializable) {
            //noinspection unchecked
            ((INBTSerializable) handler).deserializeNBT(nbt.get(key));
        } else {
            ListNBT list = nbt.getList(key, Constants.NBT.TAG_COMPOUND);
            for (INBT inbt : list) {
                handler.fill(FluidStack.loadFluidStackFromNBT((CompoundNBT) inbt), IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }
}

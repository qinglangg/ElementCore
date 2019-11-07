package com.elementtimes.elementcore.api.template.tileentity.interfaces;

import com.elementtimes.elementcore.api.template.capability.item.IItemHandler;
import com.elementtimes.elementcore.api.template.capability.item.ItemHandler;
import com.elementtimes.elementcore.api.template.interfaces.INbtReadable;
import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 对物品的接口
 * @author luqin2007
 */
public interface ITileItemHandler extends ICapabilityProvider, INbtReadable {

    String NBT_ITEMS = "_items_";
    String NBT_ITEMS_INPUT = "_inputs_";
    String NBT_ITEMS_OUTPUT = "_outputs_";
    String NBT_ITEMS_NONE = "_none_";

    /**
     * 获取对应方向可操作的物品容器种类
     * @see SideHandlerType
     * @param facing 朝向
     * @return 种类
     */
    @Nonnull
    default SideHandlerType getItemType(@Nonnull Direction facing) {
        return SideHandlerType.ALL;
    }

    /**
     * 根据物品容器种类，获取物品槽
     * @param type 物品容器种类
     * @return 对应物品容器
     */
    @Nonnull
    IItemHandler getItemHandler(@Nonnull SideHandlerType type);

    /**
     * 根据朝向直接获取物品容器
     * @param facing 朝向
     * @return 对应物品种类
     */
    @Nonnull
    default IItemHandler getItemHandler(Direction facing) {
        return getItemHandler(getItemType(facing));
    }

    /**
     * 设置某类型的 ItemHandler
     * @param type 类型
     * @param handler ItemHandler
     */
    default void setItemHandler(@Nonnull SideHandlerType type, IItemHandler handler) {};

    @Override
    default <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        return LazyOptional.of(() -> (T) getItemHandler(getItemType(facing)));
    }

    @Override
    default void read(@Nonnull CompoundNBT nbt) {
        if (nbt.contains(NBT_ITEMS)) {
            CompoundNBT nbtItems = nbt.getCompound(NBT_ITEMS);
            if (nbtItems.contains(NBT_ITEMS_INPUT)) {
                final IItemHandler input = getItemHandler(SideHandlerType.INPUT);
                input.deserializeNBT(nbtItems.getCompound(NBT_ITEMS_INPUT));
            }
            if (nbtItems.contains(NBT_ITEMS_OUTPUT)) {
                final IItemHandler output = getItemHandler(SideHandlerType.OUTPUT);
                output.deserializeNBT(nbtItems.getCompound(NBT_ITEMS_OUTPUT));
            }
            if (nbtItems.contains(NBT_ITEMS_NONE)) {
                final IItemHandler none = getItemHandler(SideHandlerType.NONE);
                none.deserializeNBT(nbtItems.getCompound(NBT_ITEMS_NONE));
            }
        }
    }

    @Nonnull
    @Override
    default CompoundNBT write(@Nonnull CompoundNBT compound) {
        CompoundNBT nbt = new CompoundNBT();
        // input
        IItemHandler inputs = getItemHandler(SideHandlerType.INPUT);
        if (inputs != ItemHandler.EMPTY && inputs.getSlots() > 0) {
            nbt.put(NBT_ITEMS_INPUT, ((INBTSerializable) inputs).serializeNBT());
        }
        // output
        IItemHandler outputs = getItemHandler(SideHandlerType.OUTPUT);
        if (outputs != ItemHandler.EMPTY && outputs.getSlots() > 0) {
            nbt.put(NBT_ITEMS_OUTPUT, ((INBTSerializable) outputs).serializeNBT());
        }
        // none
        IItemHandler none = getItemHandler(SideHandlerType.NONE);
        if (none != ItemHandler.EMPTY && outputs.getSlots() > 0) {
            nbt.put(NBT_ITEMS_NONE, ((INBTSerializable) none).serializeNBT());
        }
        compound.put(NBT_ITEMS, nbt);
        return compound;
    }

    /**
     * 用于校验输入区是否可以放入某物品栈
     * @param stack 要输入的物品栈
     * @param slot 输入的槽位
     * @return true：可以放入
     */
    boolean isInputValid(int slot, ItemStack stack);
}

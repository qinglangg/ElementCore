package com.elementtimes.elementcore.api.template.tileentity.interfaces;

import com.elementtimes.elementcore.api.template.capability.item.IItemHandler;
import com.elementtimes.elementcore.api.template.capability.item.ItemHandler;
import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 对物品的接口
 * @author luqin2007
 */
public interface ITileItemHandler extends ICapabilityProvider, INBTSerializable<NBTTagCompound> {

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
    default SideHandlerType getItemType(@Nullable EnumFacing facing) {
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
    default IItemHandler getItemHandler(EnumFacing facing) {
        return getItemHandler(getItemType(facing));
    }

    default void setItemHandler(@Nonnull SideHandlerType type, IItemHandler handler) {};

    @Override
    default boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    @Nullable
    @Override
    default <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability.cast((T) getItemHandler(getItemType(facing)));
    }

    @Override
    default void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey(NBT_ITEMS)) {
            NBTTagCompound nbtItems = nbt.getCompoundTag(NBT_ITEMS);
            if (nbtItems.hasKey(NBT_ITEMS_INPUT)) {
                final IItemHandler input = getItemHandler(SideHandlerType.INPUT);
                input.deserializeNBT(nbtItems.getCompoundTag(NBT_ITEMS_INPUT));
            }
            if (nbtItems.hasKey(NBT_ITEMS_OUTPUT)) {
                final IItemHandler output = getItemHandler(SideHandlerType.OUTPUT);
                output.deserializeNBT(nbtItems.getCompoundTag(NBT_ITEMS_OUTPUT));
            }
            if (nbtItems.hasKey(NBT_ITEMS_NONE)) {
                final IItemHandler none = getItemHandler(SideHandlerType.NONE);
                none.deserializeNBT(nbtItems.getCompoundTag(NBT_ITEMS_NONE));
            }
        }
    }

    @Override
    default NBTTagCompound serializeNBT() {
        return writeToNBT(new NBTTagCompound());
    }

    default NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        NBTTagCompound nbt = new NBTTagCompound();
        // input
        IItemHandler inputs = getItemHandler(SideHandlerType.INPUT);
        if (inputs != ItemHandler.EMPTY && inputs.getSlots() > 0) {
            nbt.setTag(NBT_ITEMS_INPUT, ((INBTSerializable) inputs).serializeNBT());
        }
        // output
        IItemHandler outputs = getItemHandler(SideHandlerType.OUTPUT);
        if (outputs != ItemHandler.EMPTY && outputs.getSlots() > 0) {
            nbt.setTag(NBT_ITEMS_OUTPUT, ((INBTSerializable) outputs).serializeNBT());
        }
        // none
        IItemHandler none = getItemHandler(SideHandlerType.NONE);
        if (none != ItemHandler.EMPTY && outputs.getSlots() > 0) {
            nbt.setTag(NBT_ITEMS_NONE, ((INBTSerializable) none).serializeNBT());
        }
        nbtTagCompound.setTag(NBT_ITEMS, nbt);
        return nbtTagCompound;
    }

    /**
     * 用于校验输入区是否可以放入某物品栈
     * @param stack 要输入的物品栈
     * @param slot 输入的槽位
     * @return true：可以放入
     */
    boolean isInputValid(int slot, ItemStack stack);
}

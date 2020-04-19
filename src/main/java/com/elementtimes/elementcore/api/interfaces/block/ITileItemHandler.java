package com.elementtimes.elementcore.api.interfaces.block;

import com.elementtimes.elementcore.api.template.capability.ProxyItemHandler;
import com.elementtimes.elementcore.api.template.capability.SideHandlerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 对物品的接口
 * @author luqin2007
 */
public interface ITileItemHandler extends ICapabilityProvider {

    String NBT_ITEMS = "ECItem";

    ProxyItemHandler getItemHandler();

    /**
     * 获取对应方向可操作的物品容器种类
     * @see SideHandlerType
     * @param facing 朝向
     * @return 种类
     */
    @Nonnull
    default SideHandlerType getItemType(@Nullable Direction facing) {
        if (facing == null) {
            return SideHandlerType.IN_OUT;
        }
        switch (facing) {
            case UP: return SideHandlerType.INPUT;
            case DOWN: return SideHandlerType.OUTPUT;
            default: return SideHandlerType.IN_OUT;
        }
    }

    /**
     * 根据物品容器种类，获取物品槽
     * @param type 物品容器种类
     * @return 对应物品容器
     */
    @Nonnull
    default ProxyItemHandler.Proxy getItemHandler(@Nonnull SideHandlerType type) {
        switch (type) {
            case INPUT: return getItemHandler().new Proxy(true, false);
            case OUTPUT: return getItemHandler().new Proxy(false, true);
            case IN_OUT: return getItemHandler().new Proxy(true, true);
            default: return getItemHandler().empty;
        }
    }

    /**
     * 根据朝向直接获取物品容器
     * @param facing 朝向
     * @return 对应物品种类
     */
    @Nonnull
    default ProxyItemHandler.Proxy getItemHandler(Direction facing) {
        return getItemHandler(getItemType(facing));
    }

    @Nonnull
    @Override
    default <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            SideHandlerType type = getItemType(side);
            if (type != SideHandlerType.NONE) {
                return LazyOptional.of(() -> (T) getItemHandler(type));
            }
        }
        return LazyOptional.empty();
    }

    default void read(CompoundNBT nbt) {
        if (nbt.contains(NBT_ITEMS)) {
            CompoundNBT nbtItems = nbt.getCompound(NBT_ITEMS);
            getItemHandler().deserializeNBT(nbtItems);
        }
    }

    default CompoundNBT write(CompoundNBT nbt) {
        nbt.put(NBT_ITEMS, getItemHandler().serializeNBT());
        return nbt;
    }

    /**
     * 用于校验输入区是否可以放入某物品栈
     * @param stack 要输入的物品栈
     * @param slot 输入的槽位
     * @return true：可以放入
     */
    boolean isInputValid(int slot, int count, ItemStack stack);
}

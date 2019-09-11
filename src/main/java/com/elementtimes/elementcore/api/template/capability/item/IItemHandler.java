package com.elementtimes.elementcore.api.template.capability.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

/**
 * Mod 中使用的物品存储 IItemHandler 接口
 * @author luqin2007
 */
public interface IItemHandler extends net.minecraftforge.items.IItemHandler, IItemHandlerModifiable, INBTSerializable<NBTTagCompound> {

    /**
     * 更改某一槽位允许存储物品的数量
     * @param slot 槽位
     * @param count 数量
     */
    void setSize(int slot, int count);

    /**
     * 在该物品容器中临时增加一个物品槽
     * 在机器生命周期中，有带有流体的物品（桶等）参与反应时，会将耗尽流体的物品绑定到输出槽，使用管道输入输出时保证其可以被抽出
     * @param itemStack 绑定物品
     * @return 绑定槽位
     */
    int bind(ItemStack itemStack);

    /**
     * 解绑所有物品
     */
    void unbindAll();

    /**
     * 检查某物品是否可以存入指定槽位
     * @param slot 槽位
     * @param stack 物品
     * @return 是否可存入
     */
    boolean isItemValid(int slot, @Nonnull ItemStack stack);

    /**
     * 使用 setSlot 时，会触发 onContentsChanged 方法。使用此方法避免触发。
     * @param slot 槽位
     * @param stack 物品
     */
    void setSlotIgnoreChangeListener(int slot, ItemStack stack);

    /**
     * 使用 insertItem 时，会使用 isItemValid 检查可行性。使用此方法避免。
     * @param slot 槽位
     * @param stack 物品
     * @param simulate 模拟
     * @return 剩余
     */
    ItemStack insertItemIgnoreValid(int slot, @Nonnull ItemStack stack, boolean simulate);
}

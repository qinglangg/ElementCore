package com.elementtimes.elementcore.api.template.groups;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;

/**
 * 创造模式物品栏
 * 物品栏图标不变化
 * @author luqin2007
 */
public class ItemGroupStatic extends ItemGroup {

    private final ItemStack mIconItem;

    public ItemGroupStatic(String label, ItemStack iconItem) {
        super(label);
        mIconItem = iconItem;
    }

    public ItemGroupStatic(String label) {
        this(label, (ItemStack) null);
    }

    public ItemGroupStatic(String label, IItemProvider item) {
        this(label, new ItemStack(item));
    }

    public ItemGroupStatic(String label, IItemProvider item, CompoundNBT nbt) {
        this(label, new ItemStack(item, 1, nbt));
    }

    @Override
    public ItemStack createIcon() {
        if (mIconItem == null) {
            NonNullList<ItemStack> list = NonNullList.create();
            fill(list);
            return list.isEmpty() ? ItemStack.EMPTY : list.get(0);
        }
        return mIconItem;
    }
}

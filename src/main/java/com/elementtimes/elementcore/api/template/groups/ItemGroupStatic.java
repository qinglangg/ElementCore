package com.elementtimes.elementcore.api.template.groups;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * 创造模式物品栏
 * 物品栏图标不变化
 * @author luqin2007
 */
public class ItemGroupStatic extends ItemGroup {

    private ItemStack mIconItem;

    public ItemGroupStatic(String label, ItemStack iconItem) {
        super(label);
        mIconItem = iconItem;
    }

    public ItemGroupStatic(String label) {
        this(label, (ItemStack) null);
    }

    public ItemGroupStatic(String label, Item item) {
        this(label, new ItemStack(item));
    }

    public ItemGroupStatic(String label, Block block) {
        this(label, new ItemStack(block));
    }

    @Override
    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public ItemStack createIcon() {
        if (mIconItem == null) {
            NonNullList<ItemStack> list = NonNullList.create();
            fill(list);
            mIconItem = list.isEmpty() ? ItemStack.EMPTY : list.get(0);
        }
        return mIconItem;
    }
}

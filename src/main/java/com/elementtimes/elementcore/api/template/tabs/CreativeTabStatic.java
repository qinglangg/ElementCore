package com.elementtimes.elementcore.api.template.tabs;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * 创造模式物品栏
 * 物品栏图标不变化
 * @author luqin2007
 */
public class CreativeTabStatic extends CreativeTabs {

    private final ItemStack mIconItem;

    public CreativeTabStatic(String label, ItemStack iconItem) {
        super(label);
        mIconItem = iconItem;
    }

    public CreativeTabStatic(String label) {
        this(label, (ItemStack) null);
    }

    public CreativeTabStatic(String label, Item item) {
        this(label, new ItemStack(item));
    }

    public CreativeTabStatic(String label, Item item, int meta) {
        this(label, new ItemStack(item, 1, meta));
    }

    public CreativeTabStatic(String label, Block block) {
        this(label, new ItemStack(block));
    }

    public CreativeTabStatic(String label, Block block, int meta) {
        this(label, new ItemStack(block, 1, meta));
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    public ItemStack getTabIconItem() {
        if (mIconItem == null) {
            NonNullList<ItemStack> list = NonNullList.create();
            displayAllRelevantItems(list);
            return list.isEmpty() ? ItemStack.EMPTY : list.get(0);
        }
        return mIconItem;
    }
}

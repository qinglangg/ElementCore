package com.elementtimes.elementcore.common.tab;

import com.elementtimes.elementcore.ElementCore;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * 主 CreativeTab
 * @author luqin2007
 */
public class MainTab extends CreativeTabs {

    public MainTab() {
        super("elementcore.main");
    }

    @Override
    @Nonnull
    public ItemStack getTabIconItem() {
        if (System.currentTimeMillis() % 5000 > 2500) {
            return new ItemStack(ElementCore.Items.debugger, 1, 0b0000);
        } else {
            return new ItemStack(ElementCore.Items.debugger, 1, 0b0001);
        }
    }
}

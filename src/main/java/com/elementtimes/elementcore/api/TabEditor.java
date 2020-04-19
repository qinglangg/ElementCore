package com.elementtimes.elementcore.api;

import com.elementtimes.elementcore.api.annotation.tools.ModTabEditorFunc;
import com.elementtimes.elementcore.api.common.ECModContainer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * @author luqin2007
 */
public class TabEditor {

    @ModTabEditorFunc
    public static void onEnchantmentBookAdd(CreativeTabs tab, NonNullList<ItemStack> items) {
        ECModContainer.MODS.values().forEach(container -> {
            container.elements.enchantmentBooks.forEach(wrapper -> wrapper.apply(tab, items));
        });
    }

    @ModTabEditorFunc
    public static void onPotionBottleAdd(CreativeTabs tabs, NonNullList<ItemStack> items) {
        ECModContainer.MODS.values().forEach(container -> {
            container.elements.potionBottles.forEach(wrapper -> wrapper.apply(tabs, items));
        });
    }
}

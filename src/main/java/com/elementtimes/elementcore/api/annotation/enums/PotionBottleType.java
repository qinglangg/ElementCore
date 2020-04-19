package com.elementtimes.elementcore.api.annotation.enums;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;

import java.util.Set;
import java.util.function.Supplier;

/**
 * 药水类型
 * @author luqin2007
 */
public enum PotionBottleType {
    /**
     * 普通药水
     */
    NORMAL(Items.POTIONITEM),
    /**
     * 喷溅药水
     */
    SPLASH(Items.SPLASH_POTION),
    /**
     * 滞留药水
     */
    LINGERING(Items.LINGERING_POTION);

    private final Item item;

    PotionBottleType(Item item) {
        this.item = item;
    }

    public void applyTo(NonNullList<ItemStack> items, Set<PotionType> effects) {
        for (PotionType effect : effects) {
            ItemStack stack = PotionUtils.addPotionToItemStack(new ItemStack(item), effect);
            items.add(stack);
        }
    }
}

package com.elementtimes.elementcore.api.annotation.enums;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;

/**
 * 药水类型
 * @author luqin2007
 */
public enum PotionBottleType {
    /**
     * 普通药水
     */
    NORMAL(Items.POTION),
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

    public void applyTo(NonNullList<ItemStack> items, Potion potion) {
        ItemStack stack = PotionUtils.addPotionToItemStack(new ItemStack(item), potion);
        items.add(stack);
    }
}

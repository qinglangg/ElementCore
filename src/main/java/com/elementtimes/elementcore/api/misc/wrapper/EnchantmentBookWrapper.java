package com.elementtimes.elementcore.api.misc.wrapper;

import com.elementtimes.elementcore.api.annotation.enums.EnchantmentBook;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemGroup;

public class EnchantmentBookWrapper {
    public final EnchantmentBook type;
    public final Enchantment enchantment;
    public final ItemGroup group;
    public EnchantmentBookWrapper(ItemGroup group, EnchantmentBook type, Enchantment enchantment) {
        this.group = group;
        this.type = type;
        this.enchantment = enchantment;
    }
}
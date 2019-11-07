package com.elementtimes.elementcore.api.utils;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 附魔
 * @author luqin2007
 */
public class EnchantmentUtils {

    private static EnchantmentUtils u = null;
    public static EnchantmentUtils getInstance() {
        if (u == null) {
            u = new EnchantmentUtils();
        }
        return u;
    }

    /**
     * 获取适用于某一物品的所有附魔
     * @param itemStack 要查找的物品
     * @return 所有可被附魔到该物品的附魔
     */
    public List<Enchantment> getSuitableEnchantments(ItemStack itemStack) {
        return Registry.ENCHANTMENT.stream()
                .filter(enchantment -> enchantment.canApply(itemStack))
                .collect(Collectors.toList());
    }

    /**
     * 为某一物品附魔所有非诅咒的最大等级
     * @param itemStack 要附魔的物品
     */
    public void addMaxEnchantments(ItemStack itemStack) {
        EnchantmentHelper.setEnchantments(
                getSuitableEnchantments(itemStack).stream()
                        .filter(enchantment -> !enchantment.isCurse())
                        .map(enchantment -> new ImmutablePair<>(enchantment, enchantment.getMaxLevel()))
                        .collect(Collectors.toMap(ImmutablePair::getKey, ImmutablePair::getValue)),
                itemStack
        );
    }

}

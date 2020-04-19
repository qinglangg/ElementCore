package com.example.examplemod.enchantment;

import com.elementtimes.elementcore.api.annotation.ModEnchantment;
import com.elementtimes.elementcore.api.annotation.tools.ModTooltips;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;

@ModEnchantment
@ModTooltips("Tooltips: Enchantment test2")
public class TestEnchantment2 extends Enchantment {

    private int maxLevel;

    protected TestEnchantment2(int maxLevel) {
        super(Rarity.RARE, EnchantmentType.ALL, EquipmentSlotType.values());
        this.maxLevel = maxLevel;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }
}

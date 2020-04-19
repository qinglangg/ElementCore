package com.example.examplemod.enchantment;

import com.elementtimes.elementcore.api.annotation.ModEnchantment;
import com.elementtimes.elementcore.api.annotation.tools.ModTooltips;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;

@ModEnchantment
@ModTooltips("Tooltips: Enchantment test")
public class TestEnchantment extends Enchantment {

    protected TestEnchantment() {
        super(Rarity.RARE, EnchantmentType.ALL, EquipmentSlotType.values());
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }
}

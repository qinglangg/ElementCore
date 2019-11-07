package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.LoaderHelper;
import com.elementtimes.elementcore.api.annotation.ModEnchantment;
import net.minecraft.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentLoader {

    private boolean isElementLoaded = false;
    private ECModElements mElements;

    List<Enchantment> enchantments = new ArrayList<>();

    public EnchantmentLoader(ECModElements elements) {
        mElements = elements;
    }

    public List<Enchantment> enchantments() {
        if (!isElementLoaded) {
            mElements.elements.load();
            loadEnchantment();
        }
        return enchantments;
    }

    private void loadEnchantment() {
        LoaderHelper.stream(mElements, ModEnchantment.class).forEach(data -> LoaderHelper.loadClass(mElements, data.getClassType().getClassName())
                .flatMap(clazz -> ECUtils.reflect.getField(clazz, data.getMemberName(), null, Enchantment.class, mElements.logger))
                .ifPresent(enchantment -> {
                    LoaderHelper.regName(mElements, enchantment, LoaderHelper.getDefault(data, data.getMemberName()));
                    enchantments.add(enchantment);
                }));
        isElementLoaded = true;
    }
}

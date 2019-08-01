package com.elementtimes.elementcore.annotation.processor;

import com.elementtimes.elementcore.annotation.AnnotationInitializer;
import com.elementtimes.elementcore.annotation.annotations.ModEnchantment;
import com.elementtimes.elementcore.util.ReflectUtil;
import net.minecraft.enchantment.Enchantment;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

/**
 * 处理附魔
 * @author luqin2007
 */
public class ModEnchantmentLoader {

    public static void load(AnnotationInitializer initializer) {
        initializer.elements.get(ModEnchantment.class).forEach(element -> buildEnchantment(initializer, element));
    }

    private static void buildEnchantment(AnnotationInitializer initializer, AnnotatedElement element) {
        Optional<Enchantment> enchantmentOptional = ReflectUtil.getFromAnnotated(element, null, initializer)
                .filter(obj -> obj instanceof Enchantment)
                .map(obj -> (Enchantment) obj);
        String name = ReflectUtil.getName(element).orElse("???");
        ModEnchantment info = element.getAnnotation(ModEnchantment.class);
        if (enchantmentOptional.isPresent()) {
            Enchantment enchantment = enchantmentOptional.get();
            initEnchantment(initializer.modInfo.modid, enchantment, info, name);
            initializer.enchantments.add(enchantment);
        } else {
            initializer.warn("Element {} is not an Enchantment", name, element);
        }
    }

    private static void initEnchantment(String modid, Enchantment enchantment, ModEnchantment info, String name) {
        if (enchantment.getRegistryName() == null) {
            String iName = info.value();
            if (iName.isEmpty()) {
                enchantment.setRegistryName(modid, name.toLowerCase());
            } else {
                enchantment.setRegistryName(modid, iName);
            }
        }
    }
}

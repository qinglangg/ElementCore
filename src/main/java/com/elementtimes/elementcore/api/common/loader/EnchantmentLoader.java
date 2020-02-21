package com.elementtimes.elementcore.api.common.loader;

import com.elementtimes.elementcore.api.annotation.ModEnchantment;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import java.util.Map;

public class EnchantmentLoader {

    public static void load(ECModElements elements) {
        ObjHelper.stream(elements, ModEnchantment.class).forEach(data -> {
            ObjHelper.findClass(elements, data.getClassName()).ifPresent(clazz -> {
                Map<String, Object> info = data.getAnnotationInfo();
                newEnchantment(elements, data.getClassName(), data.getObjectName(),
                        ObjHelper.getDefault(data), (String) info.get("name"));
            });
        });
    }

    public static Enchantment newEnchantment(ECModElements elements, String className, String objectName,
                                             String registerName, String enchantmentName) {
        Enchantment enchantment = ObjHelper.findClass(elements, className).
                flatMap(aClass -> ECUtils.reflect.get(aClass, objectName, null, Enchantment.class, elements)).
                orElseGet(() -> new Enchantment(Enchantment.Rarity.COMMON, EnumEnchantmentType.ALL, new EntityEquipmentSlot[0]) {});
        String register = StringUtils.isNullOrEmpty(registerName) ? objectName : registerName;
        if (enchantment.getRegistryName() == null) {
            if (register.contains(":")) {
                enchantment.setRegistryName(new ResourceLocation(register.toLowerCase()));
            } else {
                enchantment.setRegistryName(new ResourceLocation(elements.container.id(), register.toLowerCase()));
            }
        }
        if ("enchantment.null".equals(enchantment.getName())) {
            if (StringUtils.isNullOrEmpty(enchantmentName)) {
                enchantment.setName(elements.container.id() + "." + objectName.toLowerCase());
            } else {
                enchantment.setName(enchantment.getRegistryName().getResourceDomain() + "." + enchantmentName.toLowerCase());
            }
        }
        elements.enchantments.add(enchantment);
        return enchantment;
    }

}

package com.elementtimes.elementcore.api.common.loader;

import com.elementtimes.elementcore.api.annotation.ModPotion;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import java.util.Map;

/**
 * @author luqin2007
 */
public class PotionLoader {

    public static void load(ECModElements elements) {
        ObjHelper.stream(elements, ModPotion.class).forEach(data -> {
            ObjHelper.findClass(elements, data.getClassName()).ifPresent(aClass -> {
                Map<String, Object> info = data.getAnnotationInfo();
                String name = (String) info.get("name");
                newPotion(elements, data.getClassName(), data.getObjectName(), ObjHelper.getDefault(data), name);
            });
        });
    }

    public static Potion newPotion(ECModElements elements, String className, String objectName,
                                   String registerName, String potionName) {
        Potion potion = ObjHelper.findClass(elements, className).
                flatMap(aClass -> ECUtils.reflect.get(aClass, objectName, null, Potion.class, elements)).
                orElseGet(() -> new Potion(false, 0) {});
        String register = StringUtils.isNullOrEmpty(registerName) ? objectName : registerName;
        if (potion.getRegistryName() == null) {
            if (register.contains(":")) {
                potion.setRegistryName(new ResourceLocation(register.toLowerCase()));
            } else {
                potion.setRegistryName(new ResourceLocation(elements.container.id(), register.toLowerCase()));
            }
        }
        if (StringUtils.isNullOrEmpty(potion.getName())) {
            String name = StringUtils.isNullOrEmpty(potionName) ? objectName : potionName;
            potion.setPotionName(name.toLowerCase());
        }
        elements.potions.add(potion);
        return potion;
    }

}

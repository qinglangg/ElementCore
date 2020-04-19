package com.elementtimes.elementcore.api.common.loader;

import com.elementtimes.elementcore.api.annotation.ModPotion;
import com.elementtimes.elementcore.api.annotation.enums.PotionBottleType;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author luqin2007
 */
public class PotionLoader {

    public static void load(ECModElements elements) {
        loadPotion(elements);
        loadPotionType(elements);
        loadPotionBottle(elements);
    }

    private static void loadPotion(ECModElements elements) {
        ObjHelper.stream(elements, ModPotion.class).forEach(data -> {
            ObjHelper.find(elements, Potion.class, data).ifPresent(potion -> {
                Map<String, Object> info = data.getAnnotationInfo();
                String name = (String) info.get("name");
                String className = data.getClassName();
                String objectName = data.getObjectName();
                objectName = (StringUtils.isNullOrEmpty(objectName) || objectName.contains("."))
                        ? className.substring(className.indexOf(".")) : objectName;
                String registerName = ObjHelper.getDefault(data);
                String register = StringUtils.isNullOrEmpty(registerName) ? objectName : registerName;
                if (potion.getRegistryName() == null) {
                    if (register.contains(":")) {
                        potion.setRegistryName(new ResourceLocation(register.toLowerCase()));
                    } else {
                        potion.setRegistryName(new ResourceLocation(elements.container.id(), register.toLowerCase()));
                    }
                }
                if (StringUtils.isNullOrEmpty(potion.getName())) {
                    name = StringUtils.isNullOrEmpty(name) ? objectName : name;
                    potion.setPotionName(name.toLowerCase());
                }
                elements.warn("[ModPotion]{}{}", potion.getName(), potion.isBadEffect() ? "(BAD)" : "");
                elements.potions.add(potion);
            });
        });
    }

    private static void loadPotionType(ECModElements elements) {
        ObjHelper.stream(elements, ModPotion.class).forEach(data -> {
            ObjHelper.find(elements, Potion.class, data).ifPresent(potion -> {
                Map<String, Object> typeMap = (Map<String, Object>) data.getAnnotationInfo().get("withType");
                if (typeMap != null && !typeMap.isEmpty()) {
                    String baseName = (String) typeMap.get("baseName");
                    String registryName = (String) typeMap.get("registryName");
                    List<Map<String, Object>> effectInfos = (List<Map<String, Object>>) typeMap.getOrDefault("effects", Collections.emptyList());
                    PotionEffect[] effects = effectInfos.stream().map(effectMap -> {
                        int duration = (int) effectMap.getOrDefault("duration", 0);
                        int amplifier = (int) effectMap.getOrDefault("amplifier", 0);
                        boolean ambient = (boolean) effectMap.getOrDefault("ambient", false);
                        boolean showParticles = (boolean) effectMap.getOrDefault("showParticles", true);
                        return new PotionEffect(potion, duration, amplifier, ambient, showParticles);
                    }).toArray(PotionEffect[]::new);
                    PotionType type = new PotionType(baseName, effects);
                    type.setRegistryName(elements.container.id(), StringUtils.isNullOrEmpty(registryName) ? potion.getName() : registryName);
                    elements.potionTypes.add(type);
                    elements.warn("[ModPotion(type)]{}", type.getRegistryName());
                    for (PotionEffect effect : type.getEffects()) {
                        elements.warn("[ModPotion(type)] -> effect duration={}, amplifier={}{}{}",
                                effect.getDuration(), effect.getAmplifier(),
                                effect.getIsAmbient() ? ", ambient" : "",
                                effect.doesShowParticles() ? ", showParticles" : "");
                    }
                }
            });
        });
    }

    private static void loadPotionBottle(ECModElements elements) {
        ObjHelper.stream(elements, ModPotion.Bottles.class).forEach(data -> {
            ObjHelper.find(elements, Potion.class, data).ifPresent(potion -> {
                String tab = ObjHelper.getDefault(data, "misc");
                List<PotionBottleType> types;
                List<ModAnnotation.EnumHolder> typeHolders = (List<ModAnnotation.EnumHolder>) data.getAnnotationInfo().get("types");
                if (typeHolders == null || typeHolders.isEmpty()) {
                    types = Collections.singletonList(PotionBottleType.NORMAL);
                } else {
                    types = typeHolders.stream()
                            .map(ModAnnotation.EnumHolder::getValue)
                            .map(PotionBottleType::valueOf)
                            .collect(Collectors.toList());
                }
                elements.warn("[ModPotion.Bottles]{} tab={}", potion.getName(), tab);
                types.forEach(type -> elements.warn("[ModPotion.Bottles] -> {}", type));
                elements.potionBottles.add(new PotionBottle(elements, potion, tab, types));
            });
        });
    }

    public static class PotionBottle {

        public final String tabKey;
        public final ECModElements elements;
        public final Potion potion;
        public final List<PotionBottleType> bottleTypes;
        public CreativeTabs tab = null;
        public Set<PotionType> types = null;
        public int count = 0;

        public PotionBottle(ECModElements elements, Potion potion, String tab, List<PotionBottleType> types) {
            this.tabKey = tab;
            this.elements = elements;
            this.potion = potion;
            this.bottleTypes = types;
        }

        public void apply(CreativeTabs creativeTab, NonNullList<ItemStack> items) {
            if (this.tab == null) {
                ObjHelper.findTab(elements, tabKey).ifPresent(t -> tab = t);
            }
            if (this.tab == creativeTab) {
                if (types == null || types.isEmpty()) {
                    types = new HashSet<>();
                    for (ResourceLocation key : PotionType.REGISTRY.getKeys()) {
                        PotionType type = PotionType.REGISTRY.getObject(key);
                        for (PotionEffect effect : type.getEffects()) {
                            if (effect.getPotion() == potion) {
                                types.add(type);
                                break;
                            }
                        }
                    }
                }
                for (PotionBottleType type : bottleTypes) {
                    type.applyTo(items, types);
                }
            }
        }
    }
}

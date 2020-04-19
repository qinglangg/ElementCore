package com.elementtimes.elementcore.api.common.loader;

import com.elementtimes.elementcore.api.annotation.ModItem;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import java.util.Map;

public class ItemLoader {

    public static void loader(ECModElements elements) {
        loadItem(elements);
        loadItemRetain(elements);
        loadItemSub(elements);
        loadItemDamageable(elements);
    }

    private static void loadItem(ECModElements elements) {
        ObjHelper.stream(elements, ModItem.class).forEach(data -> {
            ObjHelper.findClass(elements, data.getClassName()).ifPresent(aClass -> {
                Map<String, Object> info = data.getAnnotationInfo();
                String registerName = (String) info.get("registerName");
                String unlocalizedName = (String) info.get("unlocalizedName");
                Item item = newItem(elements, data.getClassName(), data.getObjectName(), registerName, unlocalizedName);
                String tabKey = (String) info.get("creativeTabKey");
                ObjHelper.findTab(elements, tabKey).ifPresent(item::setCreativeTab);
                elements.warn("[ModItem]{} tab={}", item.getRegistryName(), tabKey);
            });
        });
    }

    private static void loadItemRetain(ECModElements elements) {
        ObjHelper.stream(elements, ModItem.RetainInCrafting.class).forEach(data -> {
            ObjHelper.find(elements, Item.class, data).ifPresent(item -> {
                item.setContainerItem(item);
                elements.warn("[ModItem.RetainInCrafting]{}", item.getRegistryName());
            });
        });
    }

    private static void loadItemSub(ECModElements elements) {
        ObjHelper.stream(elements, ModItem.HasSubItem.class).forEach(data -> {
            ObjHelper.find(elements, Item.class, data).ifPresent(item -> {
                item.setHasSubtypes(true);
                item.setMaxDamage(0);
                item.setNoRepair();
                elements.warn("[ModItem.HasSubItem]{}", item.getRegistryName());
            });
        });
    }

    private static void loadItemDamageable(ECModElements elements) {
        ObjHelper.stream(elements, ModItem.Damageable.class).forEach(data -> {
            ObjHelper.find(elements, Item.class, data).ifPresent(item -> {
                item.setMaxDamage(ObjHelper.getDefault(data, 0));
                item.setMaxStackSize(1);
                boolean noRepair = (boolean) data.getAnnotationInfo().getOrDefault("noRepair", false);
                if (noRepair) {
                    item.setNoRepair();
                }
                elements.warn("[ModItem.Damageable]{}{}", item.getRegistryName(), noRepair ? " noRepair" : "");
            });
        });
    }

    public static Item newItem(ECModElements elements, String className, String objectName,
                               String registerName, String unlocalizedName) {
        Item item = ObjHelper.findClass(elements, className).
                flatMap(aClass -> ECUtils.reflect.get(aClass, objectName, null, Item.class, elements)).
                orElseGet(Item::new);
        String register = StringUtils.isNullOrEmpty(registerName) ? objectName : registerName;
        if (item.getRegistryName() == null) {
            if (register.contains(":")) {
                item.setRegistryName(new ResourceLocation(register.toLowerCase()));
            } else {
                item.setRegistryName(new ResourceLocation(elements.container.id(), register.toLowerCase()));
            }
        }
        if ("item.null".equals(item.getUnlocalizedName())) {
            if (StringUtils.isNullOrEmpty(unlocalizedName)) {
                item.setUnlocalizedName(elements.container.id() + "." + objectName.toLowerCase());
            } else {
                item.setUnlocalizedName(item.getRegistryName().getResourceDomain() + "." + unlocalizedName.toLowerCase());
            }
        }
        elements.items.add(item);
        return item;
    }

}

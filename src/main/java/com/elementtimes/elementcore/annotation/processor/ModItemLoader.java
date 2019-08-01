package com.elementtimes.elementcore.annotation.processor;

import com.elementtimes.elementcore.annotation.AnnotationInitializer;
import com.elementtimes.elementcore.annotation.annotations.ModItem;
import com.elementtimes.elementcore.annotation.annotations.ModTags;
import com.elementtimes.elementcore.util.ReflectUtil;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

/**
 * 加载物品
 * 处理所有 ModItem 注解的成员
 *
 * @author luqin2007
 */
public class ModItemLoader {

    public static void load(AnnotationInitializer initializer) {
        initializer.elements.get(ModItem.class).forEach(element -> buildItem(initializer, element));
    }

    private static void buildItem(AnnotationInitializer initializer, AnnotatedElement itemHolder) {
        // object
        final ModItem info = itemHolder.getAnnotation(ModItem.class);
        if (info == null) {
            return;
        }

        Item item = createItem(initializer, itemHolder, info);
        // 矿辞
        initItemTags(initializer, item, itemHolder);
        initializer.items.add(item);
    }

    private static Item createItem(AnnotationInitializer initializer, AnnotatedElement itemHolder, ModItem info) {
        Item defaultValue = new Item(new Item.Properties());
        Item item = ReflectUtil.getFromAnnotated(itemHolder, defaultValue, initializer).orElse(defaultValue);

        String defaultName;
        if (itemHolder instanceof Class) {
            defaultName = ((Class) itemHolder).getSimpleName().toLowerCase();
        } else if (itemHolder instanceof Field) {
            defaultName = ((Field) itemHolder).getName().toLowerCase();
        } else {
            defaultName = null;
        }

        String registryName = info.registerName();
        if (registryName.isEmpty()) {
            registryName = defaultName;
        }
        if (item.getRegistryName() == null) {
            if (registryName == null) {
                initializer.warn("Item {} don't have a RegisterName. It's a Bug!!!", item);
            } else {
                item.setRegistryName(registryName);
            }
        }

        return item;
    }

    private static void initItemTags(AnnotationInitializer initializer, Item item, AnnotatedElement itemHolder) {
        ModTags tags = itemHolder.getAnnotation(ModTags.class);
        if (tags != null) {
            ResourceLocation key = new ResourceLocation(tags.value());
            Tag.Builder<Item> builder = initializer.itemTags.get(key);
            if (builder == null) {
                builder = new Tag.Builder<>();
            }
            builder.add(item);
            initializer.itemTags.put(key, builder);
        }
    }
}

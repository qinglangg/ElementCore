package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.LoaderHelper;
import com.elementtimes.elementcore.api.annotation.ModItem;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.item.Item;

import java.util.*;

public class ItemLoader {

    private boolean isItemLoaded = false;
    private boolean isItemTagLoaded = false;
    private boolean isItemBurningTimeLoaded = false;
    private ECModElements mElements;

    Map<String, List<Item>> tags = new HashMap<>();
    Map<String, Item> items = new HashMap<>();
    Object2IntMap<String> burningTimes = new Object2IntArrayMap<>();

    public ItemLoader(ECModElements elements) {
        mElements = elements;
    }

    public Map<String, Item> items() {
        if (!isItemLoaded) {
            mElements.elements.load();
            loadItem();
        }
        return items;
    }

    private void loadItem() {
        LoaderHelper.stream(mElements, ModItem.class).forEach(data -> {
            String className = data.getClassType().getClassName();
            LoaderHelper.loadClass(mElements, className).ifPresent(clazz -> {
                String memberName = data.getMemberName();
                ECUtils.reflect.getField(clazz, memberName, null, Item.class, mElements.logger).ifPresent(item -> {
                    LoaderHelper.regName(mElements, item, LoaderHelper.getDefault(data, memberName));
                    items.put(className + "." + memberName, item);
                });
            });
        });
        isItemLoaded = true;
    }

    public Map<String, List<Item>> tags() {
        if (!isItemTagLoaded) {
            loadTags();
        }
        return tags;
    }

    private void loadTags() {
        LoaderHelper.stream(mElements, ModItem.Tags.class).forEach(data -> {
            String memberName = data.getMemberName();
            LoaderHelper.getItem(mElements, data.getClassType().getClassName(), memberName).ifPresent(item -> {
                for (String tag : LoaderHelper.getDefault(data, Collections.singletonList("forge:" + memberName))) {
                    tags.computeIfAbsent(tag.toLowerCase(), (s) -> new ArrayList<>()).add(item);
                }
            });
        });
        isItemTagLoaded = true;
    }

    public Object2IntMap<String> burningTimes() {
        if (!isItemBurningTimeLoaded) {
            loadBurningTimes();
        }
        return burningTimes;
    }

    private void loadBurningTimes() {
        LoaderHelper.stream(mElements, ModItem.BurningTime.class).forEach(data -> {
            LoaderHelper.getItem(mElements, data.getClassType().getClassName(), data.getMemberName()).ifPresent(item -> {
                burningTimes.put(item.getRegistryName().toString(), LoaderHelper.getDefault(data));
            });
        });
        isItemBurningTimeLoaded = true;
    }
}

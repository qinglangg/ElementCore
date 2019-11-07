package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.LoaderHelper;
import com.elementtimes.elementcore.api.annotation.ModItemGroup;
import net.minecraft.item.ItemGroup;

import java.util.HashMap;
import java.util.Map;

public class ItemGroupLoader {

    private boolean isGroupLoaded = false;
    private ECModElements mElements;

    Map<String, ItemGroup> groups = new HashMap<>();

    public ItemGroupLoader(ECModElements elements) {
        mElements = elements;
    }

    public Map<String, ItemGroup> groups() {
        if (!isGroupLoaded) {
            mElements.elements.load();
            loadGroups();
        }
        return groups;
    }

    private void loadGroups() {
        LoaderHelper.stream(mElements, ModItemGroup.class).forEach(data -> {
            LoaderHelper.loadClass(mElements, data.getClassType().getClassName()).ifPresent(clazz -> {
                ECUtils.reflect.getField(clazz, data.getMemberName(), null, ItemGroup.class, mElements.logger).ifPresent(group -> {
                    groups.put(LoaderHelper.getDefault(data, data.getMemberName()), group);
                });
            });
        });
        isGroupLoaded = true;
    }
}

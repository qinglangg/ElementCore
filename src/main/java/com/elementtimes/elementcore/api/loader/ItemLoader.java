package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.annotation.ModItem;
import com.elementtimes.elementcore.api.annotation.part.Parts;
import com.elementtimes.elementcore.api.helper.FindOptions;
import com.elementtimes.elementcore.api.helper.ObjHelper;
import com.elementtimes.elementcore.api.utils.CommonUtils;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.annotation.ElementType;
import java.util.Map;

public class ItemLoader {

    public static void load(ECModElements elements) {
        loadItem(elements);
    }

    private static void loadItem(ECModElements elements) {
        ObjHelper.stream(elements, ModItem.class).forEach(data -> {
            Map<String, Object> map = data.getAnnotationData();
            FindOptions<Item> options = new FindOptions<>(Item.class, ElementType.FIELD, ElementType.TYPE);
            ObjHelper.find(elements, data, options).ifPresent(item -> {
                String name = ObjHelper.getDefault(data);
                ObjHelper.setRegisterName(item, name, data, elements);
                elements.items.add(item);
                ObjHelper.saveResult(options, elements.generatedItems);
                if (CommonUtils.isClient()) {
                    loadItemColor(elements, item, map);
                }
            });
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void loadItemColor(ECModElements elements, Item item, Map<String, Object> dataMap) {
        Parts.colorItem(dataMap.get("color"), elements).ifPresent(wrapper -> {
            elements.itemColors.add(wrapper.bind(item));
        });
    }
}

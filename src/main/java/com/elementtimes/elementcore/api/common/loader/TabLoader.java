package com.elementtimes.elementcore.api.common.loader;

import com.elementtimes.elementcore.api.annotation.ModTab;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import net.minecraft.creativetab.CreativeTabs;

/**
 * @author luqin2007
 */
public class TabLoader {

    public static void load(ECModElements elements) {
        ObjHelper.stream(elements, ModTab.class).forEach(data -> {
            String key = ObjHelper.getDefault(data, data.getObjectName());
            ObjHelper.findClass(elements, data.getClassName())
                    .flatMap(aClass -> ECUtils.reflect.get(aClass, data.getObjectName(), null, CreativeTabs.class, elements))
                    .ifPresent(creativeTabs -> elements.tabs.put(key, creativeTabs));
        });
    }
}

package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.annotation.enums.ValueType;
import com.elementtimes.elementcore.api.annotation.part.Parts;
import com.elementtimes.elementcore.api.annotation.tools.ModBurnTime;
import com.elementtimes.elementcore.api.annotation.tools.ModTooltips;
import com.elementtimes.elementcore.api.helper.FindOptions;
import com.elementtimes.elementcore.api.helper.ObjHelper;
import com.elementtimes.elementcore.api.misc.wrapper.AnnotationGetter;
import com.elementtimes.elementcore.api.misc.wrapper.AnnotationMethod;
import com.elementtimes.elementcore.api.misc.wrapper.BurnTimeWrapper;
import com.elementtimes.elementcore.api.misc.wrapper.TooltipsWrapper;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.annotation.ElementType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ToolLoader {

    public static void load(ECModElements elements) {
        loadTooltips(elements);
        loadBurnTime(elements);
    }

    private static void loadTooltips(ECModElements elements) {
        ObjHelper.stream(elements, ModTooltips.class).forEach(data -> {
            Map<String, Object> map = data.getAnnotationData();
            if (map != null && !map.isEmpty()) {
                switch (ObjHelper.getEnum(ValueType.class, map.get("type"), ValueType.VALUE)) {
                    case VALUE:
                        List<String> strings = ObjHelper.getDefault(data, Collections.emptyList());
                        if (!strings.isEmpty()) {
                            elements.tooltips.add(new TooltipsWrapper(strings, getTestObject(elements, data)));
                        }
                        break;
                    case METHOD:
                        AnnotationMethod method = Parts.method(elements, map.get("method"), ItemTooltipEvent.class);
                        if (method.hasContent()) {
                            elements.tooltips.add(new TooltipsWrapper((Consumer<ItemTooltipEvent>) method::invoke, getTestObject(elements, data)));
                        }
                        break;
                    case OBJECT:
                        Parts.getter(elements, map.get("object")).get()
                                .ifPresent(obj -> elements.tooltips.add(new TooltipsWrapper(obj, getTestObject(elements, data))));
                        break;
                    default:
                }
            }
        });
    }

    private static void loadBurnTime(ECModElements elements) {
        ObjHelper.stream(elements, ModBurnTime.class).forEach(data -> {
            Map<String, Object> map = data.getAnnotationData();
            if (map != null && !map.isEmpty()) {
                switch (ObjHelper.getEnum(ValueType.class, map.get("type"), ValueType.VALUE)) {
                    case VALUE:
                        int value = ObjHelper.getDefault(data, 0);
                        elements.burnTimes.add(new BurnTimeWrapper(value, getTestObject(elements, data)));
                        break;
                    case METHOD:
                        AnnotationMethod method = Parts.method(elements, map.get("method"), FurnaceFuelBurnTimeEvent.class);
                        elements.burnTimes.add(new BurnTimeWrapper(method, getTestObject(elements, data)));
                        break;
                    case OBJECT:
                        AnnotationGetter getter = Parts.getter(elements, map.get("object"));
                        elements.burnTimes.add(new BurnTimeWrapper(getter, getTestObject(elements, data)));
                        break;
                    default:
                }
            }
        });
    }

    private static Object getTestObject(ECModElements elements, ModFileScanData.AnnotationData data) {
        if (data.getTargetType() == ElementType.FIELD) {
            return ObjHelper.find(elements, data, new FindOptions<>(Object.class, ElementType.FIELD)).orElse(null);
        } else {
            return ObjHelper.findClass(elements, data.getClassType()).orElse(null);
        }
    }
}

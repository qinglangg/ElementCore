package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.annotation.ModKey;
import com.elementtimes.elementcore.api.annotation.part.Parts;
import com.elementtimes.elementcore.api.helper.FindOptions;
import com.elementtimes.elementcore.api.helper.ObjHelper;
import com.elementtimes.elementcore.api.misc.wrapper.AnnotationMethod;
import com.elementtimes.elementcore.api.misc.wrapper.KeyWrapper;
import com.elementtimes.elementcore.api.utils.CommonUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.annotation.ElementType;

public class KeyLoader {

    public static void load(ECModElements elements) {
        if (CommonUtils.isClient()) {
            loadKey(elements);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void loadKey(ECModElements elements) {
        Class<?> keyClass = net.minecraft.client.settings.KeyBinding.class;
        Class<?> keyEventClass = net.minecraftforge.client.event.InputEvent.KeyInputEvent.class;
        ObjHelper.stream(elements, ModKey.class).forEach(data -> {
            ObjHelper.find(elements, data, new FindOptions<>(keyClass, ElementType.FIELD)).ifPresent(key -> {
                AnnotationMethod method = Parts.method(elements, ObjHelper.getDefault(data), keyEventClass, keyClass);
                elements.keys.add(new KeyWrapper(key, (a, b) -> method.invoke(a, b)));
            });
        });
    }
}

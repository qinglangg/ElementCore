package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.annotation.ModEventNetwork;
import com.elementtimes.elementcore.api.annotation.ModSimpleNetwork;
import com.elementtimes.elementcore.api.annotation.part.Parts;
import com.elementtimes.elementcore.api.helper.ObjHelper;
import com.elementtimes.elementcore.api.misc.wrapper.AnnotationMethod;
import com.elementtimes.elementcore.api.misc.wrapper.NetEventWrapper;
import com.elementtimes.elementcore.api.misc.wrapper.NetSimpleWrapper;
import com.elementtimes.elementcore.api.utils.ReflectUtils;
import net.minecraft.network.PacketBuffer;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author luqin2007
 */
public class NetworkLoader {

    public static void load(ECModElements elements) {
        if (elements.simpleChannel != null) {
            loadSimple(elements);
        }
        if (elements.eventChannel != null) {
            loadEvent(elements);
        }
    }

    private static void loadSimple(ECModElements elements) {
        ObjHelper.stream(elements, ModSimpleNetwork.class).forEach(data -> {
            ObjHelper.findClass(elements, data.getClassType()).ifPresent(aClass -> {
                Map<String, Object> map = data.getAnnotationData();
                AnnotationMethod encoder = Parts.method(elements, map.get("encoder"), aClass, PacketBuffer.class);
                if (!encoder.hasContent()) {
                    elements.warn("[{}]Can't find encoder method {}", elements.container.id(), encoder.getRefName());
                    return;
                }
                AnnotationMethod decoder = Parts.method(elements, map.get("decoder"), PacketBuffer.class);
                if (!encoder.hasContent()) {
                    elements.warn("[{}]Can't find decoder method {}", elements.container.id(), decoder.getRefName());
                    return;
                }
                AnnotationMethod handler = Parts.method(elements, map.get("handler"), aClass, Supplier.class);
                if (!encoder.hasContent()) {
                    elements.warn("[{}]Can't find handler method {}", elements.container.id(), handler.getRefName());
                    return;
                }
                elements.netSimples.add(new NetSimpleWrapper(aClass,
                        (a, b) -> encoder.invoke(a, b),
                        b -> decoder.get(b).orElseThrow(() -> new RuntimeException(String.format("[%s]Can't create MSG from %s", elements.container.id(), decoder.getRefName()))),
                        (a, b) -> handler.invoke(a, b)));
            });
        });
    }

    private static void loadEvent(ECModElements elements) {
        ObjHelper.stream(elements, ModEventNetwork.class).forEach(data -> {
            ObjHelper.findClass(elements, data.getClassType()).ifPresent(aClass -> {
                if (ObjHelper.getDefault(data, false)) {
                    Optional<Object> o = ReflectUtils.findConstructor(aClass, aClass).get();
                    if (o.isPresent()) {
                        elements.netEvents.add(new NetEventWrapper(o.get()));
                    } else {
                        elements.warn("[{}]Can't create object {}", elements.container.id(), aClass.getName());
                    }
                } else {
                    elements.netEvents.add(new NetEventWrapper(aClass));
                }
            });
        });
    }
}

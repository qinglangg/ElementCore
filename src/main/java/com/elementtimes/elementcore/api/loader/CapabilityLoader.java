package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.annotation.ModCapability;
import com.elementtimes.elementcore.api.annotation.part.Parts;
import com.elementtimes.elementcore.api.helper.ObjHelper;
import com.elementtimes.elementcore.api.misc.wrapper.AnnotationGetter;
import com.elementtimes.elementcore.api.misc.wrapper.AnnotationMethod;
import com.elementtimes.elementcore.api.misc.wrapper.CapabilityWrapper;
import net.minecraftforge.common.capabilities.Capability.IStorage;

import java.util.Map;
import java.util.Optional;

public class CapabilityLoader {

    public static void load(ECModElements elements) {
        ObjHelper.stream(elements, ModCapability.class).forEach(data -> {
            ObjHelper.findClass(elements, data.getClassType()).ifPresent(aClass -> {
                Map<String, Object> info = data.getAnnotationData();
                AnnotationGetter storage = Parts.getter(elements, info.get("storage"));
                AnnotationMethod factory = Parts.method(elements, info.get("factory"));
                if (!storage.hasContent()) {
                    elements.warn("[{}]Capability at {} can't find a Getter for IStorage", elements.container.id(), aClass.getName());
                } else if (!factory.hasContent()) {
                    elements.warn("[{}]Capability at {} can't find a Method for Capability", elements.container.id(), aClass.getName());
                } else {
                    Optional<IStorage<?>> o = storage.get();
                    if (o.isPresent()) {
                        elements.capabilities.add(new CapabilityWrapper(aClass, o.get(), factory));
                    } else {
                        elements.warn("[{}]Capability at {} can't get an IStorage from {}", elements.container.id(), aClass.getName(), storage.getRefName());
                    }
                }
            });
        });
    }
}

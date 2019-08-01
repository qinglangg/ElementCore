package com.elementtimes.elementcore.annotation.processor;

import com.elementtimes.elementcore.annotation.AnnotationInitializer;
import com.elementtimes.elementcore.annotation.annotations.ModCapability;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 对于 Capability 注解解析
 * @author luqin2007
 */
public class ModCapabilityLoader {

    public static void load(AnnotationInitializer initializer) {
        initializer.elements.get(ModCapability.class).forEach(element ->
                initializer.capabilities.add(element.getAnnotation(ModCapability.class)));
    }
}

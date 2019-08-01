package com.elementtimes.elementcore.annotation.processor;

import com.elementtimes.elementcore.annotation.AnnotationInitializer;
import com.elementtimes.elementcore.annotation.annotations.ModFeature;
import com.elementtimes.elementcore.util.ReflectUtil;
import net.minecraft.world.gen.feature.Feature;

import java.lang.reflect.Field;

/**
 * @author luqin2007
 */
public class ModFeatureLoader {

    public static void load(AnnotationInitializer initializer) {
        initializer.elements.get(ModFeature.class).forEach(e -> {
            if (e instanceof Class) {
                ReflectUtil.create((Class) e, initializer)
                        .filter(f -> f instanceof Feature)
                        .ifPresent(o -> initializer.features.add((Feature) o));
            } else if (e instanceof Field) {
                ReflectUtil.get((Field) e, null, null, true, initializer)
                        .filter(f -> f instanceof Feature)
                        .ifPresent(o -> initializer.features.add((Feature) o));
            }
        });
    }
}

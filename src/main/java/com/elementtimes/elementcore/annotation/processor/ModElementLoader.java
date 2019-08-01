package com.elementtimes.elementcore.annotation.processor;

import com.elementtimes.elementcore.annotation.AnnotationInitializer;
import com.elementtimes.elementcore.annotation.annotations.ModElement;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 加载其他被注解控制的元素
 * 处理所有 ModElement 注解
 * 目前只有一个 ModElement.ModInvokeStatic 注解
 *
 * @author luqin2007
 */
public class ModElementLoader {

    public static void getElements(AnnotationInitializer initializer) {
        initializer.elements.get(ModElement.class).forEach(e -> buildInvoker(initializer, e));
    }

    private static void buildInvoker(AnnotationInitializer initializer, AnnotatedElement element) {
        ModElement.ModInvokeStatic invoker = element.getAnnotation(ModElement.ModInvokeStatic.class);
        if (invoker != null) {
            String value = invoker.value();
            Method method = null;
            try {
                if (element instanceof Class) {
                    method = ((Class) element).getDeclaredMethod(value);
                } else if (element instanceof Field) {
                    method = ((Field) element).getType().getDeclaredMethod(value);
                }
            } catch (NoSuchMethodException e) {
                try {
                    if (element instanceof Class) {
                        method = ((Class) element).getMethod(value);
                    } else if (element instanceof Field) {
                        method = ((Field) element).getType().getMethod(value);
                    }
                } catch (NoSuchMethodException ex) {
                    initializer.warn("Skip Function: {} from {}", invoker.value(), element);
                }
            }
            if (method != null) {
                method.setAccessible(true);
                initializer.staticFunction.add(method);
            }
        }
    }
}

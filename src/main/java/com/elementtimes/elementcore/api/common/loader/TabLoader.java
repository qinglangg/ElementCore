package com.elementtimes.elementcore.api.common.loader;

import com.elementtimes.elementcore.api.annotation.ModTab;
import com.elementtimes.elementcore.api.annotation.tools.ModTabEditor;
import com.elementtimes.elementcore.api.annotation.tools.ModTabEditorFunc;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import com.elementtimes.elementcore.api.common.helper.RefHelper;
import com.elementtimes.elementcore.api.template.interfaces.invoker.VoidInvoker;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * @author luqin2007
 */
public class TabLoader {

    public static void load(ECModElements elements) {
        loadTabs(elements);
        loadEditor(elements);
    }

    private static void loadTabs(ECModElements elements) {
        ObjHelper.stream(elements, ModTab.class).forEach(data -> {
            String key = ObjHelper.getDefault(data, data.getObjectName());
            ObjHelper.findClass(elements, data.getClassName())
                    .flatMap(aClass -> ECUtils.reflect.get(aClass, data.getObjectName(), null, CreativeTabs.class, elements))
                    .ifPresent(creativeTabs -> {
                        elements.warn("[ModTab]{}={}", key, creativeTabs);
                        elements.tabs.put(key, creativeTabs);
                    });
        });
    }

    private static void loadEditor(ECModElements elements) {
        ObjHelper.stream(elements, ModTabEditor.class).forEach(data -> {
            ObjHelper.find(elements, CreativeTabs.class, data).ifPresent(tab -> {
                Object aDefault = ObjHelper.getDefault(data);
                VoidInvoker invoker = RefHelper.invoker(elements, aDefault, NonNullList.class);
                elements.warn("[ModTabEditor]{} {}", tab, RefHelper.toString(aDefault));
                ECUtils.collection.computeIfAbsent(elements.tabEditors, tab, ArrayList::new).add(invoker::invoke);
            });
        });
        ObjHelper.stream(elements, ModTabEditorFunc.class).forEach(data -> {
            String className = data.getClassName();
            ObjHelper.findClass(elements, className).ifPresent(aClass -> {
                String objectName = data.getObjectName();
                String methodName = objectName.substring(0, objectName.indexOf("("));
                Method method;
                try {
                    method = ReflectionHelper.findMethod(aClass, methodName, methodName, CreativeTabs.class, NonNullList.class);
                } catch (ReflectionHelper.UnableToFindMethodException e2) {
                    method = null;
                    elements.warn("[ModTabEditorFunc]Can't find valid method {} in {}", methodName, className);
                }
                if (method != null) {
                    method.setAccessible(true);
                    elements.warn("[ModTabEditorFunc]load modTab editor method {} in {}", method, className);
                    Method finalMethod = method;
                    elements.tabEditorFuns.add((tab, list) -> {
                        try {
                            finalMethod.invoke(null, tab, list);
                        } catch (IllegalAccessException | InvocationTargetException ignored) { }
                    });
                }
            });
        });
    }
}

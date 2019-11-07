package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.LoaderHelper;
import com.elementtimes.elementcore.api.annotation.ModInvoke;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodLoader {

    private boolean isMethodLoaded = false;
    private ECModElements mElements;

    private Map<Class<? extends Event>, List<Method>> methods = new HashMap<>();

    public MethodLoader(ECModElements elements) {
        mElements = elements;
    }

    public Map<Class<? extends Event>, List<Method>> methods() {
        if (!isMethodLoaded) {
            loadMethod();
        }
        return methods;
    }

    private void loadMethod() {
        LoaderHelper.stream(mElements, ModInvoke.class).forEach(data -> {
            LoaderHelper.loadClass(mElements, data.getClassType().getClassName()).ifPresent(clazz -> {
                try {
                    String memberName = data.getMemberName();
                    final Method method = clazz.getDeclaredMethod(memberName.substring(0, memberName.indexOf("(")));
                    int modifiers = method.getModifiers();
                    if (Modifier.isStatic(modifiers)) {
                        Type eventType = LoaderHelper.getDefault(data, Type.getType(FMLCommonSetupEvent.class));
                        LoaderHelper.loadClass(mElements, eventType.getClassName()).ifPresent(eventClass -> {
                            if (method.getParameterCount() == 0
                                    || (method.getParameterCount() == 1 && eventClass.isAssignableFrom(method.getParameterTypes()[0]))) {
                                if (!Modifier.isPublic(modifiers)) {
                                    method.setAccessible(true);
                                }
                                methods.computeIfAbsent(eventClass, (s) -> new ArrayList<>()).add(method);
                            }
                        });
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            });
        });
        isMethodLoaded = true;
    }
}

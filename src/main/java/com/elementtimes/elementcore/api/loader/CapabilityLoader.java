package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.LoaderHelper;
import com.elementtimes.elementcore.api.annotation.ModCapability;
import com.elementtimes.elementcore.other.CapabilityObject;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class CapabilityLoader {

    private boolean isCapabilityLoaded = false;
    private ECModElements mElements;

    private List<CapabilityObject> capabilities = new ArrayList<>();

    public CapabilityLoader(ECModElements elements) {
        mElements = elements;
    }

    public List<CapabilityObject> capabilities() {
        if (!isCapabilityLoaded) {
            loadCapabilities();
        }
        return capabilities;
    }

    private void loadCapabilities() {
        LoaderHelper.stream(mElements, ModCapability.class).forEach(data -> LoaderHelper.loadClass(mElements, data.getClassType().getClassName()).ifPresent(typeInterface -> {
            ModCapability cap = (ModCapability) typeInterface.getAnnotation(ModCapability.class);
            Class<? extends Capability.IStorage> typeStorage = cap.value();
            ECUtils.reflect.create(typeStorage, Capability.IStorage.class, mElements.logger).ifPresent(storage -> {
                Method factory = null;
                for (Method method : typeInterface.getDeclaredMethods()) {
                    int modifiers = method.getModifiers();
                    if (Modifier.isStatic(modifiers)
                            && method.getParameterCount() == 0
                            && typeInterface.isAssignableFrom(method.getReturnType())) {
                        boolean hasAnnotation = method.isAnnotationPresent(ModCapability.Factory.class);
                        if (hasAnnotation || factory == null) {
                            factory = method;
                        }
                        if (hasAnnotation) {
                            break;
                        }
                    }
                }
                if (factory != null) {
                    final Method f = factory;
                    if (!Modifier.isPublic(f.getModifiers())) {
                        f.setAccessible(true);
                    }
                    Callable callable = () -> {
                        try {
                            return f.invoke(null);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            return null;
                        }
                    };
                    CapabilityObject capability = new CapabilityObject(typeInterface, storage, callable);
                    capabilities.add(capability);
                }
            });
        }));
        isCapabilityLoaded = true;
    }
}

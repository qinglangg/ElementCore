package com.elementtimes.elementcore.other;

import net.minecraftforge.common.capabilities.Capability;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 对于 Capability 的包装类
 * @author luqin2007
 */
public class CapabilityObject {

    public final Class typeInterfaceClass, typeImplementationClass, storageClass;

    private Capability.IStorage mStorage = null;

    public CapabilityObject(Class typeInterfaceClass, Class typeImplementationClass, Class storageClass) {
        this.typeImplementationClass = typeImplementationClass;
        this.typeInterfaceClass = typeInterfaceClass;
        this.storageClass = storageClass;
    }

    public Object newInstance() {
        try {
            Constructor constructor = typeImplementationClass.getConstructor();
            if (constructor == null) {
                constructor = typeImplementationClass.getDeclaredConstructor();
                if (constructor != null) {
                    constructor.setAccessible(true);
                }
            }
            if (constructor != null) {
                return constructor.newInstance();
            }
            return null;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            return null;
        }
    }

    public Capability.IStorage storageInstance() {
        if (mStorage == null) {
            try {
                Constructor constructor = storageClass.getConstructor();
                if (constructor == null) {
                    constructor = storageClass.getDeclaredConstructor();
                    if (constructor != null) {
                        constructor.setAccessible(true);
                    }
                }
                if (constructor != null) {
                    mStorage = (Capability.IStorage) constructor.newInstance();
                }
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) { }
        }
        return mStorage;
    }
}

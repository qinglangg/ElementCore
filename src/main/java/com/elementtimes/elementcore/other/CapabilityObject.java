package com.elementtimes.elementcore.other;

import net.minecraftforge.common.capabilities.Capability;

import java.util.concurrent.Callable;

/**
 * 对于 Capability 的包装类
 * @author luqin2007
 */
public class CapabilityObject {

    public final Class typeInterfaceClass;
    public final Capability.IStorage storage;
    public final Callable defSupplier;

    public CapabilityObject(Class typeInterfaceClass, Capability.IStorage storage, Callable defSupplier) {
        this.storage = storage;
        this.typeInterfaceClass = typeInterfaceClass;
        this.defSupplier = defSupplier;
    }
}

package com.elementtimes.elementcore.api.misc.wrapper;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.misc.IAnnotationRef;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * @author luqin2007
 */
public class CapabilityWrapper {
    public final Class typeInterface;
    public final Capability.IStorage storage;
    public final IAnnotationRef factory;

    public Object factory() {
        return factory.get().orElse(null);
    }

    public CapabilityWrapper(Class<?> t, Capability.IStorage<?> s, IAnnotationRef f) {
        typeInterface = t;
        storage = s;
        factory = f;
    }

    public void apply(Logger logger) {
        logger.warn("    {}, storage={}", typeInterface.getSimpleName(), storage);
        CapabilityManager.INSTANCE.register(typeInterface, storage, this::factory);
    }

    public static void registerAll(ECModElements elements) {
        List<CapabilityWrapper> capabilities = elements.capabilities;
        elements.warn("  Capability({})", capabilities.size());
        capabilities.forEach(c -> c.apply(elements));
    }
}

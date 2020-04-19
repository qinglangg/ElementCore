package com.elementtimes.elementcore.api.common.loader;

import com.elementtimes.elementcore.api.annotation.ModCapability;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import com.elementtimes.elementcore.api.common.helper.RefHelper;
import com.elementtimes.elementcore.api.template.interfaces.invoker.Invoker;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;
import org.objectweb.asm.Type;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class CapabilityLoader {

    public static void load(ECModElements elements) {
        ObjHelper.stream(elements, ModCapability.class).forEach(data -> {
            Map<String, Object> info = data.getAnnotationInfo();
            Type type = (Type) info.get("type");
            Map<String, Object> factory = (Map<String, Object>) info.get("typeFactory");
            Map<String, Object> storage = (Map<String, Object>) info.get("storage");
            Optional<Class<?>> typeClassOpt = ObjHelper.findClass(elements, type.getClassName());
            Optional<? extends IStorage> storageOpt = RefHelper.get(elements, storage, IStorage.class);
            Invoker<Object> factoryFunc = RefHelper.invoker(elements, factory, Invoker.empty());
            if (typeClassOpt.isPresent() && factory != null && storageOpt.isPresent()) {
                CapabilityData capability = new CapabilityData(typeClassOpt.get(), storageOpt.get(), factoryFunc::invoke);
                elements.warn("[ModCapability]{}", capability);
                elements.capabilities.add(capability);
            }
        });
    }

    public static class CapabilityData {
        public final Class typeInterface;
        public final IStorage storage;
        public final Supplier<Object> factory;

        public Object factory() {
            return factory.get();
        }

        public CapabilityData(Class<?> t, IStorage s, Supplier<Object> f) {
            typeInterface = t;
            storage = s;
            factory = f;
        }

        @Override
        public String toString() {
            return "CapabilityData{" + "type=" + typeInterface + ", storage=" + storage + '}';
        }
    }
}

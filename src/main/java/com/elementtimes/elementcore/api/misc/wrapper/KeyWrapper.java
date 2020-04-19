package com.elementtimes.elementcore.api.misc.wrapper;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.interfaces.invoker.VoidInvoker;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.BiConsumer;

public class KeyWrapper {

    private Object mKey;
    private BiConsumer<Object, Object> mListener;

    public KeyWrapper(Object key, BiConsumer<Object, Object> listener) {
        mKey = key;
        mListener = listener;
    }

    @OnlyIn(Dist.CLIENT)
    public net.minecraft.client.settings.KeyBinding getKey() {
        return (net.minecraft.client.settings.KeyBinding) mKey;
    }

    @OnlyIn(Dist.CLIENT)
    public void testPress(ECModElements elements, net.minecraftforge.client.event.InputEvent.KeyInputEvent event) {
        net.minecraft.client.settings.KeyBinding key = getKey();
        if (key.isPressed()) {
            elements.warn("[{}]  {}.{} is pressed", elements.container.id(), key.getKeyCategory(), key.getKeyDescription());
            mListener.accept(event, key);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void apply(Logger logger) {
        net.minecraft.client.settings.KeyBinding key = getKey();
        logger.warn("    {}.{}", key.getKeyCategory(), key.getKeyDescription());
        net.minecraftforge.fml.client.registry.ClientRegistry.registerKeyBinding(key);
    }

    public static void registerAll(ECModElements elements) {
        List<KeyWrapper> keys = elements.keys;
        elements.warn("  Keys({})", keys.size());
        keys.forEach(e -> e.apply(elements));
    }
}

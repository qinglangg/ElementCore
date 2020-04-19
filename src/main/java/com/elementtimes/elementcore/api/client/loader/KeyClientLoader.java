package com.elementtimes.elementcore.api.client.loader;

import com.elementtimes.elementcore.api.annotation.ModKey;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import com.elementtimes.elementcore.api.common.helper.RefHelper;
import com.elementtimes.elementcore.api.template.interfaces.invoker.VoidInvoker;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyClientLoader {

    public static void load(ECModElements elements) {
        loadKey(elements);
    }

    private static void loadKey(ECModElements elements) {
        ObjHelper.stream(elements, ModKey.class).forEach(data -> {
            ObjHelper.find(elements, KeyBinding.class, data).ifPresent(key -> {
                elements.getClientNotInit().keys.add(key);
                Object aDefault = ObjHelper.getDefault(data);
                VoidInvoker invoker = RefHelper.invokerNullable(elements, aDefault, InputEvent.KeyInputEvent.class, KeyBinding.class);
                if (invoker != null) {
                    elements.getClientNotInit().keyEvents.put(key, (e, k) -> invoker.invoke(e, k));
                    elements.warn("[ModKey]key={}, action={}", key.getDisplayName(), RefHelper.toString(aDefault));
                } else {
                    elements.warn("[ModKey]key={}", key.getDisplayName());
                }
            });
        });
    }
}

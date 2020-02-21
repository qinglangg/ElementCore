package com.elementtimes.elementcore.api.client.loader;

import com.elementtimes.elementcore.api.annotation.ModEntity;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import com.elementtimes.elementcore.api.common.helper.RefHelper;
import com.elementtimes.elementcore.api.template.interfaces.invoker.Invoker;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

/**
 * @author luqin2007
 */
public class EntityClientLoader {

    public static void load(ECModElements elements) {
        ObjHelper.stream(elements, ModEntity.class).forEach(data -> {
            ObjHelper.<Entity>findClass(elements, data.getClassName()).ifPresent(entityClass -> {
                Invoker<Render> render = RefHelper.invoker(elements, data.getAnnotationInfo().get("render"), Invoker.empty(), RenderManager.class);
                elements.getClientNotInit().entityRenders.put(entityClass, render::invoke);
            });
        });
    }
}

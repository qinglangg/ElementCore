package com.elementtimes.elementcore.api.misc.wrapper;

import com.elementtimes.elementcore.api.ECModElements;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.Function;

public class EntityRendererWrapper {

    public final Function<Object, Object> render;
    public final Class<?> entityClass;

    public EntityRendererWrapper(Function<Object, Object> render, Class<?> entityClass) {
        this.render = render;
        this.entityClass = entityClass;
    }

    @OnlyIn(Dist.CLIENT)
    public void apply(Logger logger) {
        logger.warn("    {}", entityClass.getName());
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler((Class<? extends Entity>) entityClass,
                (net.minecraftforge.fml.client.registry.IRenderFactory<Entity>) manager -> (net.minecraft.client.renderer.entity.EntityRenderer) render.apply(manager));
    }

    public static void registerAll(ECModElements elements) {
        List<EntityRendererWrapper> renders = elements.entityRenders;
        elements.warn("  Entity Render({})", renders.size());
        renders.forEach(e -> e.apply(elements));
    }
}

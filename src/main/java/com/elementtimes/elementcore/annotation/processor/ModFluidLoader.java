package com.elementtimes.elementcore.annotation.processor;

import com.elementtimes.elementcore.annotation.AnnotationInitializer;
import com.elementtimes.elementcore.annotation.annotations.ModFluid;
import com.elementtimes.elementcore.annotation.annotations.ModTags;
import com.elementtimes.elementcore.util.ReflectUtil;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.AnnotatedElement;

/**
 * 用于加载流体
 * @author luqin2007
 */
public class ModFluidLoader {

    public static void load(AnnotationInitializer initializer) {
        initializer.elements.get(ModFluid.class).forEach(element -> buildFluid(initializer, element));
    }

    private static void buildFluid(AnnotationInitializer initializer, AnnotatedElement element) {
        ModFluid info = element.getAnnotation(ModFluid.class);
        Object o = ReflectUtil.getFromAnnotated(element, null, initializer).orElse(null);
        if (o instanceof Fluid) {
            Fluid fluid = (Fluid) o;
            initializer.fluids.add(fluid);
            initializer.fluidBurningTimes.put(fluid, info.burningTime());
            initTags(initializer, element, fluid);
        } else if (o instanceof com.elementtimes.elementcore.fluid.Fluid.FluidResult) {
            com.elementtimes.elementcore.fluid.Fluid.FluidResult result = (com.elementtimes.elementcore.fluid.Fluid.FluidResult) o;
            initializer.fluids.add(result.flowingFluid);
            initializer.fluids.add(result.stillFluid);
            initializer.fluidBurningTimes.put(result.flowingFluid, info.burningTime());
            initializer.fluidBurningTimes.put(result.stillFluid, info.burningTime());
            initTags(initializer, element, result.flowingFluid, result.stillFluid);
            initializer.blocks.add(result.block);
        }
    }

    private static void initTags(AnnotationInitializer initializer, AnnotatedElement element, Fluid... fluids) {
        ModTags tags = element.getAnnotation(ModTags.class);
        if (tags != null) {
            ResourceLocation tag = new ResourceLocation(tags.value());
            Tag.Builder<Fluid> builder = initializer.fluidTags.getOrDefault(tag, new Tag.Builder<>());
            builder.add(fluids);
            initializer.fluidTags.put(tag, builder);
        }
    }
}

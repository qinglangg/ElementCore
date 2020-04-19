package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.annotation.ModFluid;
import com.elementtimes.elementcore.api.helper.FindOptions;
import com.elementtimes.elementcore.api.helper.ObjHelper;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.annotation.ElementType;
import java.util.Map;

public class FluidLoader {

    public static void load(ECModElements elements) {
        loadFluid(elements);
    }

    private static void loadFluid(ECModElements elements) {
        ObjHelper.stream(elements, ModFluid.class).forEach(data -> {
            ObjHelper.find(elements, data, new FindOptions<>(Fluid.class, ElementType.FIELD)).ifPresent(fluid -> {
                Map<String, Object> map = data.getAnnotationData();
                if (!(boolean) map.getOrDefault("noBucket", false)) {
                    Item bucket = fluid.getFilledBucket();
                    if (bucket != Items.AIR) {
                        elements.buckets.add(bucket);
                        ObjHelper.setRegisterName(bucket, null, data, elements);
                    }
                }
                if (fluid instanceof FlowingFluid) {
                    FlowingFluid f = (FlowingFluid) fluid;
                    addFluid(data, elements, f.getStillFluid(), f.getFlowingFluid());
                } else {
                    addFluid(data, elements, fluid, null);
                }
            });
        });
    }

    private static void addFluid(ModFileScanData.AnnotationData data, ECModElements elements, Fluid still, Fluid flowing) {
        Map<String, Object> map = data.getAnnotationData();
        String name = (String) map.get("name");
        ObjHelper.setRegisterName(still, name, data, elements);
        elements.fluids.add(still);
        if (flowing != null && flowing != still && flowing.getRegistryName() == null) {
            ObjHelper.setRegisterName(flowing, (String) map.get("flowingName"), still.getRegistryName().toString() + "_flowing", elements);
            elements.fluids.add(flowing);
        }
    }
}

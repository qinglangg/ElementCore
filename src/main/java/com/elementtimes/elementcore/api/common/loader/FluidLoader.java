package com.elementtimes.elementcore.api.common.loader;

import com.elementtimes.elementcore.api.annotation.ModFluid;
import com.elementtimes.elementcore.api.annotation.enums.FluidBlockType;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import com.elementtimes.elementcore.api.common.helper.RefHelper;
import com.elementtimes.elementcore.api.template.interfaces.invoker.Invoker;
import net.minecraft.block.Block;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class FluidLoader {

    public static void load(ECModElements elements) {
        loadFluid(elements);
        loadFluidBlock(elements);
    }

    private static void loadFluid(ECModElements elements) {
        ObjHelper.stream(elements, ModFluid.class).forEach(data -> {
            ObjHelper.findClass(elements, data.getClassName()).ifPresent(aClass -> {
                Map<String, Object> info = data.getAnnotationInfo();
                String unlocalizedName = (String) info.get("unlocalizedName");
                Fluid fluid = newFluid(elements, data.getClassName(), data.getObjectName(), unlocalizedName);
                if (fluid != null) {
                    // bucket
                    if ((boolean) info.getOrDefault("bucket", true)) {
                        elements.fluidBuckets.add(fluid);
                    }
                    // density & gaseous
                    int density = (int) info.getOrDefault("density", 1000);
                    fluid.setDensity(density);
                    fluid.setGaseous(density < 0);
                    // creativeTabs
                     ObjHelper.findTab(elements, (String) info.get("creativeTabKey"))
                             .ifPresent(tab -> elements.fluidTabs.put(fluid, tab));
                    // texture
                    if ((boolean) info.getOrDefault("loadTexture", true)) {
                        elements.fluidResources.add(fluid);
                    }
                }
            });
        });
    }

    private static void loadFluidBlock(ECModElements elements) {
        ObjHelper.stream(elements, ModFluid.FluidBlock.class).forEach(data -> {
            ObjHelper.find(elements, Fluid.class, data).ifPresent(fluid -> {
                Map<String, Object> info = data.getAnnotationInfo();
                String registerName = (String) info.get("registerName");
                String unlocalizedName = (String) info.get("unlocalizedName");
                String tab = (String) info.get("creativeTabKey");
                ModAnnotation.EnumHolder type = (ModAnnotation.EnumHolder) info.get("type");
                Function<Fluid, Block> fluidBlock = f -> newFluidBlock(elements, f, type, registerName, unlocalizedName, tab);
                String resource = (String) info.getOrDefault("resource", "fluids");
                if (!StringUtils.isNullOrEmpty(resource)) {
                    elements.fluidBlockResources.put(fluid, resource);
                }
                elements.fluidBlocks.put(fluid, fluidBlock);
            });
        });
        ObjHelper.stream(elements, ModFluid.FluidBlockObj.class).forEach(data -> {
            ObjHelper.find(elements, Fluid.class, data).ifPresent(fluid -> {
                Supplier<Block> getter = RefHelper.getter(elements, ObjHelper.getDefault(data), Block.class);
                Function<Fluid, Block> fluidBlock = f -> getter.get();
                String resource = (String) data.getAnnotationInfo().getOrDefault("resource", "fluids");
                if (!StringUtils.isNullOrEmpty(resource)) {
                    elements.fluidBlockResources.put(fluid, resource);
                }
                elements.fluidBlocks.put(fluid, fluidBlock);
            });
        });
        ObjHelper.stream(elements, ModFluid.FluidBlockFunc.class).forEach(data -> {
            ObjHelper.find(elements, Fluid.class, data).ifPresent(fluid -> {
                Invoker<Block> invoker = RefHelper.invoker(elements, ObjHelper.getDefault(data), p -> FluidBlockType.Classic.create(fluid), Fluid.class);
                Function<Fluid, Block> fluidBlock = f -> invoker.invoke(fluid);
                String resource = (String) data.getAnnotationInfo().getOrDefault("resource", "fluids");
                if (!StringUtils.isNullOrEmpty(resource)) {
                    elements.fluidBlockResources.put(fluid, resource);
                }
                elements.fluidBlocks.put(fluid, fluidBlock);
            });
        });
    }

    public static Fluid newFluid(ECModElements elements, String className, String objectName, String unlocalizedName) {
        Optional<Fluid> fluidOpt = ObjHelper.findClass(elements, className)
                .flatMap(aClass -> ECUtils.reflect.get(aClass, objectName, null, Fluid.class, elements));
        if (fluidOpt.isPresent()) {
            Fluid fluid = fluidOpt.get();
            if (!StringUtils.isNullOrEmpty(unlocalizedName)) {
                fluid.setUnlocalizedName(unlocalizedName.toLowerCase());
            }
            elements.fluids.add(fluid);
            return fluid;
        }
        return null;
    }

    public static Block newFluidBlock(ECModElements elements, Fluid fluid, ModAnnotation.EnumHolder type,
                                      String registerName, String unlocalizedName, String creativeTabKey) {
        FluidBlockType fbt;
        if (type == null) {
            fbt = FluidBlockType.Classic;
        } else {
            fbt = FluidBlockType.valueOf(type.getValue());
        }
        BlockFluidBase block = fbt.create(fluid);
        if (block.getRegistryName() == null) {
            registerName = StringUtils.isNullOrEmpty(registerName) ? fluid.getName() : registerName;
            if (registerName.contains(":")) {
                block.setRegistryName(registerName);
            } else {
                block.setRegistryName(elements.container.id(), registerName);
            }
        }
        ObjHelper.findTab(elements, creativeTabKey).ifPresent(block::setCreativeTab);
        if ("tile.null".equals(block.getUnlocalizedName())) {
            if (StringUtils.isNullOrEmpty(unlocalizedName)) {
                block.setUnlocalizedName(elements.container.id() + "." + fluid.getName().toLowerCase());
            } else {
                block.setUnlocalizedName(elements.container.id() + "." + unlocalizedName.toLowerCase());
            }
        }
        block.setDensity(fluid.getDensity());
        return block;
    }

}

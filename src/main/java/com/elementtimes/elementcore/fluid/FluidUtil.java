package com.elementtimes.elementcore.fluid;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * 流体工具类
 * @author luqin2007
 */
public class FluidUtil {

    private static Map<ResourceLocation, Fluid> FLUIDS = new HashMap<>();

    public static FluidStack getFluid(ItemStack itemStack) {
        FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(itemStack.getOrCreateTag());
        if (fluidStack == null) {
            LazyOptional<IFluidHandlerItem> capabilityOpt = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (capabilityOpt.isPresent()) {
                IFluidHandlerItem capability = capabilityOpt.orElse(null);
                if (capability instanceof FluidBucketWrapper) {
                    fluidStack = ((FluidBucketWrapper) capability).getFluid();
                } else if (capability instanceof FluidHandlerItemStack) {
                    fluidStack = ((FluidHandlerItemStack) capability).getFluid();
                } else if (capability instanceof FluidHandlerItemStackSimple) {
                    fluidStack = ((FluidHandlerItemStackSimple) capability).getFluid();
                } else {
                    fluidStack = capability.getTankProperties()[0].getContents();
                }
            }
        }
        return fluidStack;
    }

    @Nullable
    public static Fluid fromFluid(ResourceLocation registryName) {
        Fluid f = FLUIDS.get(registryName);
        if (f != null) {
            return f;
        }
        net.minecraft.fluid.Fluid fluid = ForgeRegistries.FLUIDS.getValue(registryName);
        if (fluid != null) {
            return registerFluid(fluid, null, null, null, -1);
        }
        return null;
    }

    public static Fluid fromFluid(net.minecraft.fluid.Fluid fluid) {
        ResourceLocation name = fluid.getRegistryName();
        Fluid f = FLUIDS.get(name);
        if (f != null) {
            return f;
        } else {
            return registerFluid(fluid, null, null, null, -1);
        }
    }

    public static Fluid registerFluid(net.minecraft.fluid.Fluid fluid,
                                      @Nullable ResourceLocation still,
                                      @Nullable ResourceLocation flowing,
                                      @Nullable ResourceLocation overlay,
                                      int color) {
        ResourceLocation location = fluid.getRegistryName();
        if (still == null) {
            still = location;
        }
        if (flowing == null) {
            flowing = location;
        }
        Fluid f = new Fluid(fluid.getRegistryName().toString(), still, flowing, overlay);
        if (color > 0) {
            f.setColor(color);
        }
        FLUIDS.put(fluid.getRegistryName(), f);
        return f;
    }
}

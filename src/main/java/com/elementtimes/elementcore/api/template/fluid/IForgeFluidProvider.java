package com.elementtimes.elementcore.api.template.fluid;

import com.elementtimes.elementcore.api.utils.FluidUtils;
import net.minecraftforge.fluids.Fluid;

public interface IForgeFluidProvider {

    Fluid getForgeFluid();

    int getFilledAmount();

    default net.minecraftforge.fluids.FluidStack getFluidStack() {
        Fluid fluid = getForgeFluid();
        if (fluid == null) {
            fluid = FluidUtils.NULL;
        }
        return new net.minecraftforge.fluids.FluidStack(getForgeFluid(), getFilledAmount());
    }
}

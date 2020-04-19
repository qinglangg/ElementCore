package com.example.examplemod.fluid;

import com.example.examplemod.ExampleMod;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;

public class TestAttribute extends FluidAttributes {

    protected TestAttribute(Builder builder, Fluid fluid) {
        super(builder, fluid);
    }

    public static Builder builder() {
        return new Builder(new ResourceLocation(ExampleMod.CONTAINER.id(), "fluid/f0_fluid"),
                new ModelResourceLocation(ExampleMod.CONTAINER.id(), "fluid/f0_still"), TestAttribute::new) {};
    }
}

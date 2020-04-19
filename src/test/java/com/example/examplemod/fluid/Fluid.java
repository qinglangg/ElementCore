package com.example.examplemod.fluid;

import com.elementtimes.elementcore.api.annotation.ModFluid;
import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;

public class Fluid {

    @ModFluid
    public static net.minecraft.fluid.Fluid testFluid = new TestFluid();

    public static FlowingFluidBlock testBlock = new FlowingFluidBlock(() -> TestFluid.source, Block.Properties.create(Material.WATER));
}

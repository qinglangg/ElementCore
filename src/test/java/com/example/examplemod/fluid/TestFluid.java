package com.example.examplemod.fluid;

import com.example.examplemod.group.Groups;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class TestFluid extends ForgeFlowingFluid.Source {

    public static TestFluid source;
    public static TestFluidFlowing flowing;
    private static Properties properties = new Properties(() -> source, () -> flowing, TestAttribute.builder())
            .block(() -> Fluid.testBlock)
            .bucket(() -> new BucketItem(() -> source, new Item.Properties().group(Groups.main)));

    public TestFluid() {
        super(properties);
        source = this;
        flowing = new TestFluidFlowing(properties);
    }

    private class TestFluidFlowing extends Flowing {
        public TestFluidFlowing(Properties properties) {
            super(properties);
        }
    }
}

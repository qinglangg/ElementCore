package com.elementtimes.elementcore.api.template.fluid;

import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;

/**
 * 与原版流体相关
 * @author luqin2007
 */
public class VanillaFluidHelper {

    public static FlowingFluidBlock createBlock(AbstractFluid fluid, Block.Properties properties) {
        FlowingFluidBlock block = new FlowingFluidBlock(fluid.getStillFluid(), properties.doesNotBlockMovement().hardnessAndResistance(100.0F).noDrops()) {};
        block.setRegistryName(Objects.requireNonNull(fluid.getRegistryName()));
        return block;
    }

    public static AbstractFluid.Source createFluid(String modId, String name, FlowingFluidBlock block, Item filledBucket) {
        AbstractFluid.Source source = new AbstractFluid.Source(block, filledBucket);
        source.setRegistryName(new ResourceLocation(modId, name));
        AbstractFluid.Flowing flowing = new AbstractFluid.Flowing(block, filledBucket);
        flowing.setRegistryName(new ResourceLocation(modId, "flowing_" + name));
        source.setOther(flowing);
        flowing.setOther(source);
        return source;
    }

    public static AbstractFluid.Source createFluid(String modId, String name, ItemGroup group) {
        AbstractFluid.Source fluid = createFluid(modId, name, null, null);
        FlowingFluidBlock block = createBlock(fluid, Block.Properties.create(Material.WATER));
        BucketItem filledBucket = new BucketItem(fluid, new Item.Properties().group(group));
        fluid.setFluidBlock(block);
        fluid.setFilledBucket(filledBucket);
        return fluid;
    }
}

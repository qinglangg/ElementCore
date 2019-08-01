package com.elementtimes.elementcore.fluid;

import com.elementtimes.elementcore.itemgroup.CoreGroup;
import net.minecraft.item.Item;

public class FluidBucket extends Item {

    public FluidBucket() {
        super(new Properties().maxStackSize(1).group(CoreGroup.INSTANCE));
    }
}

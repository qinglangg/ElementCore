package com.elementtimes.elementcore.item;

import com.elementtimes.elementcore.itemgroup.CoreGroup;
import net.minecraft.item.Item;

public class Bottle extends Item {

    public Bottle() {
        super(new Properties().group(CoreGroup.INSTANCE).maxStackSize(1));
    }
}

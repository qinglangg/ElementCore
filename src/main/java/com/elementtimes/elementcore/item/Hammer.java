package com.elementtimes.elementcore.item;

import com.elementtimes.elementcore.itemgroup.CoreGroup;
import net.minecraft.item.Item;

public class Hammer extends Item {

    public Hammer(int damage) {
        super(new Properties().maxDamage(damage).setNoRepair().group(CoreGroup.INSTANCE));
    }
}

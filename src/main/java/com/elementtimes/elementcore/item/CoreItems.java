package com.elementtimes.elementcore.item;

import com.elementtimes.elementcore.annotation.annotations.ModItem;
import net.minecraft.item.Item;

/**
 * @author luqin2007
 */
public class CoreItems {

    @ModItem
    public static Item bigHammer = new Hammer(100);

    @ModItem
    public static Item smallHammer = new Hammer(20);

    @ModItem
    public static Item bottle = new Bottle();
}

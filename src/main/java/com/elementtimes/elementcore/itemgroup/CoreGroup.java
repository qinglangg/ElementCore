package com.elementtimes.elementcore.itemgroup;

import com.elementtimes.elementcore.item.CoreItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

/**
 * @author luqin2007
 */
public class CoreGroup extends ItemGroup {

    public static final ItemGroup INSTANCE = new CoreGroup();

    public CoreGroup() {
        super("elementcore");
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(CoreItems.smallHammer);
    }
}

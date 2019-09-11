package com.elementtimes.elementcore.api.template.block;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.util.EnumFacing;

public class Properties {
    public static final PropertyBool IS_RUNNING = PropertyBool.create("running");
    public static final IProperty<EnumFacing> FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
}

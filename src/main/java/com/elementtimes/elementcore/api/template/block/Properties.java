package com.elementtimes.elementcore.api.template.block;

import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;

/**
 * 方块属性集
 * @author luqin2007
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Properties {
    public static final BooleanProperty IS_RUNNING = BooleanProperty.create("running");
    public static final BooleanProperty IS_BURNING = BooleanProperty.create("burning");
    public static final Property<Direction> FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
}

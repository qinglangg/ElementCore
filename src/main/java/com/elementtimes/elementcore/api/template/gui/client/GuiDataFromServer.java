package com.elementtimes.elementcore.api.template.gui.client;

import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import com.elementtimes.elementcore.api.template.tileentity.lifecycle.HandlerInfoMachineLifecycle;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Map;

/**
 * 接收来自
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
public class GuiDataFromServer {
    public static final Int2ObjectArrayMap<HandlerInfoMachineLifecycle.EnergyInfo> ENERGIES
            = new Int2ObjectArrayMap<>(3);

    public static final Int2ObjectArrayMap<Map<SideHandlerType, Int2ObjectMap<ImmutablePair<FluidStack, Integer>>>> FLUIDS
            = new Int2ObjectArrayMap<>(3);
}

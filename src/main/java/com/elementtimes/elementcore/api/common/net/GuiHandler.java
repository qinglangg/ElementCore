package com.elementtimes.elementcore.api.common.net;

import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.api.annotation.ModGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@ModGui(ElementCore.MODID)
public class GuiHandler implements IGuiHandler {

    private static int sNextId = 0;
    private static List<IGuiHandler> sGui = new ArrayList<>();

    public static int getNextId(IGuiHandler gui) {
        int nextId = sNextId++;
        sGui.add(nextId, gui);
        return nextId;
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return sGui.get(ID).getServerGuiElement(ID, player, world, x, y, z);
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return sGui.get(ID).getClientGuiElement(ID, player, world, x, y, z);
    }
}

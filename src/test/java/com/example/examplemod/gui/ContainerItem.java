package com.example.examplemod.gui;

import com.elementtimes.elementcore.api.annotation.ModContainer;
import com.elementtimes.elementcore.api.annotation.part.Method2;
import com.example.examplemod.ExampleMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;

@ModContainer(screen = @Method2("com.example.examplemod.gui.ScreenItem"))
public class ContainerItem extends Container {

    protected ContainerItem(int id, PlayerInventory inventory, PacketBuffer extra) {
        super(ExampleMod.CONTAINER.elements.generatedContainerTypes.get(ContainerItem.class), id);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }
}

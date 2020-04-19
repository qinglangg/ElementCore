package com.example.examplemod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScreenItem extends ContainerScreen<ContainerItem> {

    protected ScreenItem(Container container, PlayerInventory inventory, ITextComponent titleIn) {
        super((ContainerItem) container, inventory, titleIn);
        setSize(300, 300);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        Minecraft.getInstance().fontRenderer.drawString(title.getFormattedText(), getGuiLeft(), getGuiTop(), 0xFF323232);
    }
}

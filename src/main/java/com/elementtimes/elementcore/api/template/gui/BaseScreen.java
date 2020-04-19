package com.elementtimes.elementcore.api.template.gui;

import com.elementtimes.elementcore.api.interfaces.block.IMachineTickable;
import com.elementtimes.elementcore.api.interfaces.gui.IGuiProvider;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GuiContainer
 * @author luqin2007
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@OnlyIn(Dist.CLIENT)
public class BaseScreen extends ContainerScreen<BaseContainer> {

    protected BaseContainer container;

    public BaseScreen(BaseContainer container) {
        super(container, Minecraft.getInstance().player.inventory, container.provider.getDisplayName());
        this.container = container;
        xSize = container.size.width;
        ySize = container.size.height;
    }

    @Override
    protected void renderHoveredToolTip(int x, int y) {
        super.renderHoveredToolTip(x, y);
        renderHoveredFluid(x, y);
        renderHoveredEnergy(x, y);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color3f(1.0F, 1.0F, 1.0F);
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bindTexture(container.provider.getBackground());
        GuiUtils.drawTexturedModalRect(getGuiLeft(), getGuiTop(), 0, 0, this.xSize, this.ySize, 0);
        // 能量
        for (IGuiProvider.Size energyGui : container.size.energy) {
            renderGuiEnergy(energyGui);
        }
        // 进度
        if (container.tile instanceof IMachineTickable) {
            IMachineTickable machine = (IMachineTickable) container.tile;
            float p = (float) machine.getEnergyProcessed() / (machine.getEnergyProcessed() + machine.getEnergyUnprocessed());
            for (IGuiProvider.Size process : container.size.process) {
                renderGuiProcess(process, p);
            }
        }
        // 其他
        container.provider.onBackgroundRender(this, container, partialTicks, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        String name = container.provider.getDisplayName().getFormattedText();
        this.font.drawString(name, (container.size.width - font.getStringWidth(name)) / 2f, container.provider.getSize().titleOffsetY, 0x404040);
        renderGuiFluid();
        container.provider.onForegroundRender(this, container, mouseX, mouseY);
    }

    private void renderGuiFluid() {
        GlStateManager.color3f(1.0F, 1.0F, 1.0F);
        container.tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(handler -> {
            for (IGuiProvider.FluidSlotInfo slotInfo : container.provider.getFluids()) {
                int slot = slotInfo.slotId;
                FluidStack fluidStack = handler.getFluidInTank(slot);
                int capacity = handler.getTankCapacity(slot);
                if (!fluidStack.isEmpty()) {
                    ResourceLocation stillTexture = fluidStack.getFluid().getAttributes().getFlowingTexture();
                    stillTexture = new ResourceLocation(stillTexture.getNamespace(), "textures/" + stillTexture.getPath() + ".png");
                    assert minecraft != null;
                    minecraft.getTextureManager().bindTexture(stillTexture);
                    if (slotInfo.isHorizontal) {
                        int w = slotInfo.w * fluidStack.getAmount() / capacity;
                        GuiUtils.drawTexturedModalRect(slotInfo.x, slotInfo.y, 0, 0, w, slotInfo.h, 0);
                    } else {
                        int h = slotInfo.h * fluidStack.getAmount() / capacity;
                        int y = slotInfo.y + slotInfo.h - h;
                        GuiUtils.drawTexturedModalRect(slotInfo.x, y, 0, 0, slotInfo.w, h, 0);
                    }
                }
            }
        });
    }

    private void renderGuiEnergy(IGuiProvider.Size energyGui) {
        container.tile.getCapability(CapabilityEnergy.ENERGY).ifPresent(handler -> {
            int energyStored = handler.getEnergyStored();
            int energyCapacity = handler.getMaxEnergyStored();
            int textureWidth = energyStored == 0 ? 0 : (int) (((float) energyGui.w) * energyStored / energyCapacity);
            GuiUtils.drawTexturedModalRect(getGuiLeft() + energyGui.x, getGuiTop() + energyGui.y, energyGui.u, energyGui.v, textureWidth, energyGui.h, 0);
        });
    }

    private void renderGuiProcess(IGuiProvider.Size process, float processValue) {
        int arrowWidth = (int) (process.w * processValue);
        GuiUtils.drawTexturedModalRect(getGuiLeft() + process.x,
                getGuiTop() + process.y,
                process.u, process.v, arrowWidth, process.h, 0);
    }

    private void renderHoveredFluid(int mouseX, int mouseY) {
        container.tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(handler -> {
            for (IGuiProvider.FluidSlotInfo fluidPosition : container.getFluidPositions()) {
                if (mouseIn(mouseX, mouseY, fluidPosition.x, fluidPosition.y, fluidPosition.w, fluidPosition.h)) {
                    FluidStack fluidStack = handler.getFluidInTank(fluidPosition.slotId);
                    if (!fluidStack.isEmpty()) {
                        int total = handler.getTankCapacity(fluidPosition.slotId);
                        List<String> texts = new ArrayList<>(2);
                        texts.add(fluidStack.getDisplayName().getFormattedText());
                        texts.add(fluidStack.getAmount() + "/" + total);
                        GuiUtils.drawHoveringText(texts, mouseX, mouseY, width, height, width, font);
                    }
                }
            }
        });
    }

    private void renderHoveredEnergy(int mouseX, int mouseY) {
        container.tile.getCapability(CapabilityEnergy.ENERGY).ifPresent(handler -> {
            for (IGuiProvider.Size energyPosition : container.size.energy) {
                if (mouseIn(mouseX, mouseY, energyPosition.x, energyPosition.y, energyPosition.w, energyPosition.h)) {
                    GuiUtils.drawHoveringText(Collections.singletonList(handler.getEnergyStored() + "/" + handler.getMaxEnergyStored()),
                            mouseX, mouseY, width, height, width, font);
                }
            }
        });
    }

    protected boolean mouseIn(int mouseX, int mouseY, int x, int y, int w, int h) {
        int mx = mouseX - getGuiLeft();
        int my = mouseY - getGuiTop();
        return mx >= x
                && mx <= (x + w)
                && my >= y
                && my <= (y + h);
    }
}

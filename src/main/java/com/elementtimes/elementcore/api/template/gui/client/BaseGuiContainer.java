package com.elementtimes.elementcore.api.template.gui.client;

import com.elementtimes.elementcore.api.template.gui.server.BaseContainer;
import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.IGuiProvider;
import com.elementtimes.elementcore.api.template.tileentity.lifecycle.HandlerInfoMachineLifecycle;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * GuiContainer
 * @author luqin2007
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@SideOnly(Side.CLIENT)
public class BaseGuiContainer extends GuiContainer {

    /**
     * 机器的 BaseContainer，内含其 TileEntity
     */
    protected BaseContainer container;

    // gui 数据
    protected Map<SideHandlerType, Int2ObjectMap<ImmutablePair<FluidStack, Integer>>> fluids;
    protected HandlerInfoMachineLifecycle.EnergyInfo energy;

    public BaseGuiContainer(BaseContainer container) {
        super(container);
        this.container = container;
        xSize = container.size.width;
        ySize = container.size.height;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int gui = container.provider.getGuiId();
        synchronized (this) {
            fluids = GuiDataFromServer.FLUIDS.get(gui);
            energy = GuiDataFromServer.ENERGIES.get(gui);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        renderHoveredFluid(mouseX, mouseY);
        renderHoveredEnergy(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(container.provider.getBackground());
        this.drawTexturedModalRect(getGuiLeft(), getGuiTop(), 0, 0, this.xSize, this.ySize);
        // 能量
        if (energy != null) {
            for (IGuiProvider.Size energyGui : container.size.energy) {
                renderGuiEnergy(energyGui);
            }
        }
        // 进度
        short processValue = container.getEnergyProcessed();
        for (IGuiProvider.Size process : container.size.process) {
            renderGuiProcess(process, processValue);
        }
        // 其他
        container.provider.onBackgroundRender(this, container, partialTicks, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        String name = container.getName();
        this.fontRenderer.drawString(name,
                (container.size.width - fontRenderer.getStringWidth(name)) / 2,
                container.provider.getSize().titleOffsetY, 0x404040);
        // 流体
        if (fluids != null) {
            renderGuiFluid();
        }
        // 其他
        container.provider.onForegroundRender(this, container, mouseX, mouseY);
    }

    private void renderGuiFluid() {
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        for (IGuiProvider.FluidSlotInfo slotInfo : container.provider.getFluids()) {
            SideHandlerType type = slotInfo.type;
            int slot = slotInfo.slotId;
            Int2ObjectMap<ImmutablePair<FluidStack, Integer>> typedFluids = fluids.get(type);
            if (typedFluids != null) {
                ImmutablePair<FluidStack, Integer> fluidPair = typedFluids.get(slot);
                if (fluidPair != null) {
                    float total = fluidPair.right;
                    FluidStack fluidStack = fluidPair.left;
                    if (fluidStack == null) {
                        fluidStack = new FluidStack(FluidRegistry.WATER, 0);
                    }
                    ResourceLocation stillTexture = fluidStack.getFluid().getStill();
                    ResourceLocation stillTexture2 = new ResourceLocation(stillTexture.getResourceDomain(), "textures/" + stillTexture.getResourcePath() + ".png");
                    mc.getTextureManager().bindTexture(stillTexture2);
                    if (slotInfo.isHorizontal) {
                        int w = (int) (slotInfo.w * fluidStack.amount / total);
                        this.drawTexturedModalRect(slotInfo.x, slotInfo.y, 0, 0, w, slotInfo.h);
                    } else {
                        int h = (int) (slotInfo.h * fluidStack.amount / total);
                        int y = slotInfo.y + slotInfo.h - h;
                        this.drawTexturedModalRect(slotInfo.x, y, 0, 0, slotInfo.w, h);
                    }
                }
            }
        }
    }

    private void renderGuiEnergy(IGuiProvider.Size energyGui) {
        int textureWidth = energy.capacity == 0 ? 0 : (int) (((float) energyGui.w) * energy.stored / energy.capacity);
        this.drawTexturedModalRect(getGuiLeft() + energyGui.x, getGuiTop() + energyGui.y, energyGui.u, energyGui.v, textureWidth, energyGui.h);
    }

    private void renderGuiProcess(IGuiProvider.Size process, short processValue) {
        int arrowWidth = process.w * processValue / Short.MAX_VALUE;
        this.drawTexturedModalRect(getGuiLeft() + process.x,
                getGuiTop() + process.y,
                process.u, process.v, arrowWidth, process.h);
    }

    private void renderHoveredFluid(int mouseX, int mouseY) {
        if (fluids != null) {
            for (IGuiProvider.FluidSlotInfo fluidPosition : container.getFluidPositions()) {
                if (mouseIn(mouseX, mouseY, fluidPosition.x, fluidPosition.y, fluidPosition.w, fluidPosition.h)) {
                    ImmutablePair<FluidStack, Integer> pair = fluids.get(fluidPosition.type).get(fluidPosition.slotId);
                    if (pair != null && pair.right > 0) {
                        FluidStack fluidStack = pair.left;
                        int total = pair.right;
                        List<String> texts = new ArrayList<>(2);
                        texts.add(fluidStack.getLocalizedName());
                        texts.add(fluidStack.amount + "/" + total);
                        drawHoveringText(texts, mouseX, mouseY);
                    }
                }
            }
        }
    }

    private void renderHoveredEnergy(int mouseX, int mouseY) {
        if (energy != null) {
            for (IGuiProvider.Size energyPosition : container.size.energy) {
                if (mouseIn(mouseX, mouseY, energyPosition.x, energyPosition.y, energyPosition.w, energyPosition.h)) {
                    drawHoveringText(Collections.singletonList(energy.stored + "/" + energy.capacity), mouseX, mouseY);
                }
            }
        }
    }

    protected boolean mouseIn(int mouseX, int mouseY, int x, int y, int w, int h) {
        int mx = mouseX - getGuiLeft();
        int my = mouseY - getGuiTop();
        return mx >= x
                && mx <= (x + w)
                && my >= y
                && my <= (y + h);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}

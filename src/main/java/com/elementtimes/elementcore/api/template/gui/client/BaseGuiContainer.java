package com.elementtimes.elementcore.api.template.gui.client;

import com.elementtimes.elementcore.api.template.capability.fluid.FluidTankInfo;
import com.elementtimes.elementcore.api.template.gui.server.BaseContainer;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.IGuiProvider;
import com.elementtimes.elementcore.common.network.GuiDataNetwork;
import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * GuiContainer
 * @author luqin2007
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@OnlyIn(Dist.CLIENT)
public class BaseGuiContainer extends ContainerScreen<BaseContainer> {

    /**
     * 机器的 BaseContainer，内含其 TileEntity
     */
    protected BaseContainer container;

    // gui 数据
    protected GuiDataNetwork mGuiData = null;

    public BaseGuiContainer(BaseContainer container) {
        super(container, container.player.inventory, container.provider.getDisplayName());
        this.container = container;
        xSize = container.size.width;
        ySize = container.size.height;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        int gui = container.provider.getGuiId();
        synchronized (this) {
            mGuiData = GuiDataNetwork.DATA.get(gui);
        }
        super.render(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        renderHoveredFluid(mouseX, mouseY);
        renderHoveredEnergy(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color3f(1.0F, 1.0F, 1.0F);
        Objects.requireNonNull(this.minecraft).getTextureManager().bindTexture(container.provider.getBackground());
        // 取代 drawTexturedModalRect
        blit(getGuiLeft(), getGuiTop(), 0, 0, xSize, ySize);
        if (mGuiData != null) {
            // 能量
            for (IGuiProvider.Size energyGui : container.size.energy) {
                renderGuiEnergy(energyGui);
            }
            // 进度
            float processValue = 1f;
            if (mGuiData.total != 0) {
                processValue = ((float) mGuiData.process) / mGuiData.total;
            }
            for (IGuiProvider.Size process : container.size.process) {
                renderGuiProcess(process, processValue);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        String name = container.getName();
        this.font.drawString(name,
                (container.size.width - font.getStringWidth(name)) / 2,
                container.provider.getSize().titleOffsetY, 0x404040);
        // 流体
        renderGuiFluid();
    }

    private void renderGuiFluid() {
        GlStateManager.color3f(1.0F, 1.0F, 1.0F);
        for (IGuiProvider.FluidSlotInfo slotInfo : container.provider.getFluids()) {
            int slot = slotInfo.slotId;
            Int2ObjectMap<FluidTankInfo> typedFluids;
            switch (slotInfo.type) {
                case INPUT:
                    typedFluids = mGuiData.fluidInputs;
                    break;
                case OUTPUT:
                    typedFluids = mGuiData.fluidOutputs;
                    break;
                default:
                    typedFluids = mGuiData.fluidOthers;
            }
            if (typedFluids != null) {
                FluidTankInfo tank = typedFluids.get(slot);
                if (tank != null) {
                    // TODO: 获取 Fluid 材质
                    ResourceLocation texture = tank.fluid.getRegistryName();
                    minecraft.getTextureManager().bindTexture(texture);
                    if (slotInfo.isHorizontal) {
                        int w = (int) (slotInfo.w * tank.amount / tank.capacity);
                        this.blit(slotInfo.x, slotInfo.y, 0, 0, w, slotInfo.h);
                    } else {
                        int h = (int) (slotInfo.h * tank.amount / tank.capacity);
                        int y = slotInfo.y + slotInfo.h - h;
                        this.blit(slotInfo.x, y, 0, 0, slotInfo.w, h);
                    }
                }
            }
        }
    }

    private void renderGuiEnergy(IGuiProvider.Size energyGui) {
        int textureWidth = mGuiData.capacity == 0 ? 0 : (int) (((float) energyGui.w) * mGuiData.stored / mGuiData.capacity);
        this.blit(getGuiLeft() + energyGui.x, getGuiTop() + energyGui.y, energyGui.u, energyGui.v, textureWidth, energyGui.h);
    }

    private void renderGuiProcess(IGuiProvider.Size process, float processValue) {
        int arrowWidth = (int) (process.w * processValue);
        this.blit(getGuiLeft() + process.x,
                getGuiTop() + process.y,
                process.u, process.v, arrowWidth, process.h);
    }

    private void renderHoveredFluid(int mouseX, int mouseY) {
        for (IGuiProvider.FluidSlotInfo fluidPosition : container.getFluidPositions()) {
            if (mouseIn(mouseX, mouseY, fluidPosition.x, fluidPosition.y, fluidPosition.w, fluidPosition.h)) {
                Int2ObjectMap<FluidTankInfo> typedFluids;
                switch (fluidPosition.type) {
                    case INPUT:
                        typedFluids = mGuiData.fluidInputs;
                        break;
                    case OUTPUT:
                        typedFluids = mGuiData.fluidOutputs;
                        break;
                    default:
                        typedFluids = mGuiData.fluidOthers;
                }
                FluidTankInfo tank = typedFluids.get(fluidPosition.slotId);
                List<String> texts = new ArrayList<>(2);
                // TODO: 流体名称
                texts.add(I18n.format(tank.fluid.getRegistryName().toString()));
                texts.add(tank.amount + "/" + tank.capacity);
                renderTooltip(texts, mouseX, mouseY);
            }
        }
    }

    private void renderHoveredEnergy(int mouseX, int mouseY) {
        if (mGuiData != null) {
            for (IGuiProvider.Size energyPosition : container.size.energy) {
                if (mouseIn(mouseX, mouseY, energyPosition.x, energyPosition.y, energyPosition.w, energyPosition.h)) {
                    renderTooltip(Collections.singletonList(mGuiData.stored + "/" + mGuiData.capacity), mouseX, mouseY);
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
}

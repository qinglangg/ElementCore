package com.elementtimes.elementcore.api.template.gui.client;

import com.elementtimes.elementcore.api.template.gui.server.BaseContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;

/**
 * 对所有只有一个输入和一个输出的机器的 GUI 抽象
 *
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
public class BaseGuiContainerElectrical extends BaseGuiContainer {

    private final int[][] processPos, energyPos;

    public BaseGuiContainerElectrical(BaseContainer inventorySlotsIn, String modid, String texture, int textOffsetY,
                                      int xProcess, int yProcess, int xEnergy, int yEnergy) {
        this(inventorySlotsIn, modid, texture, textOffsetY,
                new int[][] { new int[] {xProcess, yProcess, 0, inventorySlotsIn.getHeight(), 24, 17} },
                new int[][] { new int[] {xEnergy, yEnergy, 24, inventorySlotsIn.getHeight(), 90, 4} });
    }

    public BaseGuiContainerElectrical(BaseContainer inventorySlotsIn, String modid, String texture, int textOffsetY,
                                      int xProcess, int yProcess, int uProcess, int vProcess, int wProcess, int hProcess,
                                      int xEnergy, int yEnergy, int uEnergy, int vEnergy, int wEnergy, int hEnergy) {
        this(inventorySlotsIn, modid, texture, textOffsetY,
                new int[][] { new int[] {xProcess, yProcess, uProcess, vProcess, wProcess, hProcess} },
                new int[][] { new int[] {xEnergy, yEnergy, uEnergy, vEnergy, wEnergy, hEnergy} });
    }

    public BaseGuiContainerElectrical(BaseContainer inventorySlotsIn, String modid, String texture, int textOffsetY,
                                      int[][] processXYUVWH,
                                      int xEnergy, int yEnergy, int uEnergy, int vEnergy, int wEnergy, int hEnergy) {
        this(inventorySlotsIn, modid, texture, textOffsetY, processXYUVWH,
                new int[][] { new int[] {xEnergy, yEnergy, uEnergy, vEnergy, wEnergy, hEnergy} });
    }

    public BaseGuiContainerElectrical(BaseContainer inventorySlotsIn, String modid, String texture, int textOffsetY,
                                      int xProcess, int yProcess, int uProcess, int vProcess, int wProcess, int hProcess,
                                      int[][] energyXYUVWH) {
        this(inventorySlotsIn, modid, texture, textOffsetY,
                new int[][] { new int[] {xProcess, yProcess, uProcess, vProcess, wProcess, hProcess} }, energyXYUVWH);
    }

    public BaseGuiContainerElectrical(BaseContainer inventorySlotsIn, String modid, String texture, int textOffsetY, int[][] processXYUVWH, int[][] energyXYUVWH) {
        super(inventorySlotsIn, new ResourceLocation(modid, "textures/gui/" + texture + ".png"), textOffsetY);
        processPos = processXYUVWH;
        energyPos = energyXYUVWH;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredEnergy(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        int offsetX = (this.width - this.xSize) / 2, offsetY = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);

        if (energy != null) {
            // 能量条
            for (int[] energyGui : energyPos) {
                int textureWidth;
                if (energy.capacity == 0) {
                    textureWidth = 0;
                } else {
                    textureWidth = (int) (((float) energyGui[4]) * energy.stored / energy.capacity);
                }
                this.drawTexturedModalRect(offsetX + energyGui[0], offsetY + energyGui[1], energyGui[2], energyGui[3], textureWidth, energyGui[5]);
            }

            // 箭头
            short processValue = container.getEnergyProcessed();
            for (int[] process : processPos) {
                int arrowWidth = process[4] * processValue / Short.MAX_VALUE;
                this.drawTexturedModalRect(offsetX + process[0], offsetY + process[1], process[2], process[3], arrowWidth, process[5]);
            }
        }
    }

    private void renderHoveredEnergy(int mouseX, int mouseY) {
        if (energy != null) {
            for (int[] energyPosition : energyPos) {
                if (mouseIn(mouseX, mouseY, energyPosition[0], energyPosition[1], energyPosition[4], energyPosition[5])) {
                    drawHoveringText(Collections.singletonList(energy.stored + "/" + energy.capacity), mouseX, mouseY);
                }
            }
        }
    }
}

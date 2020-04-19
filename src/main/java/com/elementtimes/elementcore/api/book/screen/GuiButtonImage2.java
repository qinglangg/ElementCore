package com.elementtimes.elementcore.api.book.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 对于使用了不符合标准（256x256）的材质，使用此按钮
 * @author 鹿钦
 */
@SideOnly(Side.CLIENT)
public class GuiButtonImage2 extends GuiButtonImage {

    protected final ResourceLocation icon;
    protected final int texX;
    protected final int texY;
    protected final int diffY;
    protected final float texW;
    protected final float texH;

    public GuiButtonImage2(int buttonId, int x, int y,
                           int widthIn, int heightIn, int xTexStart, int yTexStart, int yDiffText,
                           float texWidth, float texHeight, ResourceLocation icon) {
        super(buttonId, x, y, widthIn, heightIn, xTexStart, yTexStart, yDiffText, icon);
        this.icon = icon;
        this.texX = xTexStart;
        this.texY = yTexStart;
        this.diffY = yDiffText;
        this.texW = texWidth;
        this.texH = texHeight;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            mc.getTextureManager().bindTexture(icon);
            GlStateManager.disableDepth();
            Gui.drawModalRectWithCustomSizedTexture(x, y, texX, texY + diffY, width, height, texW, texH);
            GlStateManager.enableDepth();
        }
    }
}

package com.elementtimes.elementcore.api.book.screen;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 绘制图片
 * 若图片符合标准 Gui 图片尺寸，即 256x256，则 textureWidth 和 textureHeight 可忽略
 * @author luqin2007
 */
public class Image extends BaseContent {

    protected ResourceLocation mTexture;
    protected DrawStage mDrawStage;
    protected int mOffsetX;
    protected int mWidth, mHeight;
    protected int mTextureWidth, mTextureHeight;
    protected int mU, mV;
    protected boolean mTemp;

    public Image(ResourceLocation texture, DrawStage drawStage, int offsetX, int width, int height, int textureWidth, int textureHeight, int u, int v, boolean isTemp) {
        mTexture = texture;
        mDrawStage = drawStage;
        mOffsetX = offsetX;
        mWidth = width;
        mHeight = height;
        mTextureWidth = textureWidth;
        mTextureHeight = textureHeight;
        mU = u;
        mV = v;
        mTemp = isTemp;
    }

    public Image(ResourceLocation texture, DrawStage drawStage, int offsetX, int width, int height, int textureWidth, int textureHeight, int u, int v) {
        this(texture, drawStage, offsetX, width, height, textureWidth, textureHeight, u, v, false);
    }

    public Image(ResourceLocation texture, int width, int height) {
        this(texture, DrawStage.BACKGROUND, 0, width, height, 0, 0, 0, 0);
    }

    public Image(String domain, String path, int width, int height) {
        this(new ResourceLocation(domain, "/textures/" + path), DrawStage.BACKGROUND, 0, width, height, 0, 0, 0, 0);
    }

    public Image(ResourceLocation texture, int width, int height, int u, int v) {
        this(texture, DrawStage.BACKGROUND, 0, width, height, 0, 0, u, v);
    }

    public Image(String domain, String path, int width, int height, int u, int v) {
        this(new ResourceLocation(domain, "/textures/" + path), DrawStage.BACKGROUND, 0, width, height, 0, 0, u, v);
    }

    public Image(ResourceLocation texture, int width, int height, int textureWidth, int textureHeight, int u, int v) {
        this(texture, DrawStage.BACKGROUND, 0, width, height, textureWidth, textureHeight, u, v);
    }

    public Image(String domain, String path, int width, int height, int textureWidth, int textureHeight, int u, int v) {
        this(new ResourceLocation(domain, "/textures/" + path), DrawStage.BACKGROUND, 0, width, height, textureWidth, textureHeight, u, v);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int draw(int x, int y, int spaceX, int spaceY, int mouseX, int mouseY) {
        BookGuiContainer container = getGuiContainer();
        GlStateManager.color(1f, 1f, 1f);
        container.mc.getTextureManager().bindTexture(mTexture);
        x += mOffsetX;
        if (mTextureWidth > 0 && mTextureHeight > 0) {
            Gui.drawModalRectWithCustomSizedTexture(x, y, mU, mV, mWidth, mHeight, mTextureWidth, mTextureHeight);
        } else {
            container.drawTexturedModalRect(x, y, mU, mV, mWidth, mHeight);
        }
        return y + mHeight;
    }

    @Override
    public DrawStage getStage() {
        return mDrawStage;
    }

    @Override
    public boolean isTemp() {
        return mTemp;
    }
}

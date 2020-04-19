package com.elementtimes.elementcore.api.book.screen;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 绘制悬浮文字
 * @author luqin2007
 */
public class HoverText extends BaseContent {

    protected int mMouseStartX, mMouseEndX;
    protected int mMouseStartY, mMouseEndY;
    protected boolean mRelative;
    protected List<ITextComponent> mTexts;

    public HoverText(int mouseStartX, int mouseEndX, int mouseStartY, int mouseEndY, boolean isRelative, List<ITextComponent> texts) {
        mMouseStartX = mouseStartX;
        mMouseEndX = mouseEndX;
        mMouseStartY = mouseStartY;
        mMouseEndY = mouseEndY;
        mTexts = texts;
        mRelative = isRelative;
    }

    public HoverText(int mouseStartX, int mouseEndX, int mouseStartY, int mouseEndY, boolean isRelative, ITextComponent... texts) {
        this(mouseStartX, mouseEndX, mouseStartY, mouseEndY, isRelative, Arrays.asList(texts));
    }

    public HoverText(int mouseStartX, int mouseEndX, int mouseStartY, int mouseEndY, boolean isRelative, String... texts) {
        this(mouseStartX, mouseEndX, mouseStartY, mouseEndY, isRelative,
                Arrays.stream(texts).map(TextComponentTranslation::new).collect(Collectors.toList()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int draw(int x, int y, int spaceX, int spaceY, int mouseX, int mouseY) {
        if (isIn(spaceX, spaceY, mouseX, mouseY)) {
            BookGuiContainer container = getGuiContainer();
            container.drawHoveringText(mTexts.stream()
                    .map(ITextComponent::getFormattedText)
                    .collect(Collectors.toList())
                    , mouseX - container.getGuiLeft(), mouseY - container.getGuiTop());
        }
        return y;
    }

    private boolean isIn(int spaceX, int spaceY, int mouseX, int mouseY) {
        int mouseStartX = mRelative ? mMouseStartX + spaceX : mMouseStartX;
        int mouseStartY = mRelative ? mMouseStartY + spaceY : mMouseStartY;
        int mouseEndX = mRelative ? mMouseEndX + spaceX : mMouseEndX;
        int mouseEndY = mRelative ? mMouseEndY + spaceY : mMouseEndY;
        return mouseStartX <= mouseX && mouseX <= mouseEndX && mouseStartY <= mouseY && mouseY <= mouseEndY;
    }

    @Override
    public DrawStage getStage() {
        return DrawStage.FOREGROUND;
    }

    @Override
    public boolean isTemp() {
        return false;
    }
}

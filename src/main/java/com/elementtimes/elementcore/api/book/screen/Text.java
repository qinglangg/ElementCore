package com.elementtimes.elementcore.api.book.screen;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 添加一段文字，会自动换行
 * @author luqin2007
 */
public class Text extends BaseContent {

    protected int mColor;
    protected Supplier<String> mText;
    protected DrawStage mDrawStage;
    protected int mOffsetX;
    protected List<IContent> mNextPage;
    protected boolean mTemp;

    public Text(int color, Supplier<String> text, DrawStage drawStage, int offsetX, boolean isTemp) {
        mColor = color;
        mText = text;
        mDrawStage = drawStage;
        mOffsetX = offsetX;
        mNextPage = new ArrayList<>();
        mTemp = isTemp;
    }

    public Text(int color, Supplier<String> text, DrawStage drawStage, int offsetX) {
        this(color, text, drawStage, offsetX, false);
    }

    public Text(String text) {
        this(0x404040, () -> text, DrawStage.BACKGROUND, 0);
    }

    public Text(String text, int offsetX) {
        this(0x404040, () -> text, DrawStage.BACKGROUND, offsetX);
    }

    public Text(ITextComponent text) {
        this(0x404040, text::getFormattedText, DrawStage.BACKGROUND, 0);
    }

    public Text(ITextComponent text, int offsetX) {
        this(0x404040, text::getFormattedText, DrawStage.BACKGROUND, offsetX);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int draw(int x, int y, int spaceX, int spaceY, int mouseX, int mouseY) {
        BookGuiContainer container = getGuiContainer();
        String text = mText.get();
        int textWidth = container.mc.fontRenderer.getStringWidth(text);
        int textHeight = container.mc.fontRenderer.getWordWrappedHeight(text, textWidth);
        x += mOffsetX;
        spaceX -= mOffsetX;
        if (spaceX >= textWidth && spaceY >= textHeight) {
            container.mc.fontRenderer.drawString(text, x, y, mColor);
            return y + textHeight;
        }
        // width
        String[] lines;
        if (textWidth > spaceX) {
            List<String> lineArray = new ArrayList<>();
            String[] split = text.split("\n");
            for (String s : split) {
                int start = 0;
                int end = start;
                int length = 0;
                while (end < s.length()) {
                    char c = s.charAt(end);
                    length += container.mc.fontRenderer.getCharWidth(c);
                    end++;
                    if (length >= spaceX) {
                        lineArray.add(s.substring(start, end));
                        start = end;
                        length = 0;
                    }
                }
                if (start != end) {
                    lineArray.add(s.substring(start, end));
                }
            }
            lines = lineArray.toArray(new String[0]);
        } else {
            lines = text.split("\n");
        }
        // draw
        boolean display = true;
        for (String s : lines) {
            if (display) {
                int subTextHeight = container.mc.fontRenderer.getWordWrappedHeight(s, container.mc.fontRenderer.getStringWidth(s));
                if (subTextHeight <= spaceY) {
                    container.mc.fontRenderer.drawString(s, x, y, mColor);
                    spaceY -= subTextHeight;
                    y += subTextHeight;
                } else {
                    display = false;
                }
            } else {
                mNextPage.add(new Text(mColor, () -> s, mDrawStage, mOffsetX, true));
            }
        }
        return y;
    }

    @Override
    public DrawStage getStage() {
        return mDrawStage;
    }

    @Override
    public IContent[] split() {
        return mNextPage.toArray(new IContent[0]);
    }

    @Override
    public boolean isTemp() {
        return mTemp;
    }
}
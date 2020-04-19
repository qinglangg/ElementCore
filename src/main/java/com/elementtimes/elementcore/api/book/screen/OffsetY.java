package com.elementtimes.elementcore.api.book.screen;

/**
 * 对于 Y 轴的改变
 * @author luqin2007
 */
public class OffsetY extends BaseContent {

    private int mOffsetY;
    private DrawStage mStage;

    public OffsetY(int offsetY, DrawStage stage) {
        mOffsetY = offsetY;
        mStage = stage;
    }

    public OffsetY(int offsetY) {
        mOffsetY = offsetY;
        mStage = DrawStage.BACKGROUND;
    }

    @Override
    public int draw(int x, int y, int spaceX, int spaceY, int mouseX, int mouseY) {
        return y + Math.min(mOffsetY, spaceY);
    }

    @Override
    public DrawStage getStage() {
        return mStage;
    }

    @Override
    public boolean isTemp() {
        return false;
    }
}

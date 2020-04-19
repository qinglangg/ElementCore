package com.elementtimes.elementcore.api.book.screen;

import java.util.function.IntFunction;

/**
 * 按钮
 * 构造中传入一个 GuiButton 的创建方法，参数为 id 在创建时自动分配
 * @author luqin2007
 */
public class Button extends BaseContent {

    protected IntFunction<Object> mButton;

    public Button(IntFunction<Object> button) {
        mButton = button;
    }

    @Override
    public int draw(int x, int y, int spaceX, int spaceY, int mouseX, int mouseY) {
        return getGuiContainer()
                .addButton((net.minecraft.client.gui.GuiButton) mButton.apply(getGuiContainer().nextButtonId())).height;
    }

    @Override
    public DrawStage getStage() {
        return DrawStage.INIT;
    }

    @Override
    public boolean isTemp() {
        return false;
    }
}

package com.elementtimes.elementcore.api.book.screen;

import com.elementtimes.elementcore.api.book.BookContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * 代表一个绘制的内容
 * @author luqin2007
 */
public interface IContent {

    /**
     * 分割控件，若控件没有完全显示，则分割控件到下一页
     * draw 方法调用后，当绘制完成后再无剩余空间时调用该方法
     */
    @SideOnly(Side.CLIENT)
    default IContent[] split() {
        return new IContent[0];
    }

    /**
     * 绘制内容
     * @param x 该页 X 轴绘制起始坐标
     * @param y 该页 Y 轴绘制起始坐标
     * @param spaceX 该页 X 轴剩余空间
     * @param spaceY 该页 Y 轴剩余空间
     * @param mouseX 鼠标 X 坐标
     * @param mouseY 鼠标 Y 坐标
     * @return 绘制后，下一个控件的绘制 Y 轴起始位置（即 y 参数）
     */
    int draw(int x, int y, int spaceX, int spaceY, int mouseX, int mouseY);

    /**
     * 绘制阶段
     * @return 绘制阶段
     */
    DrawStage getStage();

    /**
     * 是否为临时控件
     * 临时控件指的是，由于书籍剩余空间无法显示时，添加的用于显示未显示内容的控件
     * 这些控件在窗口关闭时应当删除
     * @return 临时
     */
    boolean isTemp();

    /**
     * 获取 BookGuiContainer 对象，用于绘制
     * 当 DrawStage == CONTAINER 时，其值为 null
     */
    @SideOnly(Side.CLIENT)
    BookGuiContainer getGuiContainer();

    /**
     * 设置 BookGuiContainer 对象
     */
    @SideOnly(Side.CLIENT)
    void setGuiContainer(BookGuiContainer container);

    /**
     * 获取 BookContainer 对象，用于绘制
     */
    BookContainer getContainer();

    /**
     * 设置 BookContainer 对象
     */
    void setContainer(BookContainer container);

    /**
     * 按钮响应
     * 当添加的是按钮时，重写该方法处理按钮响应
     * @param button 按钮
     * @throws IOException 异常？
     */
    @SideOnly(Side.CLIENT)
    default void actionPerformed(net.minecraft.client.gui.GuiButton button) throws IOException {}

    /**
     * 若显示完成后需要替换成其他控件，重写该方法
     * @return 替换后的控件
     */
    @Nullable
    default IContent replaceAfterDisplay() {
        return this;
    }

    /**
     * 当关闭书籍时，调用此方法，多用于无法完全显示的增加的临时控件的自我删除
     * @return 替换后的控件
     */
    @Nullable
    default IContent replaceAfterClose() {
        return isTemp() ? null : this;
    }
}

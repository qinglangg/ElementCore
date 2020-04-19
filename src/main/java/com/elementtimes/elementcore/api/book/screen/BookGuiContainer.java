package com.elementtimes.elementcore.api.book.screen;

import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.api.book.BookContainer;
import com.elementtimes.elementcore.api.book.Page;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
public class BookGuiContainer extends GuiContainer {

    public static ResourceLocation BACKGROUND = new ResourceLocation(ElementCore.MODID, "textures/gui/book.png");

    protected BookContainer mContainer;

    protected int buttonId = 0;
    protected GuiButton prev, next;
    protected int xStart, yStart, xSpace, ySpace, xLength, yLast, yLength;

    public BookGuiContainer(BookContainer container) {
        super(container);
        mContainer = container;
        xSize = 300;
        ySize = 232;
        draw(DrawStage.CONSTRUCTOR);
    }

    public int nextButtonId() {
        return buttonId++;
    }

    @Override
    public void initGui() {
        super.initGui();
        xStart = guiLeft + 43;
        yStart = guiTop + 18;
        xLength = 215;
        yLength = 197;
        xSpace = xLength;
        ySpace = yLength;
        yLast = yStart;
        prev = new GuiButtonImage2(nextButtonId(),   8 + guiLeft, 202 + guiTop, 16, 17, 0, 240, 0, 512, 256, BACKGROUND);
        next = new GuiButtonImage2(nextButtonId(), 280 + guiLeft, 202 + guiTop, 16, 16, 16, 240, 0, 512, 256, BACKGROUND);
        addButton(prev);
        addButton(next);
        draw(DrawStage.INIT);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1f, 1f, 1f);
        mc.getTextureManager().bindTexture(BACKGROUND);
        drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, xSize, ySize, 512, 256);
        draw(DrawStage.BACKGROUND, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        if (mouseY > next.y && mouseY < next.y + next.height) {
            if (mouseX > next.x && mouseX < next.x + next.width) {
                drawHoveringText("Next Page", mouseX - guiLeft, mouseY - guiTop);
            } else if (mouseX > prev.x && mouseX < prev.x + prev.width) {
                drawHoveringText("Previous Page", mouseX - guiLeft, mouseY - guiTop);
            }
        }
        draw(DrawStage.FOREGROUND, mouseX, mouseY);
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button == prev) {
            mContainer.prevPage();
        } else if (button == next) {
            mContainer.nextPage();
        }
    }

    public void setLastY(int lastY) {
        yLast = lastY;
    }

    protected void draw(DrawStage stage, int mouseX, int mouseY) {
        yLast = yStart;
        ySpace = yLength;
        Page page = mContainer.getPage();
        List<IContent> contents = page.getContents(stage);
        Iterator<IContent> iterator = contents.iterator();
        boolean draw = true;
        while (iterator.hasNext()) {
            IContent content = iterator.next();
            content.setGuiContainer(this);
            content.setContainer(mContainer);
            if (draw) {
                // draw
                int newY = content.draw(xStart, yLast, xSpace, ySpace, mouseX, mouseY);
                int height = newY - yLast;
                ySpace -= height;
                yLast = newY;
                // replace
                if (ySpace <= 0) {
                    draw = false;
                    mContainer.newPage().addAll(content.split());
                }
                IContent afterDisplay = content.replaceAfterDisplay();
                if (afterDisplay == null) {
                    iterator.remove();
                } else {
                    page.replace(content, afterDisplay);
                }
            } else {
                mContainer.newPage().add(content);
            }
        }
    }

    protected void draw(DrawStage stage) {
        draw(stage, Mouse.getX(), Mouse.getY());
    }

    @Override
    public <T extends GuiButton> T addButton(T buttonIn) {
        return super.addButton(buttonIn);
    }
}

package com.elementtimes.elementcore.api.book;

import com.elementtimes.elementcore.api.book.screen.DrawStage;
import com.elementtimes.elementcore.api.book.screen.IContent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

public class BookContainer extends Container {

    protected int slotId = 0;
    protected IBook mBook;
    protected int mPage = 0;
    protected int xLength = 215;
    protected int yLength = 197;

    public BookContainer(IBook book) {
        mBook = book;
        if (getTotalPage() == 0) {
            mBook.getPages().add(new Page(mBook));
        }
        // draw
        Page page = getPage();
        List<IContent> contents = page.getContents(DrawStage.CONTAINER);
        Iterator<IContent> iterator = contents.iterator();
        boolean draw = true;
        int yLast = 0;
        int ySpace = yLength;
        while (iterator.hasNext()) {
            IContent content = iterator.next();
            content.setContainer(this);
            if (draw) {
                // draw
                int newY = content.draw(0, yLast, xLength, yLength, 0, 0);
                int height = newY - yLast;
                ySpace -= height;
                yLast = newY;
                // replace
                if (ySpace <= 0) {
                    draw = false;
                    newPage().addAll(content.split());
                }
                IContent afterDisplay = content.replaceAfterDisplay();
                if (afterDisplay == null) {
                    iterator.remove();
                } else {
                    page.replace(content, afterDisplay);
                }
            } else {
                newPage().add(content);
            }
        }
    }

    public int nextSlotId() {
        return slotId++;
    }

    public IBook getBook() {
        return mBook;
    }

    public int getTotalPage() {
        return getBook().getPages().size();
    }

    public int getIndex() {
        jumpTo(mPage);
        return mPage;
    }

    public Page getPage() {
        return getBook().getPages().get(getIndex());
    }

    public Page getPage(int index) {
        if (index < 0) {
            return getBook().getPages().get(0);
        } else if (index >= getTotalPage()) {
            return getBook().getPages().get(getTotalPage() - 1);
        }
        return getBook().getPages().get(getIndex());
    }

    public void nextPage() {
        jumpTo(getIndex() + 1);
    }

    public void prevPage() {
        jumpTo(getIndex() - 1);
    }

    public void jumpTo(int page) {
        mPage = Math.min(Math.max(0, page), getTotalPage() - 1);
    }

    @Override
    public Slot addSlotToContainer(Slot slotIn) {
        return super.addSlotToContainer(slotIn);
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        return true;
    }

    public Page newPage() {
        Page head = getPage().getPageHead();
        Page page = getPage().newTempPage();
        int totalPage = getTotalPage();
        int i = getIndex();
        while (i < totalPage && getPage(i).getPageHead() == head) {
            i++;
        }
        mBook.getPages().add(Math.min(i, totalPage), page);
        return page;
    }

    @Override
    public void onContainerClosed(@Nonnull EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        mBook.resume();
    }
}

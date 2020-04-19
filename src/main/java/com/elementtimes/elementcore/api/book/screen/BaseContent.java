package com.elementtimes.elementcore.api.book.screen;

import com.elementtimes.elementcore.api.book.BookContainer;

/**
 * @author luqin2007
 */
public abstract class BaseContent implements IContent {

    protected BookGuiContainer mGuiContainer;
    protected BookContainer mContainer;

    @Override
    public BookGuiContainer getGuiContainer() {
        return mGuiContainer;
    }

    @Override
    public void setGuiContainer(BookGuiContainer container) {
        mGuiContainer = container;
    }

    @Override
    public BookContainer getContainer() {
        return mContainer;
    }

    @Override
    public void setContainer(BookContainer container) {
        mContainer = container;
    }
}

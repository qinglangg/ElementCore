package com.elementtimes.elementcore.api.book;

import com.elementtimes.elementcore.api.book.screen.DrawStage;
import com.elementtimes.elementcore.api.book.screen.IContent;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;

public class Page {

    private final IBook mBook;
    private final Map<DrawStage, ArrayList<IContent>> mContents;
    protected Page mPageHead, mPageNext;

    public Page(IBook book) {
        mBook = book;
        mPageHead = this;
        mPageNext = null;
        mContents = new HashMap<>();
    }

    public ArrayList<IContent> getContents(DrawStage stage) {
        if (mContents.containsKey(stage)) {
            return mContents.get(stage);
        } else {
            ArrayList<IContent> contents = new ArrayList<>();
            mContents.put(stage, contents);
            return contents;
        }
    }

    public IBook getBook() {
        return mBook;
    }

    public Page newTempPage() {
        if (mPageNext == null) {
            mPageNext = new Page(mBook);
            mPageNext.mPageHead = mPageHead;
        }
        return mPageNext;
    }

    public Page getPageHead() {
        return mPageHead;
    }

    public void add(IContent content) {
        getContents(content.getStage()).add(content);
    }

    public void addAll(Collection<IContent> contents) {
        contents.forEach(this::add);
    }

    public void addAll(IContent... contents) {
        if (contents == null || contents.length == 0) {
            return;
        }
        for (IContent content : contents) {
            add(content);
        }
    }

    public void clear(DrawStage stage) {
        getContents(stage).clear();
    }

    public void clear() {
        mContents.clear();
    }

    public void remove(IContent content) {
        getContents(content.getStage()).remove(content);
    }

    public void remove(DrawStage stage, int index) {
        getContents(stage).remove(index);
    }

    public void replace(@Nonnull IContent from, IContent to) {
        if (from == to || to == null) {
            return;
        }
        ArrayList<IContent> list = mContents.get(from.getStage());
        int index = list.indexOf(from);
        if (index >= 0) {
            list.set(index, to);
        }
    }

    public void forEach(DrawStage stage, Consumer<? super IContent> consumer) {
        getContents(stage).forEach(consumer);
    }

    public void resume(Iterator<Page> iterator) {
        resume();
        if (this != mPageHead) {
            iterator.remove();
        }
    }

    protected void resume() {
        if (mPageHead == null || mPageHead == this) {
            for (DrawStage stage : DrawStage.values()) {
                if (mContents.containsKey(stage)) {
                    ArrayList<IContent> contents = mContents.get(stage);
                    for (int i = contents.size() - 1; i >= 0; i--) {
                        IContent content = contents.get(i);
                        IContent c = content.replaceAfterClose();
                        if (c == null) {
                            contents.remove(i);
                        } else if (c != content) {
                            contents.set(i, c);
                        }
                    }
                }
            }
        } else {
            for (DrawStage stage : DrawStage.values()) {
                if (mContents.containsKey(stage)) {
                    ArrayList<IContent> contents = mPageHead.getContents(stage);
                    for (IContent content : mContents.get(stage)) {
                        IContent c = content.replaceAfterClose();
                        if (c != content) {
                            contents.add(c);
                        }
                    }
                }
            }
        }
    }
}

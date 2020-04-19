package com.elementtimes.elementcore.api.template.lifecycle;

import com.elementtimes.elementcore.api.interfaces.block.IMachineLifecycle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 用于世界替换，如挖掘，抽水等
 * @author luqin2007
 */
public abstract class WorldReplaceLifecycle<ELEMENT, REPLACE> implements IMachineLifecycle {

    protected List<Wrapper> mElements = new ArrayList<>();
    protected List<ELEMENT> mElementFind = new ArrayList<>();

    /**
     * 从世界中寻找可替代的元素，将其添加到 List<ELEMENT> elements 中
     * 当该列表为空时，机器不运行
     * @param elements 可替代的元素
     */
    public abstract void findElements(List<ELEMENT> elements);

    /**
     * 判断是否会查找可用元素
     * @return 是否查找
     */
    public abstract boolean canFind();

    /**
     * 将原本可替代的元素转换为新的元素。该方法会在寻找到要替代的元素时调用一次
     * @param from 原元素
     * @return 新元素
     */
    public abstract REPLACE convert(ELEMENT from);

    /**
     * 从世界中移除旧元素。一般在该方法中移除旧元素，放置新元素
     * {@link Wrapper#isRemoved} 属性是一个元素完全破坏的标志。该方法会在 {@link Wrapper#isRemoved} 为 true 前不断调用，且 {@link Wrapper#tickRemove} 自增
     * @param element 原元素及其移除时间
     */
    public abstract void removeOldElement(Wrapper element);

    /**
     * 收集新元素。
     * 该方法会与 removeOldElement 同时开始调用，模拟边破坏边收取的操作
     * {@link Wrapper#isCollected} 为一个元素收集完成的标志，{@link Wrapper#isCollected} 为 true 时停止调用
     * @param element 原元素及其移除时间
     */
    public abstract void collectElement(Wrapper element);

    /**
     * 存储必要的数据
     * 注意：该方法会在每 tick 结束时主动调用，但恢复数据需要手动调用 loadSavedData 方法
     * @param elements 移除中的元素
     */
    public abstract void save(List<Wrapper> elements);

    /**
     * 恢复数据，该方法需要手动调用
     * @param elements 移除中的元素
     */
    public void loadSavedData(List<Wrapper> elements) {
        mElements.clear();
        mElements.addAll(elements);
        mElementFind.clear();
    }

    @Override
    public void onStart() {
        if (canFind()) {
            findElements(mElementFind);
            mElementFind.forEach(element -> mElements.add(new Wrapper(element)));
            mElementFind.clear();
        }
    }

    @Override
    public boolean onLoop() {
        // replace
        Iterator<Wrapper> iterator = mElements.iterator();
        while (iterator.hasNext()) {
            Wrapper element = iterator.next();
            if (!element.isRemoved) {
                removeOldElement(element);
                element.tickRemove++;
            }
            if (!element.isCollected) {
                collectElement(element);
                element.tickCollect++;
            }
            if (element.isRemoved && element.isCollected) {
                iterator.remove();
            }
        }
        return true;
    }

    @Override
    public boolean onCheckFinish() {
        return mElements.isEmpty();
    }

    @Override
    public void onTickFinish() {
        save(mElements);
    }

    public class Wrapper {
        ELEMENT element;
        REPLACE collect;
        int tickRemove;
        int tickCollect;
        boolean isRemoved;
        boolean isCollected;

        public ELEMENT getElement() {
            return element;
        }

        public void setElement(ELEMENT element) {
            this.element = element;
        }

        public REPLACE getCollect() {
            return collect;
        }

        public int getRemoveTick() {
            return tickRemove;
        }

        public int getCollectTick() {
            return tickCollect;
        }

        public boolean isRemoved() {
            return isRemoved;
        }

        public void setRemoved(boolean removed) {
            isRemoved = removed;
        }

        public boolean isCollected() {
            return isCollected;
        }

        public void setCollected(boolean collected) {
            isCollected = collected;
        }

        Wrapper(ELEMENT element) {
            this(element, convert(element), 0, 0, false, false);
        }

        public Wrapper(ELEMENT element, REPLACE collect, int tickRemove, int tickCollect, boolean isRemoved, boolean isCollected) {
            this.element = element;
            this.collect = collect;
            this.tickRemove = tickRemove;
            this.tickCollect = tickCollect;
            this.isRemoved = isRemoved;
            this.isCollected = isCollected;
        }
    }
}

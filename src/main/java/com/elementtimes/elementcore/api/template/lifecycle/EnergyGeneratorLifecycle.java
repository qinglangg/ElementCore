package com.elementtimes.elementcore.api.template.lifecycle;

import com.elementtimes.elementcore.api.interfaces.block.IMachineLifecycle;
import net.minecraft.tileentity.TileEntity;

/**
 * 发电机的生命周期
 * @author luqin2007
 */
public class EnergyGeneratorLifecycle<T extends TileEntity> implements IMachineLifecycle {

    private TileEntity mTe;
    private IStrategy mStrategy;

    public EnergyGeneratorLifecycle(T te) {
        this(te, Strategies.AVERAGE);
    }

    public EnergyGeneratorLifecycle(T te, IStrategy strategy) {
        mTe = te;
        mStrategy = strategy;
    }

    public void setStrategy(IStrategy strategy) {
        mStrategy = strategy;
    }

    public IStrategy getStrategy() {
        return mStrategy;
    }

    public TileEntity getTe() {
        return mTe;
    }

    @Override
    public void onTickFinish() {
        if (mTe.hasWorld() && !mTe.isRemoved()) {
            mStrategy.send(this);
        }
    }

    /**
     * @see Strategies
     */
    @FunctionalInterface
    public interface IStrategy {

        /**
         * 发送能量
         * 发电机 TileEntity 可以从 getTe 方法获取
         * @param context EnergyGeneratorLifecycle 上下文
         */
        void send(EnergyGeneratorLifecycle<? extends TileEntity> context);
    }
}

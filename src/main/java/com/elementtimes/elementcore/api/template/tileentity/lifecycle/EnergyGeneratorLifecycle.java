package com.elementtimes.elementcore.api.template.tileentity.lifecycle;

import com.elementtimes.elementcore.api.template.capability.EnergyHandler;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.IMachineLifecycle;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.ITileEnergyHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

/**
 * 发电机的生命周期
 * @author luqin2007
 */
public class EnergyGeneratorLifecycle implements IMachineLifecycle {

    private TileEntity mTe;
    private ITileEnergyHandler mHandler;

    public EnergyGeneratorLifecycle(TileEntity te, ITileEnergyHandler handler) {
        mTe = te;
        mHandler = handler;
    }

    @Override
    public void onTickFinish() {
        if (mTe.getWorld() != null) {
            for (EnumFacing value : EnumFacing.values()) {
                EnergyHandler.EnergyProxy proxy = mHandler.getEnergyProxy(value);
                mHandler.sendEnergy(proxy.getEnergyStored(), value.getOpposite(),
                        mTe.getWorld().getTileEntity(mTe.getPos().offset(value)), proxy);
            }
        }
    }
}

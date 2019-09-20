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
public class EnergyGeneratorLifecycle <T extends TileEntity & ITileEnergyHandler> implements IMachineLifecycle {

    private TileEntity mTe;

    public EnergyGeneratorLifecycle(T te) {
        mTe = te;
    }

    @Override
    public void onTickFinish() {
        if (mTe.getWorld() != null) {
            for (EnumFacing value : EnumFacing.values()) {
                EnergyHandler.EnergyProxy proxy = ((ITileEnergyHandler) mTe).getEnergyProxy(value);
                ((ITileEnergyHandler) mTe).sendEnergy(proxy.getEnergyStored(), value.getOpposite(),
                        mTe.getWorld().getTileEntity(mTe.getPos().offset(value)), proxy);
            }
        }
    }
}

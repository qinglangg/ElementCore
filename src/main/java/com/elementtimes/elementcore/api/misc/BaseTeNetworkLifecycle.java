package com.elementtimes.elementcore.api.misc;

import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.api.interfaces.block.IMachineLifecycle;
import com.elementtimes.elementcore.api.misc.data.BaseTeMsg;
import com.elementtimes.elementcore.api.template.block.BaseTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public class BaseTeNetworkLifecycle implements IMachineLifecycle {

    private BaseTileEntity mTileEntity;
    private BaseTeMsg mTeMsg = null;

    public BaseTeNetworkLifecycle(BaseTileEntity te) {
        mTileEntity = te;
    }

    @Override
    public void onTickFinish() {
        BaseTeMsg msg = new BaseTeMsg(mTileEntity);
        if (!msg.equals(mTeMsg)) {
            mTeMsg = msg;
            for (ServerPlayerEntity player : mTileEntity.getOpenedPlayers()) {
                ElementCore.CONTAINER.elements.sendTo(msg, player);
            }
        }
    }
}

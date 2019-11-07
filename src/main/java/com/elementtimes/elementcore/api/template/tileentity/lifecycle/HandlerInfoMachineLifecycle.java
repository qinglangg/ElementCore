package com.elementtimes.elementcore.api.template.tileentity.lifecycle;

import com.elementtimes.elementcore.api.template.tileentity.interfaces.IMachineLifecycle;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 用于向客户端传递信息
 * @author luqin2007
 */
public class HandlerInfoMachineLifecycle implements IMachineLifecycle {

    private Supplier<List<PlayerEntity>> player;
    private Consumer<PlayerEntity> message;

    public HandlerInfoMachineLifecycle(Supplier<List<PlayerEntity>> player,
                                        Consumer<PlayerEntity> message) {
        this.player = player;
        this.message = message;
    }

    @Override
    public void onTickFinish() {
        player.get().forEach(message);
    }
}

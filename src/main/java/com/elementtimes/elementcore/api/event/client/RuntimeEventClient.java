package com.elementtimes.elementcore.api.event.client;

import com.elementtimes.elementcore.api.ECModElements;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 用于游戏过程中的客户端事件
 * @author luqin2007
 */
@OnlyIn(Dist.CLIENT)
public class RuntimeEventClient {

    private ECModElements mElements;

    public RuntimeEventClient(ECModElements elements) {
        mElements = elements;
    }

    @SubscribeEvent
    public void onItemStackTooltips(ItemTooltipEvent event) {
        mElements.client().items.tooltips().forEach(tt -> tt.addTooltip(event.getItemStack(), event.getToolTip()));
        mElements.client().blocks.tooltips().forEach(tt -> tt.addTooltip(event.getItemStack(), event.getToolTip()));
    }
}

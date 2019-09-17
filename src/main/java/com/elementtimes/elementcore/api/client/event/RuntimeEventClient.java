package com.elementtimes.elementcore.api.client.event;

import com.elementtimes.elementcore.api.common.ECModElements;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * 用于游戏过程中的客户端事件
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
public class RuntimeEventClient {

    private ECModElements mElements;

    public RuntimeEventClient(ECModElements elements) {
        mElements = elements;
    }

    @SubscribeEvent
    public void onItemStackTooltips(ItemTooltipEvent event) {
        final ItemStack itemStack = event.getItemStack();
        final List<String> toolTip = event.getToolTip();
        mElements.toolTips.forEach(tt -> tt.addTooltip(itemStack, toolTip));
    }
}

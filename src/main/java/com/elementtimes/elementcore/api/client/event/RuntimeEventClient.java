package com.elementtimes.elementcore.api.client.event;

import com.elementtimes.elementcore.api.common.ECModContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 用于游戏过程中的客户端事件
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
public class RuntimeEventClient {

    private ECModContainer mContainer;

    public RuntimeEventClient(ECModContainer container) {
        mContainer = container;
    }

    @SubscribeEvent
    public void onItemStackTooltips(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        List<String> toolTip = event.getToolTip();
        mContainer.elements().getClientElements().tooltips.forEach(consumer -> consumer.accept(itemStack, toolTip));
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Map<KeyBinding, BiConsumer<InputEvent.KeyInputEvent, KeyBinding>> events = mContainer.elements().getClientElements().keyEvents;
        if (!events.isEmpty()) {
            for (KeyBinding keyBinding : events.keySet()) {
                if (keyBinding.isPressed()) {
                    events.get(keyBinding).accept(event, keyBinding);
                    break;
                }
            }
        }
    }
}

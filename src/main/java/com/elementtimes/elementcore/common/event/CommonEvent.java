package com.elementtimes.elementcore.common.event;

import com.elementtimes.elementcore.common.CoreElements;
import com.elementtimes.elementcore.common.item.DebugStick;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CommonEvent {

    @SubscribeEvent
    public static void onLighting(EntityStruckByLightningEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ItemEntity) {
            ItemStack item = ((ItemEntity) entity).getItem();
            if (item.getItem() == CoreElements.itemDebugger) {
                ItemStack drop;
                String[] typeAndServer = DebugStick.getTypeAndServer(item);
                if (DebugStick.TYPE_DEBUG.equals(typeAndServer[0])) {
                    if (DebugStick.SIDE_SERVER.equals(typeAndServer[1])) {
                        drop = DebugStick.STACK_DEBUG_CLIENT.get();
                    } else {
                        drop = DebugStick.STACK_TOOL_SERVER.get();
                    }
                } else {
                    drop = DebugStick.STACK_DEBUG_SERVER.get();
                }
                InventoryHelper.spawnItemStack(entity.world, entity.posX, entity.posY, entity.posZ, drop);
                entity.remove();
                event.setCanceled(true);
            }
        }
    }
}

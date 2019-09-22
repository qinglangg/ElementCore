package com.elementtimes.elementcore.common.event;

import com.elementtimes.elementcore.ElementCore;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class CommonEvent {

    @SubscribeEvent
    public static void onLighting(EntityStruckByLightningEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityItem) {
            ItemStack item = ((EntityItem) entity).getItem();
            if (item.getItem() == ElementCore.Items.debugger) {
                int metadata = item.getMetadata();
                ItemStack drop;
                if (metadata == 0b0000) {
                    drop = new ItemStack(ElementCore.Items.debugger, 1, 0b0001);
                } else if (metadata == 0b0001) {
                    drop = new ItemStack(ElementCore.Items.debugger, 1, 0b0010);
                } else {
                    drop = new ItemStack(ElementCore.Items.debugger, 1, 0b0000);
                }
                InventoryHelper.spawnItemStack(entity.world, entity.posX, entity.posY, entity.posZ, drop);
                entity.setDead();
                event.setCanceled(true);
            }
        }
    }
}

package com.elementtimes.elementcore.api.event;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.ITileTer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Iterator;

/**
 * 用于游戏过程中的的事件
 * @author luqin2007
 */
public class RuntimeEvent {

    private ECModElements mElements;

    public RuntimeEvent(ECModElements elements) {
        mElements = elements;
    }

    @SubscribeEvent
    public void onBurningTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack itemStack = event.getItemStack();
        Item item = itemStack.getItem();
        String itemName = item.getRegistryName().toString();
        if (mElements.items.burningTimes().containsKey(itemName)) {
            event.setBurnTime(mElements.items.burningTimes().getInt(itemName));
            return;
        }
        Block block = Block.getBlockFromItem(item);
        if (block != Blocks.AIR) {
            String name = block.getRegistryName().toString();
            if (mElements.blocks.burningTimes().containsKey(name)) {
                event.setBurnTime(mElements.blocks.burningTimes().getInt(name));
                return;
            }
        }
        for (FlowingFluid fluid : mElements.fluids.burningTimes().keySet()) {
            if (fluid.getFilledBucket() == item) {
                event.setBurnTime(mElements.fluids.burningTimes().getInt(fluid));
                return;
            }
        }
    }

    @SubscribeEvent
    public void onTickServerEvent(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side == LogicalSide.SERVER && !ITileTer.RENDER_POS.isEmpty()) {
            Iterator<BlockPos> iterator = ITileTer.RENDER_POS.iterator();
            while (iterator.hasNext()) {
                render(event.world, iterator.next());
                iterator.remove();
            }
        }
    }

    private void render(World world, BlockPos pos) {
        if (world instanceof ServerWorld && world.isAreaLoaded(pos, 1)) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof ITileTer) {
                tileEntity.markDirty();
                world.markForRerender(pos);
            }
        }
    }
}

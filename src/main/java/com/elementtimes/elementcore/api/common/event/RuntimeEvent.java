package com.elementtimes.elementcore.api.common.event;

import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.api.common.net.TESRRenderNetwork;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.ITileTESR;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Iterator;

/**
 * 用于游戏过程中的的事件
 * @author luqin2007
 */
@Mod.EventBusSubscriber
public class RuntimeEvent {

    @SubscribeEvent
    public static void onTickServerEvent(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side == Side.SERVER) {
            Iterator<BlockPos> iterator = ITileTESR.renderDirty.iterator();
            while (iterator.hasNext()) {
                render(event.world, iterator.next());
                iterator.remove();
            }
        }
    }

    private static void render(World world, BlockPos pos) {
        if (world instanceof WorldServer && world.isBlockLoaded(pos)) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof ITileTESR) {
                tileEntity.markDirty();
                ITileTESR te = (ITileTESR) tileEntity;
                PlayerChunkMapEntry entry = ((WorldServer) world).getPlayerChunkMap().getEntry(pos.getX() >> 4, pos.getZ() >> 4);
                if (entry != null) {
                    for (EntityPlayerMP player : entry.getWatchingPlayers()) {
                        ElementCore.instance().container.elements.simpleChannel.
                                sendTo(new TESRRenderNetwork(te.writeRenderNbt(new NBTTagCompound()), world.provider.getDimension(), pos), player);
                    }
                }
            }
        }
    }
}

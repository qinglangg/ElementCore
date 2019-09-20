package com.elementtimes.elementcore.api.common.event;

import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.ITileTESR;
import com.elementtimes.elementcore.common.network.TESRRenderNetwork;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

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
        ECUtils.common.runWithModActive(mElements.container.mod, () -> {
            ItemStack itemStack = event.getItemStack();
            String name = null;
            if (FluidRegistry.isUniversalBucketEnabled() && itemStack.getItem() == ForgeModContainer.getInstance().universalBucket) {
                Optional<IFluidTankProperties> fluidBucket = Arrays.stream(Objects.requireNonNull(FluidUtil.getFluidHandler(itemStack)).getTankProperties()).findFirst();
                if (fluidBucket.isPresent()) {
                    Fluid fluid = Objects.requireNonNull(fluidBucket.get().getContents()).getFluid();
                    if (fluid != null) {
                        name = fluid.getName();
                    }
                }
            }
            int time = mElements.fluidBurningTimes == null ? -1 : mElements.fluidBurningTimes.getOrDefault(name, -1);
            if (time > 0) {
                event.setBurnTime(time);
            }
        }, event);
    }

    @SubscribeEvent
    public void onTickServerEvent(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side == Side.SERVER) {
            Iterator<BlockPos> iterator = ITileTESR.renderDirty.iterator();
            while (iterator.hasNext()) {
                render(event.world, iterator.next());
                iterator.remove();
            }
        }
    }

    private void render(World world, BlockPos pos) {
        if (world instanceof WorldServer && world.isBlockLoaded(pos)) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof ITileTESR) {
                tileEntity.markDirty();
                ITileTESR te = (ITileTESR) tileEntity;
                PlayerChunkMapEntry entry = ((WorldServer) world).getPlayerChunkMap().getEntry(pos.getX() >> 4, pos.getZ() >> 4);
                if (entry != null) {
                    for (EntityPlayerMP player : entry.getWatchingPlayers()) {
                        ElementCore.instance().container.elements.channel.
                                sendTo(new TESRRenderNetwork(te.writeRenderNbt(new NBTTagCompound()), world.provider.getDimension(), pos), player);
                    }
                }
            }
        }
    }
}

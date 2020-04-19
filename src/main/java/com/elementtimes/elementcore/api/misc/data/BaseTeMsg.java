package com.elementtimes.elementcore.api.misc.data;

import com.elementtimes.elementcore.api.annotation.ModSimpleNetwork;
import com.elementtimes.elementcore.api.annotation.part.Method;
import com.elementtimes.elementcore.api.template.block.BaseTileEntity;
import com.elementtimes.elementcore.api.template.capability.ProxyTankHandler;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@ModSimpleNetwork(
        encoder = @Method(value = BaseTeMsg.class, name = "encoder"),
        decoder = @Method(value = BaseTeMsg.class, name = "decoder"),
        handler = @Method(value = BaseTeMsg.class, name = "handler"))
public class BaseTeMsg {

    public int id;
    public int x, y, z;
    public int energy;
    public int processed, unprocessed;
    public Int2ObjectMap<FluidStack> fluids = new Int2ObjectArrayMap<>();

    public BaseTeMsg() {}

    public BaseTeMsg(BaseTileEntity te) {
        id = te.getGuiId();
        BlockPos pos = te.getPos();
        x = pos.getX();
        y = pos.getY();
        z = pos.getZ();
        energy = te.getEnergyHandler().getEnergyStored();
        ProxyTankHandler handler = te.getTanks();
        for (int i = 0; i < handler.getTanks(); i++) {
            FluidStack fluidStack = handler.getFluidInTank(i);
            if (!fluidStack.isEmpty()) {
                fluids.put(i, fluidStack);
            }
        }
        processed = te.getEnergyProcessed();
        unprocessed = te.getEnergyUnprocessed();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        BaseTeMsg that = (BaseTeMsg) object;

        if (id != that.id) {
            return false;
        }
        if (x != that.x) {
            return false;
        }
        if (y != that.y) {
            return false;
        }
        if (z != that.z) {
            return false;
        }
        if (processed != that.processed) {
            return false;
        }
        if (unprocessed != that.unprocessed) {
            return false;
        }
        if (energy != that.energy) {
            return false;
        }
        if (fluids.size() != that.fluids.size()) {
            return false;
        }
        for (Int2ObjectMap.Entry<FluidStack> entry : fluids.int2ObjectEntrySet()) {
            FluidStack thisFluid = entry.getValue();
            FluidStack thatFluid = that.fluids.get(entry.getIntKey());
            if (thatFluid == null) {
                return false;
            }
            if (!thisFluid.isFluidStackIdentical(thatFluid)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        result = 31 * result + id;
        result = 31 * result + energy;
        result = 31 * result + processed;
        result = 31 * result + unprocessed;
        result = 31 * result + (fluids != null ? fluids.hashCode() : 0);
        return result;
    }

    public static BaseTeMsg decoder(PacketBuffer buffer) {
        BaseTeMsg msg = new BaseTeMsg();
        msg.id = buffer.readInt();
        msg.x = buffer.readInt();
        msg.y = buffer.readInt();
        msg.z = buffer.readInt();
        msg.energy = buffer.readInt();
        msg.processed = buffer.readInt();
        msg.unprocessed = buffer.readInt();
        int size = buffer.readInt();
        msg.fluids = new Int2ObjectArrayMap<>(size);
        for (int i = 0; i < size; i++) {
            int key = buffer.readInt();
            FluidStack value = buffer.readFluidStack();
            msg.fluids.put(key, value);
        }
        return msg;
    }

    public static void encoder(BaseTeMsg msg, PacketBuffer buffer) {
        buffer.writeInt(msg.id);
        buffer.writeInt(msg.x);
        buffer.writeInt(msg.y);
        buffer.writeInt(msg.z);
        buffer.writeInt(msg.energy);
        buffer.writeInt(msg.processed);
        buffer.writeInt(msg.unprocessed);
        buffer.writeInt(msg.fluids.size());
        for (Int2ObjectMap.Entry<FluidStack> entry : msg.fluids.int2ObjectEntrySet()) {
            buffer.writeInt(entry.getIntKey());
            buffer.writeFluidStack(entry.getValue());
        }
    }

    public static void handler(BaseTeMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        BlockPos pos = new BlockPos(msg.x, msg.y, msg.z);
        World world = net.minecraft.client.Minecraft.getInstance().world;
        Block block = world.getBlockState(pos).getBlock();
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof BaseTileEntity && msg.id == ((BaseTileEntity) te).getGuiId()) {
            BaseTileEntity tileEntity = (BaseTileEntity) te;
            tileEntity.setEnergyProcessed(msg.processed);
            tileEntity.setEnergyUnprocessed(msg.unprocessed);
            tileEntity.getEnergyHandler().setEnergy(msg.energy);
            Int2ObjectMap<FluidStack> fluids = msg.fluids;
            ProxyTankHandler tanks = tileEntity.getTanks();
            for (int i = 0; i < tanks.getTanks(); i++) {
                tanks.setFluidIgnoreCheck(i, fluids.getOrDefault(i, FluidStack.EMPTY));
            }
        }
    }
}

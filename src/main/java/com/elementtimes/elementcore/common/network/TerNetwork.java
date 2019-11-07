package com.elementtimes.elementcore.common.network;

import com.elementtimes.elementcore.api.annotation.ModNetwork;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.ITileTer;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 用于 SupportStand 的 Ter 传递
 * @author luqin2007
 */
@ModNetwork()
public class TerNetwork {

    public CompoundNBT nbt;
    public int dim;
    public BlockPos pos;

    public TerNetwork() {}

    public TerNetwork(CompoundNBT nbt, int dim, BlockPos pos) {
        this.nbt = nbt;
        this.dim = dim;
        this.pos = pos;
    }

    @ModNetwork.Decoder
    public static TerNetwork decoder(PacketBuffer buffer) {
        TerNetwork network = new TerNetwork();
        network.dim = buffer.readInt();
        network.pos = buffer.readBlockPos();
        network.nbt = buffer.readCompoundTag();
        return network;
    }

    @ModNetwork.Encoder
    public static void encoder(TerNetwork network, PacketBuffer buffer) {
        buffer.writeInt(network.dim);
        buffer.writeBlockPos(network.pos);
        buffer.writeCompoundTag(network.nbt);
    }

    @ModNetwork.Consumer
    public static void consumer(TerNetwork msg, Supplier<NetworkEvent.Context> contextSupplier) {
        if (ECUtils.common.isClient()) {
            TileEntity te = Minecraft.getInstance().world.getTileEntity(msg.pos);
            if (te instanceof ITileTer) {
                ((ITileTer) te).receiveRenderMessage(msg.nbt);
            }
        }
    }
}

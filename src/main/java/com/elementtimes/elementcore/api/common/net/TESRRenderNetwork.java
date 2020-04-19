package com.elementtimes.elementcore.api.common.net;

import com.elementtimes.elementcore.api.annotation.ModSimpleNetwork;
import com.elementtimes.elementcore.api.annotation.part.Getter;
import com.elementtimes.elementcore.api.common.net.handler.TESRRenderHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Objects;

/**
 * 用于 SupportStand 的 TESR 传递
 * @author luqin2007
 */
@ModSimpleNetwork(value = @Getter(TESRRenderHandler.class), side = Side.CLIENT)
public class TESRRenderNetwork implements IMessage {

    public NBTTagCompound nbt;
    public int dim;
    public BlockPos pos;
    public boolean isValid = false;

    public TESRRenderNetwork() { }

    public TESRRenderNetwork(NBTTagCompound nbt, int dim, BlockPos pos) {
        this.nbt = nbt;
        this.dim = dim;
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            nbt = ByteBufUtils.readTag(buf);
            pos = NBTUtil.getPosFromTag(Objects.requireNonNull(ByteBufUtils.readTag(buf)));
            dim = buf.readInt();
            isValid = true;
        } catch (RuntimeException ignore) {}
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, nbt);
        ByteBufUtils.writeTag(buf, NBTUtil.createPosTag(pos));
        buf.writeInt(dim);
    }
}

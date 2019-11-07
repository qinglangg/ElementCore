package com.elementtimes.elementcore.common.block;

import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.template.block.BlockTileBase;
import com.elementtimes.elementcore.api.template.capability.fluid.vanilla.CapabilityFluidHandler;
import com.elementtimes.elementcore.api.template.capability.fluid.vanilla.FluidHandler;
import com.elementtimes.elementcore.api.template.capability.fluid.vanilla.interfaces.IFluidHandler;
import com.elementtimes.elementcore.api.template.fluid.FluidActionResult;
import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.ITileFluidHandler;
import com.elementtimes.elementcore.common.CoreElements;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

public class FluidBox extends BlockTileBase<FluidBox.FluidBoxTileEntity> {

    public static FluidBoxTileEntity creator() {
        return new FluidBoxTileEntity();
    }

    public FluidBox() {
        super(Block.Properties.create(Material.ROCK), FluidBoxTileEntity::new);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos,
                                    PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {
            ItemStack heldItem = player.getHeldItem(handIn);
            TileEntity fluidBox = worldIn.getTileEntity(pos);
            assert fluidBox != null;
            LazyOptional<IFluidHandler> handlerOpt = fluidBox.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
            if (heldItem.getItem() == Items.BUCKET) {
                handlerOpt.ifPresent(handler -> {
                    int slot = worldIn.rand.nextInt(2);
                    Fluid fluid = handler.getFluid(slot);
                    int amount = ECUtils.fluid.getFilledAmount(fluid);
                    FluidActionResult transfer = ECUtils.fluid.transfer(null, handler, heldItem, amount);
                    player.setHeldItem(handIn, transfer.result);
                });
            } else if (heldItem.getItem() == Items.WATER_BUCKET) {
                handlerOpt.ifPresent(handler -> {
                    FluidActionResult transfer = ECUtils.fluid.transfer(Fluids.WATER, handler, heldItem, 0);
                    player.setHeldItem(handIn, transfer.result);
                });
            } else if (heldItem.getItem() == Items.LAVA_BUCKET) {
                handlerOpt.ifPresent(handler -> {
                    FluidActionResult transfer = ECUtils.fluid.transfer(Fluids.LAVA, handler, heldItem, 1);
                    player.setHeldItem(handIn, transfer.result);
                });
            }
        }
        return true;
    }

    public static class FluidBoxTileEntity extends TileEntity implements ITileFluidHandler {

        IFluidHandler mHandler = new FluidHandler(2, 16000);

        public FluidBoxTileEntity() {
            super(Registry.BLOCK_ENTITY_TYPE.getValue(CoreElements.blockFluid.getRegistryName()).get());
        }

        @Override
        public IFluidHandler getTanks(SideHandlerType type) {
            return mHandler;
        }

        @Override
        public void read(@Nonnull CompoundNBT compound) {
            super.read(compound);
            ITileFluidHandler.super.read(compound);
        }

        @Nonnull
        @Override
        public CompoundNBT write(@Nonnull CompoundNBT compound) {
            CompoundNBT nbt = super.write(compound);
            ITileFluidHandler.super.write(compound);
            return nbt;
        }
    }
}

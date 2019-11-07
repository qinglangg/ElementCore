package com.elementtimes.elementcore.common.block;

import com.elementtimes.elementcore.api.annotation.ModTileEntity;
import com.elementtimes.elementcore.api.template.block.BlockTileBase;
import com.elementtimes.elementcore.api.template.capability.EnergyHandler;
import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.ITileEnergyHandler;
import com.elementtimes.elementcore.api.template.tileentity.lifecycle.EnergyGeneratorLifecycle;
import com.elementtimes.elementcore.common.CoreElements;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 创造能量盒
 * @author luqin2007
 */
public class EnergyBox extends BlockTileBase<EnergyBox.EnergyBoxTileEntity> {

    public EnergyBox() {
        super(Block.Properties.create(Material.IRON), EnergyBoxTileEntity.class);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new EnergyBoxTileEntity();
    }

    public static class EnergyBoxTileEntity extends TileEntity implements ITileEnergyHandler, ITickable {

        private EnergyHandler mEnergyHandler;
        private EnergyGeneratorLifecycle mLifecycle;

        @ModTileEntity.TileEntityType("energy_box")
        static final TileEntityType<EnergyBoxTileEntity> TYPE = TileEntityType.Builder
                .create(EnergyBoxTileEntity::new, CoreElements.blockEnergy)
                .build(null);

        EnergyBoxTileEntity() {
            super(TYPE);
            mEnergyHandler = new EnergyHandler(-1, Integer.MAX_VALUE, Integer.MAX_VALUE);
            mLifecycle = new EnergyGeneratorLifecycle<>(this);
        }

        @Override
        public EnergyHandler getEnergyHandler() {
            return mEnergyHandler;
        }

        @Override
        public SideHandlerType getEnergyType(Direction facing) {
            return SideHandlerType.ALL;
        }

        @Override
        public void tick() {
            mLifecycle.onTickFinish();
        }

        @Override
        public void read(@Nonnull CompoundNBT nbt) {
            ITileEnergyHandler.super.read(nbt);
            super.deserializeNBT(nbt);
        }

        @Nonnull
        @Override
        public CompoundNBT write(@Nonnull CompoundNBT nbt) {
            ITileEnergyHandler.super.write(nbt);
            return nbt;
        }
    }
}

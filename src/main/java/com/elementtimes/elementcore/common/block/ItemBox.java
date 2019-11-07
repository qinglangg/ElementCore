package com.elementtimes.elementcore.common.block;

import com.elementtimes.elementcore.api.annotation.ModTileEntity;
import com.elementtimes.elementcore.api.template.block.BlockTileBase;
import com.elementtimes.elementcore.api.template.capability.item.IItemHandler;
import com.elementtimes.elementcore.api.template.capability.item.ItemHandler;
import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.ITileItemHandler;
import com.elementtimes.elementcore.common.CoreElements;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import javax.annotation.Nonnull;
import java.util.Collections;

public class ItemBox extends BlockTileBase<ItemBox.ItemBoxTileEntity> {

    public ItemBox() {
        super(Block.Properties.create(Material.GLASS), ItemBoxTileEntity::new);
    }

    public static class ItemBoxTileEntity extends TileEntity implements ITileItemHandler {

        @ModTileEntity.TileEntityType
        public static TileEntityType<ItemBoxTileEntity> TYPE = TileEntityType.Builder.create(ItemBoxTileEntity::new, CoreElements.blockItem).build(null);

        public ItemBoxTileEntity() {
            super(TYPE);
        }

        @Nonnull
        @Override
        public IItemHandler getItemHandler(@Nonnull SideHandlerType type) {
            return new ItemHandler(1);
        }

        @Override
        public boolean isInputValid(int slot, ItemStack stack) {
            return true;
        }
    }
}

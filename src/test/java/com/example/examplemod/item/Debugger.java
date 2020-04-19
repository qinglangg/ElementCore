package com.example.examplemod.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class Debugger extends Item {

    public Debugger(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        boolean useful = player != null
                && ((world.isRemote && this == Items.DebuggerClient) || (!world.isRemote && this == Items.DebuggerServer));
        if (useful) {
            BlockPos pos = context.getPos();
            BlockState state = world.getBlockState(pos);
            TileEntity entity = world.getTileEntity(pos);
            if (!state.isAir(world, pos)) {
                player.sendMessage(getName(world, state.getBlock()));
                player.sendMessage(new StringTextComponent(pos.toString()));
                if (entity == null) {
                    player.sendMessage(new StringTextComponent("No TestTileEntity"));
                } else {
                    displayMessage(player, entity.serializeNBT(), 0, "");
                }
            }
        }
        return ActionResultType.PASS;
    }

    private void displayMessage(PlayerEntity player, INBT nbt, int tab, String key) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tab; i++) {
            sb.append(" ");
        }
        if (!StringUtils.isNullOrEmpty(key)) {
            sb.append(key).append("=");
        }
        if (nbt instanceof NumberNBT || nbt instanceof StringNBT) {
            player.sendMessage(new StringTextComponent(sb.append(nbt).toString()));
        } else if (nbt instanceof CollectionNBT) {
            CollectionNBT<? extends INBT> list = (CollectionNBT<? extends INBT>) nbt;
            player.sendMessage(new StringTextComponent(sb.append("collection->").toString()));
            list.forEach(n -> {
                int nextTab = tab + 1;
                displayMessage(player, n, nextTab, "");
            });
        } else if (nbt instanceof CompoundNBT) {
            player.sendMessage(new StringTextComponent(sb.append("compound->").toString()));
            int nextTab = tab + 1;
            CompoundNBT compound = (CompoundNBT) nbt;
            for (String s : compound.keySet()) {
                displayMessage(player, compound.get(s), nextTab, s);
            }
        }
    }

    private ITextComponent getName(World world, Block block) {
        if (world.isRemote) {
            return block.getNameTextComponent();
        } else {
            return new TranslationTextComponent(block.getTranslationKey());
        }
    }
}

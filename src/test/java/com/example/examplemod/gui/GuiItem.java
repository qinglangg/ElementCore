package com.example.examplemod.gui;

import com.elementtimes.elementcore.api.annotation.ModItem;
import com.example.examplemod.group.Groups;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

@ModItem
public class GuiItem extends Item {

    public GuiItem() {
        super(new Properties().group(Groups.main));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if (!context.getWorld().isRemote && context.getPlayer() instanceof ServerPlayerEntity) {
            NetworkHooks.openGui((ServerPlayerEntity) context.getPlayer(), new INamedContainerProvider() {
                @Override
                public ITextComponent getDisplayName() {
                    return new StringTextComponent("Test GUI");
                }

                @Nullable
                @Override
                public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
                    return new ContainerItem(id, inv, null);
                }
            });
        }
        return ActionResultType.PASS;
    }
}

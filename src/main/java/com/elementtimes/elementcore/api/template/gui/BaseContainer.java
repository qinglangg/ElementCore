package com.elementtimes.elementcore.api.template.gui;

import com.elementtimes.elementcore.api.interfaces.gui.IGuiProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

/**
 * 一个机器的 Container
 * @author KSGFK create in 2019/3/9
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class BaseContainer extends Container {
    public IGuiProvider provider;
    public IGuiProvider.GuiSize size;
    public TileEntity tile;

    public BaseContainer(TileEntity tileEntity, PlayerEntity player) {
        this(tileEntity, (IGuiProvider) tileEntity, player);
    }

    public BaseContainer(TileEntity tileEntity, IGuiProvider provider, PlayerEntity player) {
        super(provider.getContainerType(), provider.getGuiId());
        this.tile = tileEntity;
        this.provider = provider;
        this.size = provider.getSize();

        if (size.hasPlayerInventory) {
            int line = 3, slotCount = 9;
            int offsetX = size.playerInventoryOffsetX;
            int offsetY = size.playerInventoryOffsetY;

            for (int i = 0; i < line; ++i) {
                for (int j = 0; j < slotCount; ++j) {
                    addSlot(new Slot(player.inventory, j + i * 9 + 9, offsetX + j * 18, offsetY + i * 18));
                }
            }
            for (int i = 0; i < slotCount; ++i) {
                addSlot(new Slot(player.inventory, i, offsetX + i * 18, offsetY + 58));
            }

            Slot[] slots = provider.getSlots();
            for (Slot slot : slots) {
                addSlot(slot);
            }
        }

        if (player instanceof ServerPlayerEntity) {
            provider.getOpenedPlayers().add((ServerPlayerEntity) player);
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity playerIn) {
        if (size.hasInteractDistanceLimit) {
            BlockPos pos = tile.getPos();
            return playerIn.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) <= size.maxInteractDistance;

        }
        return true;
    }

    /**
     * 来源于 https://github.com/Yaossg/SausageCore
     */
    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack newStack = slot.getStack(), oldStack = newStack.copy();
        boolean isMerged;
        int total = inventorySlots.size();
        // fix: Shift 转移问题
        // 按执行顺序先添加的是 player 的物品槽，Yaossg 用的应该是 4z 的那套方法，显然在这里判断槽位对应错了
        int bagIndex = 27, inventoryIndex = 36;
        if (index < bagIndex) {
            // 背包
            isMerged = mergeItemStack(newStack, 36, total, false)
                    || mergeItemStack(newStack, 27, 36, false);
        } else if (index < inventoryIndex) {
            // 物品栏
            isMerged = mergeItemStack(newStack, 36, total, false)
                    || mergeItemStack(newStack, 0, 27, false);
        } else {
            // 36-total 机器
            isMerged = mergeItemStack(newStack, 0, 36, true);
        }
        if (!isMerged) {
            return ItemStack.EMPTY;
        }
        if (newStack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        return oldStack;
    }

    public IGuiProvider.FluidSlotInfo[] getFluidPositions() {
        return provider.getFluids();
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        provider.onGuiClosed(playerIn);
        if (playerIn instanceof ServerPlayerEntity) {
            provider.getOpenedPlayers().remove(playerIn);
        }
    }
}

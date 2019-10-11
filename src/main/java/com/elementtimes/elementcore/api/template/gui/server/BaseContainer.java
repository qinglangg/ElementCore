package com.elementtimes.elementcore.api.template.gui.server;

import com.elementtimes.elementcore.api.template.tileentity.interfaces.IGuiProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

    public BaseContainer(TileEntity tileEntity, EntityPlayer player) {
        this(tileEntity, (IGuiProvider) tileEntity, player);
    }

    public BaseContainer(TileEntity tileEntity, IGuiProvider provider, EntityPlayer player) {
        this.tile = tileEntity;
        this.provider = provider;
        this.size = provider.getSize();

        if (size.hasPlayerInventory) {
            int line = 3, slotCount = 9;
            int offsetX = size.playerInventoryOffsetX;
            int offsetY = size.playerInventoryOffsetY;

            for (int i = 0; i < line; ++i) {
                for (int j = 0; j < slotCount; ++j) {
                    this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, offsetX + j * 18, offsetY + i * 18));
                }
            }
            for (int i = 0; i < slotCount; ++i) {
                this.addSlotToContainer(new Slot(player.inventory, i, offsetX + i * 18, offsetY + 58));
            }

            Slot[] slots = provider.getSlots();
            for (Slot slot : slots) {
                this.addSlotToContainer(slot);
            }
        }

        if (player instanceof EntityPlayerMP) {
            provider.getOpenedPlayers().add((EntityPlayerMP) player);
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        if (size.hasInteractDistanceLimit) {
            return playerIn.getDistanceSq(tile.getPos()) <= size.maxInteractDistance;

        }
        return true;
    }

    /**
     * 来源于 https://github.com/Yaossg/SausageCore
     */
    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
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

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        short processed = (short) (Short.MAX_VALUE * provider.getProcess());
        listeners.forEach(listener -> listener.sendWindowProperty(this, 0, processed));
    }

    private short mEnergyProcessed = 0;
    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int id, int data) {
        super.updateProgressBar(id, data);
        if (id == 0) {
            mEnergyProcessed = (short) data;
        }
    }

    /**
     * 获取已处理的能量（比例缩放），范围为 0-Short.MAX_VALUE，仅用于确定进度
     * @return 已处理能量（按比例缩放）
     */
    public short getEnergyProcessed() {
        return mEnergyProcessed;
    }

    /**
     * 获取该 GUI 标题，默认为所使用机器名
     * @return 该 GUI 标题，服务器获得则为 getLocalizedName 值
     */
    public String getName() {
        String localizedName = tile.getBlockType().getLocalizedName();
        if (tile.getWorld().isRemote) {
            return I18n.format(localizedName);
        }
		return localizedName;
    }

    public IGuiProvider.FluidSlotInfo[] getFluidPositions() {
        return provider.getFluids();
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        provider.onGuiClosed(playerIn);
        if (playerIn instanceof EntityPlayerMP) {
            provider.getOpenedPlayers().remove(playerIn);
        }
        int guiId = provider.getGuiId();
        if (playerIn.world.isRemote) {
            if (com.elementtimes.elementcore.api.template.gui.client.GuiDataFromServer.ENERGIES.containsKey(guiId)) {
                com.elementtimes.elementcore.api.template.gui.client.GuiDataFromServer.ENERGIES.remove(guiId);
            }
            if (com.elementtimes.elementcore.api.template.gui.client.GuiDataFromServer.FLUIDS.containsKey(guiId)) {
                com.elementtimes.elementcore.api.template.gui.client.GuiDataFromServer.FLUIDS.remove(guiId);
            }
        }
    }
}

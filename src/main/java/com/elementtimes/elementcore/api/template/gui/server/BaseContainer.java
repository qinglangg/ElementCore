package com.elementtimes.elementcore.api.template.gui.server;

import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.annotation.ModContainerType;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.IGuiProvider;
import com.elementtimes.elementcore.common.network.GuiDataNetwork;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * 一个机器的 Container
 * @author KSGFK create in 2019/3/9
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class BaseContainer extends Container {
    public IGuiProvider provider;
    public IGuiProvider.GuiSize size;
    public TileEntity tile;
    public PlayerEntity player;

    public static TileEntity TE = null;
    public static IGuiProvider GUI = null;

    @ModContainerType.ContainerFactory
    public static Container tileBaseGui(int windowId, PlayerInventory inventory) {
        if (TE != null && GUI != null) {
            return new BaseContainer(TE, GUI, inventory.player);
        }
        return null;
    }

    @ModContainerType.ScreenCreator
    @OnlyIn(Dist.CLIENT)
    public static net.minecraft.client.gui.screen.Screen tileBaseGui(Container container, PlayerInventory inventory, ITextComponent text) {
        return new com.elementtimes.elementcore.api.template.gui.client.BaseGuiContainer((BaseContainer) container);
    }

    public BaseContainer(TileEntity tileEntity, PlayerEntity player) {
        this(tileEntity, (IGuiProvider) tileEntity, player);
    }

    public BaseContainer(TileEntity tileEntity, IGuiProvider provider, PlayerEntity player) {
        super(provider.getContainerType(), provider.getGuiId());
        this.tile = tileEntity;
        this.provider = provider;
        this.size = provider.getSize();
        this.player = player;

        if (size.hasPlayerInventory) {
            int line = 3, slotCount = 9;
            for (int i = 0; i < line; ++i) {
                for (int j = 0; j < slotCount; ++j) {
                    inventorySlots.add(new Slot(player.inventory, j + i * 9 + 9,
                            size.playerInventoryOffsetX + j * 18, size.playerInventoryOffsetY + i * 18));
                }
            }
            for (int i = 0; i < slotCount; ++i) {
                inventorySlots.add(new Slot(player.inventory, i,
                        size.playerInventoryOffsetX + i * 18, size.playerInventoryOffsetY + 58));
            }
            inventorySlots.addAll(Arrays.asList(provider.getSlots()));
        }

        if (player != null) {
            provider.getOpenedPlayers().add(player);
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

    /**
     * 获取该 GUI 标题，默认为所使用机器名
     * @return 该 GUI 标题，服务器获得则为 getLocalizedName 值
     */
    public String getName() {
        String localizedName = tile.getBlockState().getBlock().getTranslationKey();
        if (ECUtils.common.isClient()) {
            return net.minecraft.client.resources.I18n.format(localizedName);
        }
		return localizedName;
    }

    public IGuiProvider.FluidSlotInfo[] getFluidPositions() {
        return provider.getFluids();
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        provider.onGuiClosed(playerIn);
        provider.getOpenedPlayers().remove(playerIn);
        int guiId = provider.getGuiId();
        if (GuiDataNetwork.DATA.containsKey(guiId)) {
            GuiDataNetwork.DATA.remove(guiId);
        }
    }
}

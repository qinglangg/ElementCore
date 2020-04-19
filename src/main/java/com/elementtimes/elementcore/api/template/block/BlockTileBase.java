package com.elementtimes.elementcore.api.template.block;

import com.elementtimes.elementcore.api.interfaces.block.IDismantleBlock;
import com.elementtimes.elementcore.api.interfaces.gui.IGuiProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 需要带有 TestTileEntity 的方块时继承此类
 *
 * @author KSGFK create in 2019/2/17
 */
@SuppressWarnings("WeakerAccess")
public class BlockTileBase extends Block implements IDismantleBlock {

    private Supplier<? extends TileEntity> mTeCreator;
    private INamedContainerProvider mContainer = null;

    public BlockTileBase(Properties properties, Supplier<? extends TileEntity> teCreator) {
        super(properties);
        mTeCreator = teCreator;
    }

    public BlockTileBase(Properties properties, TileEntityType<? extends TileEntity> teType) {
        this(properties, teType::create);
    }

    public BlockTileBase(Properties properties, ResourceLocation name) {
        this(properties, Objects.requireNonNull(ForgeRegistries.TILE_ENTITIES.getValue(name))::create);
    }

    public BlockTileBase(Properties properties, String namespace, String path) {
        this(properties, new ResourceLocation(namespace, path));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return mTeCreator != null;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return mTeCreator == null ? null : mTeCreator.get();
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {
            ItemStack stack = playerIn.getHeldItem(hand);
            TileEntity te = worldIn.getTileEntity(pos);
            if (te != null) {
                // fluid
                try {
                    IFluidHandlerItem fluidHandlerStack = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElseThrow(NullPointerException::new);
                    IFluidHandler fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, hit.getFace()).orElseThrow(NullPointerException::new);
                    if (transfer(fluidHandler, fluidHandlerStack, playerIn, hand)) {
                        return true;
                    }
                } catch (NullPointerException ignored) { }
                // item
                try {
                    IItemHandler itemHandlerStack = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(NullPointerException::new);
                    IItemHandler itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, hit.getFace()).orElseThrow(NullPointerException::new);
                    if (transfer(itemHandler, itemHandlerStack)) {
                        return true;
                    }
                } catch (NullPointerException ignored) { }
                // energy
                try {
                    IEnergyStorage energyHandlerStack = stack.getCapability(CapabilityEnergy.ENERGY).orElseThrow(NullPointerException::new);
                    IEnergyStorage energyHandler = te.getCapability(CapabilityEnergy.ENERGY, hit.getFace()).orElseThrow(NullPointerException::new);
                    if (transfer(energyHandler, energyHandlerStack)) {
                        return true;
                    }
                } catch (NullPointerException ignored) { }
            }
            if (mContainer != null) {
                NetworkHooks.openGui((ServerPlayerEntity) playerIn, mContainer);
                return true;
            } else {
                TileEntity e = worldIn.getTileEntity(pos);
                if (e instanceof IGuiProvider) {
                    NetworkHooks.openGui((ServerPlayerEntity) playerIn, (IGuiProvider) e);
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public void setContainer(INamedContainerProvider container) {
        mContainer = container;
    }

    private boolean transfer(IFluidHandler teHandler, IFluidHandlerItem itemHandler, PlayerEntity player, Hand hand) {
        if (teHandler != null && itemHandler != null) {
            if (transferOnce(teHandler, itemHandler)) {
                player.setHeldItem(hand, itemHandler.getContainer());
                return true;
            }
            if (transferOnce(itemHandler, teHandler)) {
                player.setHeldItem(hand, itemHandler.getContainer());
                return true;
            }
        }
        return false;
    }

    private boolean transfer(IItemHandler teHandler, IItemHandler itemHandler) {
        if (teHandler != null && itemHandler != null) {
            if (transferOnce(teHandler, itemHandler)) {
                return true;
            }
            return transferOnce(itemHandler, teHandler);
        }
        return false;
    }

    private boolean transfer(IEnergyStorage teHandler, IEnergyStorage itemHandler) {
        if (teHandler != null && itemHandler != null) {
            if (transferOnce(teHandler, itemHandler)) {
                return true;
            }
            return transferOnce(itemHandler, teHandler);
        }
        return false;
    }

    private boolean transferOnce(IFluidHandler drainHandler, IFluidHandler fillHandler) {
        FluidStack drain = drainHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        if (!drain.isEmpty()) {
            int fill = fillHandler.fill(drain, IFluidHandler.FluidAction.SIMULATE);
            drain = drain.copy();
            drain.setAmount(fill);
            FluidStack drainReal = drainHandler.drain(drain, IFluidHandler.FluidAction.SIMULATE);
            if (drainReal.getAmount() == fill) {
                drainHandler.drain(drainReal, IFluidHandler.FluidAction.EXECUTE);
                fillHandler.fill(drainReal, IFluidHandler.FluidAction.EXECUTE);
                return true;
            }
        }
        return false;
    }

    private boolean transferOnce(IItemHandler extractHandler, IItemHandler insertHandler) {
        for (int i = 0; i < extractHandler.getSlots(); i++) {
            ItemStack extract = extractHandler.extractItem(i, extractHandler.getSlotLimit(i), true);
            if (!extract.isEmpty()) {
                ItemStack insert = ItemHandlerHelper.insertItem(insertHandler, extract, true);
                if (!insert.isEmpty()) {
                    ItemStack extractReal = extractHandler.extractItem(i, insert.getCount(), true);
                    int count = extractReal.getCount();
                    if (count == insert.getCount()) {
                        extractReal = extractHandler.extractItem(i, count, false);
                        ItemHandlerHelper.insertItem(insertHandler, extractReal, false);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean transferOnce(IEnergyStorage extractHandler, IEnergyStorage receiveHandler) {
        int extract = extractHandler.extractEnergy(Integer.MAX_VALUE, true);
        if (extract != 0) {
            int receive = receiveHandler.receiveEnergy(extract, true);
            if (receive != 0) {
                extract = receiveHandler.extractEnergy(receive, true);
                if (extract == receive) {
                    extractHandler.extractEnergy(extract, false);
                    receiveHandler.receiveEnergy(extract, false);
                    return true;
                }
            }
        }
        return false;
    }
}

package com.elementtimes.elementcore.api.template.block;

import com.elementtimes.elementcore.api.template.block.interfaces.IDismantleBlock;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.IGuiProvider;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 需要带有 TileEntity 的方块时继承此类
 *
 * @author KSGFK create in 2019/2/17
 */
@SuppressWarnings("WeakerAccess")
public class BlockTileBase<T extends TileEntity> extends BlockContainer implements IDismantleBlock {

    private Class<T> mEntityClass;
    private int mGui;
    private Object mMod;

    public BlockTileBase(Class<T> entityClass, int gui, Object mod) {
        super(Material.IRON);
        setHardness(15.0F);
        setResistance(25.0F);
        mGui = gui;
        mMod = mod;
        mEntityClass = entityClass;
    }

    public BlockTileBase(Class<T> entityClass, Object mod) {
        this(entityClass, 0, mod);
    }

    public BlockTileBase(Class<T> entityClass) {
        this(entityClass, 0, null);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@SuppressWarnings("NullableProblems") World worldIn, int meta) {
        try {
            if (mEntityClass != null) {
                for (Constructor<?> constructor : mEntityClass.getDeclaredConstructors()) {
                    constructor.setAccessible(true);
                    if (constructor.getParameterCount() == 0) {
                        return (TileEntity) constructor.newInstance();
                    }
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public EnumBlockRenderType getRenderType(IBlockState state) {//渲染类型设为普通方块
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state,
                                    EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            ItemStack stack = playerIn.getHeldItem(hand);
            TileEntity te = worldIn.getTileEntity(pos);
            if (te != null) {
                IFluidHandlerItem fluidHandlerStack = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                IFluidHandler fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);
                if (transfer(fluidHandler, fluidHandlerStack, playerIn, hand)) {
                    return true;
                }
                IItemHandler itemHandlerStack = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                IItemHandler itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
                if (transfer(itemHandler, itemHandlerStack)) {
                    return true;
                }
                IEnergyStorage energyHandlerStack = stack.getCapability(CapabilityEnergy.ENERGY, null);
                IEnergyStorage energyHandler = te.getCapability(CapabilityEnergy.ENERGY, facing);
                if (transfer(energyHandler, energyHandlerStack)) {
                    return true;
                }
            }
            if (mMod != null) {
                TileEntity e = worldIn.getTileEntity(pos);
                if (e instanceof IGuiProvider) {
                    playerIn.openGui(mMod, ((IGuiProvider) e).getGuiId(), worldIn, pos.getX(), pos.getY(), pos.getZ());
                } else {
                    playerIn.openGui(mMod, mGui, worldIn, pos.getX(), pos.getY(), pos.getZ());
                }
                return true;
            }
            return false;
        }
        return true;
    }

    private boolean transfer(IFluidHandler teHandler, IFluidHandlerItem itemHandler, EntityPlayer player, EnumHand hand) {
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
        FluidStack drain = drainHandler.drain(Integer.MAX_VALUE, false);
        if (drain != null && drain.amount > 0) {
            int fill = fillHandler.fill(drain, false);
            drain = drain.copy();
            drain.amount = fill;
            FluidStack drainReal = drainHandler.drain(drain, false);
            if (drainReal != null && drainReal.amount == fill) {
                drainHandler.drain(fill, true);
                fillHandler.fill(drainReal, true);
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

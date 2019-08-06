package com.elementtimes.elementcore.common.item;

import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.util.FluidUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 可燃的瓶子？
 * @author luqin2007
 */
public class Bottle extends Item {

    public Bottle() {
        setContainerItem(Items.GLASS_BOTTLE);
        setMaxStackSize(1);
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            FluidRegistry.getRegisteredFluids().values().stream()
                    .map(Bottle::createByFluid)
                    .forEach(items::add);
        }
    }

    @Override
    @Nonnull
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            ItemStack bottle = player.getHeldItem(hand);
            FluidStack fluidStack = FluidUtil.getFluid(bottle);
            Capability capability = CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
            if (te != null && te.hasCapability(capability, facing.getOpposite())) {
                @SuppressWarnings("unchecked")
                IFluidHandler handler = (IFluidHandler) te.getCapability(capability, facing.getOpposite());
                FluidStack resource = new FluidStack(fluidStack, 1000);
                assert handler != null;
                FluidStack drain = handler.drain(resource, false);
                if (drain != null && drain.containsFluid(resource)) {
                    handler.drain(resource, true);
                    player.setHeldItem(hand, new ItemStack(Items.GLASS_BOTTLE));
                }
            }
        }
        return EnumActionResult.PASS;
    }

    @Override
    @Nonnull
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            Fluid fluid = FluidUtil.getFluidNotNull(stack).getFluid();
            if (fluid != null) {
                String name = fluid.getLocalizedName(new FluidStack(fluid, 1000));
                if (name == null) {
                    name = "???";
                }
                return net.minecraft.client.resources.I18n.format(stack.getUnlocalizedName() + ".name", name);
            }
        }
        return "???";
    }

    @SuppressWarnings("WeakerAccess")
    public static ItemStack createByFluid(Fluid fluid) {
        return createByFluid(new FluidStack(fluid, Fluid.BUCKET_VOLUME));
    }

    @SuppressWarnings("WeakerAccess")
    public static ItemStack createByFluid(FluidStack fluid) {
        Item bottle = ElementCore.Items.bottle;
        ItemStack itemStack = new ItemStack(bottle);
        FluidHandlerItemStackSimple capability = (FluidHandlerItemStackSimple) itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        assert capability != null;
        capability.fill(fluid, true);
        return itemStack;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new FluidHandlerItemStackSimple.SwapEmpty(stack, new ItemStack(Items.GLASS_BOTTLE),  Fluid.BUCKET_VOLUME);
    }
}

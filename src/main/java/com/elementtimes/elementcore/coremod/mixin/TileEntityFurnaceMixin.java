package com.elementtimes.elementcore.coremod.mixin;

import com.elementtimes.elementcore.api.common.ECModContainer;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.ToIntFunction;

/**
 * 注入 {@link net.minecraft.tileentity.TileEntityFurnace}
 */
@Mixin(TileEntityFurnace.class)
public abstract class TileEntityFurnaceMixin extends TileEntityLockable {

    /**
     * 由于网易 forge 找不到 FurnaceFuelBurnTimeEvent 类，或由于某些原因该类被重写到错误的包中，所以重写下 getItemBurnTime 方法
     * @author luqin2007
     */
    @Inject(method = "getItemBurnTime", at = @At("HEAD"), cancellable = true)
    private static void inject_getItemBurnTime(ItemStack stack, CallbackInfoReturnable<Integer> returnable) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        Object o;
        Item item = stack.getItem();
        if (item instanceof ItemBlock) {
            o = Block.getBlockFromItem(item);
        } else if (item == Items.LAVA_BUCKET || item == Items.MILK_BUCKET || item == Items.WATER_BUCKET || item == ForgeModContainer.getInstance().universalBucket) {
            FluidStack fluidStack = FluidUtil.getFluidContained(stack);
            if (fluidStack != null) {
                o = fluidStack.getFluid();
            } else {
                o = item;
            }
        } else {
            o = item;
        }
        for (ECModContainer container : ECModContainer.MODS.values()) {
            ToIntFunction<ItemStack> function = container.elements.burnTimes.get(o);
            if (function != null) {
                int time = function.applyAsInt(stack);
                if (time >= 0) {
                    returnable.setReturnValue(time);
                }
                break;
            }
        }
    }
}

package com.elementtimes.elementcore.api.common.event;

import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 用于游戏过程中的的事件
 * @author luqin2007
 */
public class RuntimeEvent {

    private ECModElements mElements;

    public RuntimeEvent(ECModElements elements) {
        mElements = elements;
    }

    @SubscribeEvent
    public void onBurningTime(FurnaceFuelBurnTimeEvent event) {
        ECUtils.common.runWithModActive(mElements.container.mod, () -> {
            ItemStack itemStack = event.getItemStack();
            String name = null;
            if (itemStack.getItem() == ElementCore.Items.bottle) {
                name = ECUtils.fluid.getFluid(itemStack).getFluid().getName();
            } else if (FluidRegistry.isUniversalBucketEnabled() && itemStack.getItem() == ForgeModContainer.getInstance().universalBucket) {
                Optional<IFluidTankProperties> fluidBucket = Arrays.stream(Objects.requireNonNull(FluidUtil.getFluidHandler(itemStack)).getTankProperties()).findFirst();
                if (fluidBucket.isPresent()) {
                    Fluid fluid = Objects.requireNonNull(fluidBucket.get().getContents()).getFluid();
                    if (fluid != null) {
                        name = fluid.getName();
                    }
                }
            }
            int time = mElements.fluidBurningTimes.getOrDefault(name, -1);
            if (time > 0) {
                event.setBurnTime(time);
            }
        }, event);
    }
}

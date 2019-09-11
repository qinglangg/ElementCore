package com.elementtimes.elementcore.api.template.tileentity;

import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

/**
 * 对一个输入 一个输出的机器的抽象
 * @author KSGFK create in 2019/5/12
 */
public abstract class BaseOneToOne extends BaseTileEntity {

    public BaseOneToOne(int maxEnergy) {
        super(maxEnergy, 1, 1);
    }

    @Override
    public SideHandlerType getEnergyType(EnumFacing facing) {
        return SideHandlerType.INPUT;
    }

    @Nonnull
    @Override
    public Slot[] getSlots() {
        return new Slot[] {
                new SlotItemHandler(getItemHandler(SideHandlerType.INPUT), 0, 56, 30),
                new SlotItemHandler(getItemHandler(SideHandlerType.OUTPUT), 0, 110, 30)
        };
    }
}

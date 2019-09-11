package com.elementtimes.elementcore.api.template.tileentity.interfaces;

import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 为 GUI 提供交互元素
 * @author luqin2007
 */
@SuppressWarnings("unused")
public interface IGuiProvider {

    Slot[] ITEM_NULL = new Slot[0];
    FluidSlotInfo[] FLUID_NULL = new FluidSlotInfo[0];

    /**
     * 获取所有打开该机器gui的玩家，用于向他们同步信息
     * @return 所有打开该机器 gui 的玩家
     */
    List<EntityPlayerMP> getOpenedPlayers();

    /**
     * 获取 GUI 类型， 根据此打开对应的 GUI
     * @return GUI 类型
     */
    int getGuiId();

    /**
     * 创建物品槽位
     * @return 物品槽位
     */
    @Nonnull
    default Slot[] getSlots() { return ITEM_NULL; }

    /**
     * 创建流体槽位
     * type -> slot -> [x, y, w, h]
     * @return 流体
     */
    @Nonnull
    default FluidSlotInfo[] getFluids() {
        return FLUID_NULL;
    }

    /**
     * 获取当前进度
     * @return 进度
     */
    float getProcess();

    class FluidSlotInfo {
        final public int slotId, x, y, w, h;
        final public SideHandlerType type;
        final public boolean isHorizontal;

        @SuppressWarnings("WeakerAccess")
        public FluidSlotInfo(int slotId, SideHandlerType type, int x, int y, int w, int h, boolean isHorizontal) {
            this.slotId = slotId;
            this.type = type;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.isHorizontal = isHorizontal;
        }

        public static FluidSlotInfo create(int slotId, SideHandlerType type, int x, int y) {
            return new FluidSlotInfo(slotId, type, x, y, 16, 46, false);
        }

        public static FluidSlotInfo createHorizontal(int slotId, SideHandlerType type, int x, int y) {
            return new FluidSlotInfo(slotId, type, x, y, 16, 46, true);
        }

        public static FluidSlotInfo createInput(int slotId, int x, int y) {
            return new FluidSlotInfo(slotId, SideHandlerType.INPUT, x, y, 16, 46, false);
        }

        public static FluidSlotInfo createInputHorizontal(int slotId, int x, int y) {
            return new FluidSlotInfo(slotId, SideHandlerType.INPUT, x, y, 16, 46, true);
        }

        public static FluidSlotInfo createOutput(int slotId, int x, int y) {
            return new FluidSlotInfo(slotId, SideHandlerType.OUTPUT, x, y, 16, 46, false);
        }

        public static FluidSlotInfo createOutputHorizontal(int slotId, int x, int y) {
            return new FluidSlotInfo(slotId, SideHandlerType.OUTPUT, x, y, 16, 46, true);
        }
    }
}

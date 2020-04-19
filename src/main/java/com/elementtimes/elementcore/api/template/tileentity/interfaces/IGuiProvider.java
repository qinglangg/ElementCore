package com.elementtimes.elementcore.api.template.tileentity.interfaces;

import com.elementtimes.elementcore.api.template.gui.server.BaseContainer;
import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

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
     * 获取 GUI 背景
     * @return 背景资源
     */
    ResourceLocation getBackground();

    /**
     * 获取 GUI 的一系列尺寸
     * @return 尺寸
     */
    GuiSize getSize();

    /**
     * 获取显示文字
     * @return 文字
     */
    String getTitle();

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

    /**
     * GUI 关闭时调用
     */
    default void onGuiClosed(EntityPlayer player) {}

    /**
     * 在 {@link net.minecraft.client.gui.inventory.GuiContainer#drawGuiContainerBackgroundLayer(float, int, int)} 时触发
     */
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("JavadocReference")
    default void onBackgroundRender(com.elementtimes.elementcore.api.template.gui.client.BaseGuiContainer gui, BaseContainer container, float partialTicks, int mouseX, int mouseY) { }

    /**
     * 在 {@link net.minecraft.client.gui.inventory.GuiContainer#drawGuiContainerForegroundLayer(int, int)} 时触发
     */
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("JavadocReference")
    default void onForegroundRender(com.elementtimes.elementcore.api.template.gui.client.BaseGuiContainer gui, BaseContainer container, int mouseX, int mouseY) { }

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
            return new FluidSlotInfo(slotId, type, x, y, 46, 16, true);
        }

        public static FluidSlotInfo createInput(int slotId, int x, int y) {
            return new FluidSlotInfo(slotId, SideHandlerType.INPUT, x, y, 16, 46, false);
        }

        public static FluidSlotInfo createInputHorizontal(int slotId, int x, int y) {
            return new FluidSlotInfo(slotId, SideHandlerType.INPUT, x, y, 46, 16, true);
        }

        public static FluidSlotInfo createOutput(int slotId, int x, int y) {
            return new FluidSlotInfo(slotId, SideHandlerType.OUTPUT, x, y, 16, 46, false);
        }

        public static FluidSlotInfo createOutputHorizontal(int slotId, int x, int y) {
            return new FluidSlotInfo(slotId, SideHandlerType.OUTPUT, x, y, 46, 16, true);
        }
    }

    class GuiSize {
        public int width, height;
        public boolean hasTitle = true;
        public int titleOffsetY;
        public boolean hasPlayerInventory = true;
        public int playerInventoryOffsetX, playerInventoryOffsetY;
        public boolean hasInteractDistanceLimit = true;
        public int maxInteractDistance = 64;
        public Size[] process = new Size[0];
        public Size[] energy = new Size[0];

        public GuiSize copy() {
            GuiSize size = new GuiSize();
            size.width = width;
            size.height = height;
            size.hasTitle = hasTitle;
            size.titleOffsetY = titleOffsetY;
            size.hasPlayerInventory = hasPlayerInventory;
            size.playerInventoryOffsetX = playerInventoryOffsetX;
            size.playerInventoryOffsetY = playerInventoryOffsetY;
            size.hasInteractDistanceLimit = hasInteractDistanceLimit;
            size.maxInteractDistance = maxInteractDistance;
            size.process = new Size[size.process.length];
            for (int i = 0; i < process.length; i++) {
                size.process[i] = process[i].copy();
            }
            size.energy = new Size[size.energy.length];
            for (int i = 0; i < energy.length; i++) {
                size.energy[i] = energy[i].copy();
            }
            return size;
        }

        public GuiSize withNoInventory() {
            hasPlayerInventory = false;
            return this;
        }

        public GuiSize withSize(int width, int height, int offsetX, int offsetY) {
            this.width = width;
            this.height = height;
            this.hasPlayerInventory = true;
            this.playerInventoryOffsetX = offsetX;
            this.playerInventoryOffsetY = offsetY;
            return this;
        }

        public GuiSize withNoTitle() {
            hasTitle = false;
            return this;
        }

        public GuiSize withTitleY(int offsetY) {
            hasTitle = true;
            titleOffsetY = offsetY;
            return this;
        }

        public GuiSize withNoInteractLimit() {
            hasInteractDistanceLimit = false;
            return this;
        }

        public GuiSize withInteractDistance(int distance) {
            hasInteractDistanceLimit = true;
            maxInteractDistance = distance;
            return this;
        }

        public GuiSize withEnergy(int x, int y, int u, int v, int w, int h) {
            Size s = new Size(x, y, u, v, w, h);
            energy = ArrayUtils.add(energy, s);
            return this;
        }

        public GuiSize withEnergy(int x, int y) {
            return withEnergy(x, y, 24, height, 90, 4);
        }

        public GuiSize withProcess(int x, int y, int u, int v, int w, int h) {
            Size s = new Size(x, y, u, v, w, h);
            process = ArrayUtils.add(process, s);
            return this;
        }

        public GuiSize withProcess(int x, int y) {
            return withProcess(x, y, 0, height, 24, 17);
        }
    }

    class Size {
        public int x, y, u, v, w, h;
        public Size(int x, int y, int u, int v, int w, int h) {
            this.x = x;
            this.y = y;
            this.u = u;
            this.v = v;
            this.w = w;
            this.h = h;
        }

        public Size copy() {
            return new Size(x, y, u ,v ,w, h);
        }
    }

    GuiSize GUI_SIZE_176_156_74 = new GuiSize().withSize(176, 156, 8, 74);
    GuiSize GUI_SIZE_176_166_84 = new GuiSize().withSize(176, 166, 8, 84);
    GuiSize GUI_SIZE_176_204_122 = new GuiSize().withSize(176, 204, 8, 122);
    GuiSize GUI_SIZE_176_201_119 = new GuiSize().withSize(176, 201, 8, 119);
    GuiSize GUI_SIZE_176_179_97 = new GuiSize().withSize(176, 179, 8, 97);
}
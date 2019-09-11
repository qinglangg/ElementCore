package com.elementtimes.elementcore.api.template.tileentity.interfaces;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author KSGFK create in 2019/6/12
 */
public interface ITESRSupport extends INBTSerializable<NBTTagCompound> {

    RenderObject EMPTY = RenderObject.create(Items.AIR, 0, 0, 0);
    String BIND_NBT_TESR_TE = "_nbt_tesr_te_";
    String BIND_NBT_TESR_TE_ITEMS = "_nbt_tesr_te_items_";
    String BIND_NBT_TESR_TE_PROPERTIES = "_nbt_tesr_te_properties_";

    NonNullList<RenderObject> getRenderItems();

    @Nullable
    default NBTTagCompound getRenderProperties() {
        return null;
    }

    void setRenderProperties(NBTTagCompound properties);

    void markRenderClient();

    default void receiveRenderMessage(NBTTagCompound nbt) {
        NBTTagCompound nbtTe = nbt.getCompoundTag(BIND_NBT_TESR_TE);
        NBTTagList list = (NBTTagList) nbtTe.getTag(BIND_NBT_TESR_TE_ITEMS);
        NonNullList<RenderObject> renderItems = getRenderItems();
        renderItems.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            renderItems.add(i, RenderObject.create(list.getCompoundTagAt(i)));
        }
        if (nbtTe.hasKey(BIND_NBT_TESR_TE_PROPERTIES)) {
            setRenderProperties(nbtTe.getCompoundTag(BIND_NBT_TESR_TE_PROPERTIES));
        }
    }

    default NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagCompound nbtTESR = new NBTTagCompound();
        NBTTagList items = new NBTTagList();
        for (RenderObject item : getRenderItems()) {
            items.appendTag(item.serializeNBT());
        }
        nbtTESR.setTag(BIND_NBT_TESR_TE_ITEMS, items);
        NBTTagCompound properties = getRenderProperties();
        if (properties != null) {
            nbtTESR.setTag(BIND_NBT_TESR_TE_PROPERTIES, properties);
        }
        nbt.setTag(BIND_NBT_TESR_TE, nbtTESR);
        return nbt;
    }

    default void readFromNBT(NBTTagCompound nbt) {
        receiveRenderMessage(nbt);
    }

    default int registerRender(RenderObject renderObject) {
        NonNullList<RenderObject> renderItems = getRenderItems();
        for (int i = 0; i < renderItems.size(); i++) {
            if (EMPTY.equals(renderItems.get(i))) {
                renderItems.set(i, renderObject);
                return i;
            }
        }
        int index = renderItems.size();
        renderItems.add(renderObject);
        return index;
    }

    default int registerRender(RenderObject renderObject, int i) {
        NonNullList<RenderObject> renderItems = getRenderItems();
        while (renderItems.size() < i) {
            renderItems.add(EMPTY);
        }
        renderItems.add(renderObject);
        return i;
    }

    default void setRender(int index, boolean isRender) {
        getRenderItems().get(index).setRender(isRender);
        markRenderClient();
    }

    default void removeRender(int index) {
        NonNullList<RenderObject> renderItems = getRenderItems();
        setRender(index, false);
        if (renderItems.size() == index + 1) {
            renderItems.remove(index);
        } else {
            renderItems.set(index, EMPTY);
        }
    }

    default boolean isRender(int i) {
        return getRenderItems().get(i).isRender();
    }

    @Override
    default NBTTagCompound serializeNBT() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    default void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey(ITESRSupport.BIND_NBT_TESR_TE)) {
            readFromNBT(nbt);
        }
    }

    class RenderObject implements INBTSerializable<NBTTagCompound> {

        public ItemStack obj;
        public Vec3d vector;
        private boolean isRender = false;
        private boolean isBlock;

        private static final String BIND_NBT_TESR_RENDER = "_nbt_tesr_render_";
        private static final String BIND_NBT_TESR_RENDER_OBJ = "_nbt_tesr_render_obj_";
        private static final String BIND_NBT_TESR_RENDER_EMPTY = "_nbt_tesr_render_empty_";
        private static final String BIND_NBT_TESR_RENDER_VECTOR_X = "_nbt_tesr_render_vector_x_";
        private static final String BIND_NBT_TESR_RENDER_VECTOR_Y = "_nbt_tesr_render_vector_y_";
        private static final String BIND_NBT_TESR_RENDER_VECTOR_Z = "_nbt_tesr_render_vector_z_";
        private static final String BIND_NBT_TESR_RENDER_IS_RENDER = "_nbt_tesr_render_is_render_";
        private static final String BIND_NBT_TESR_RENDER_IS_BLOCK = "_nbt_tesr_render_is_block_";

        public static RenderObject create(Item item, double x, double y, double z) {
            return new RenderObject(new ItemStack(item), new Vec3d(x, y, z)).setRender(false);
        }

        public static RenderObject create(ItemStack item, double x, double y, double z) {
            return new RenderObject(item, new Vec3d(x, y, z)).setRender(false);
        }

        public static RenderObject create(NBTTagCompound nbt) {
            if (nbt.getCompoundTag(BIND_NBT_TESR_RENDER).getBoolean(BIND_NBT_TESR_RENDER_EMPTY)) {
                return EMPTY;
            }
            RenderObject copy = EMPTY.copy();
            copy.deserializeNBT(nbt);
            return copy;
        }

        public static RenderObject create(Block block, double x, double y, double z) {
            return new RenderObject(new ItemStack(Item.getItemFromBlock(block)), new Vec3d(x, y, z)).setRender(false);
        }

        private RenderObject(ItemStack obj, Vec3d vector) {
            this.obj = obj;
            this.vector = vector;
            this.isBlock = false;
        }

        public RenderObject setItem(ItemStack obj) {
            this.obj = obj;
            return this;
        }

        public RenderObject setRender(boolean isRender) {
            this.isRender = isRender;
            return this;
        }

        public boolean isRender() {
            return isRender;
        }

        public boolean isBlock() {
            return isBlock;
        }

        public RenderObject copy() {
            return new RenderObject(obj.copy(), new Vec3d(vector.x, vector.y, vector.z));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof RenderObject)) {
                return false;
            }
            return ((RenderObject) obj).obj.equals(this.obj) && ((RenderObject) obj).vector.equals(this.vector);
        }

        @Override
        public int hashCode() {
            return Objects.hash(obj, vector);
        }

        public NBTTagCompound writeNBT(NBTTagCompound nbt) {
            NBTTagCompound nbtRender = new NBTTagCompound();
            if (this.equals(EMPTY)) {
                nbtRender.setBoolean(BIND_NBT_TESR_RENDER_EMPTY, true);
            } else {
                nbtRender.setBoolean(BIND_NBT_TESR_RENDER_EMPTY, false);
                nbtRender.setTag(BIND_NBT_TESR_RENDER_OBJ, obj.writeToNBT(new NBTTagCompound()));
                nbtRender.setDouble(BIND_NBT_TESR_RENDER_VECTOR_X, vector.x);
                nbtRender.setDouble(BIND_NBT_TESR_RENDER_VECTOR_Y, vector.y);
                nbtRender.setDouble(BIND_NBT_TESR_RENDER_VECTOR_Z, vector.z);
                nbtRender.setBoolean(BIND_NBT_TESR_RENDER_IS_RENDER, isRender);
                nbtRender.setBoolean(BIND_NBT_TESR_RENDER_IS_BLOCK, isBlock);
            }
            nbt.setTag(BIND_NBT_TESR_RENDER, nbtRender);
            return nbt;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return writeNBT(new NBTTagCompound());
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            NBTTagCompound nbtRender = nbt.getCompoundTag(BIND_NBT_TESR_RENDER);
            obj = new ItemStack(nbtRender.getCompoundTag(BIND_NBT_TESR_RENDER_OBJ));
            vector = new Vec3d(
                    nbtRender.getDouble(BIND_NBT_TESR_RENDER_VECTOR_X),
                    nbtRender.getDouble(BIND_NBT_TESR_RENDER_VECTOR_Y),
                    nbtRender.getDouble(BIND_NBT_TESR_RENDER_VECTOR_Z)
            );
            isBlock = nbtRender.getBoolean(BIND_NBT_TESR_RENDER_IS_BLOCK);
            isRender = nbtRender.getBoolean(BIND_NBT_TESR_RENDER_IS_RENDER);
        }
    }
}

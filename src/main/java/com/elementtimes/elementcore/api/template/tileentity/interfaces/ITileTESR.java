package com.elementtimes.elementcore.api.template.tileentity.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 添加 TESR 渲染支持
 * 实现该接口后，会自动注册 TESR
 * @see com.elementtimes.elementcore.api.template.tileentity.BaseTESR
 * @author KSGFK create in 2019/6/12
 */
public interface ITileTESR extends INBTSerializable<NBTTagCompound> {

    ArrayList<BlockPos> renderDirty = new ArrayList<>();

    RenderObject EMPTY = new RenderObject(ItemStack.EMPTY);
    String BIND_NBT_TESR_TE = "_nbt_tesr_te_";
    String BIND_NBT_TESR_TE_ITEMS = "_nbt_tesr_te_items_";
    String BIND_NBT_TESR_TE_ITEM_KEY = "_nbt_tesr_te_item_k_";
    String BIND_NBT_TESR_TE_ITEM_OBJ = "_nbt_tesr_te_item_o_";
    String BIND_NBT_TESR_TE_PROPERTIES = "_nbt_tesr_te_properties_";

    HashMap<String, RenderObject> getRenderItems();

    @Nonnull
    NBTTagCompound getRenderProperties();

    void setRenderProperties(@Nonnull NBTTagCompound properties);

    default void markRenderClient(BlockPos pos) {
        if (!renderDirty.contains(pos)) {
            renderDirty.add(pos);
        }
    }

    default void receiveRenderMessage(NBTTagCompound nbt) {
        NBTTagCompound nbtTe = nbt.getCompoundTag(BIND_NBT_TESR_TE);
        NBTTagList list = (NBTTagList) nbtTe.getTag(BIND_NBT_TESR_TE_ITEMS);
        HashMap<String, RenderObject> renderItems = getRenderItems();
        renderItems.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            renderItems.put(tag.getString(BIND_NBT_TESR_TE_ITEM_KEY), RenderObject.create(tag.getCompoundTag(BIND_NBT_TESR_TE_ITEM_OBJ)));
        }
        if (nbtTe.hasKey(BIND_NBT_TESR_TE_PROPERTIES)) {
            setRenderProperties(nbtTe.getCompoundTag(BIND_NBT_TESR_TE_PROPERTIES));
        }
    }

    default NBTTagCompound writeRenderNbt(NBTTagCompound nbt) {
        NBTTagCompound nbtTESR = new NBTTagCompound();
        NBTTagList items = new NBTTagList();
        for (Map.Entry<String, RenderObject> renderObj : getRenderItems().entrySet()) {
            NBTTagCompound nbtRender = new NBTTagCompound();
            nbtRender.setString(BIND_NBT_TESR_TE_ITEM_KEY, renderObj.getKey());
            nbtRender.setTag(BIND_NBT_TESR_TE_ITEM_OBJ, renderObj.getValue().serializeNBT());
            items.appendTag(nbtRender);
        }
        nbtTESR.setTag(BIND_NBT_TESR_TE_ITEMS, items);
        NBTTagCompound properties = getRenderProperties();
        nbtTESR.setTag(BIND_NBT_TESR_TE_PROPERTIES, properties);
        nbt.setTag(BIND_NBT_TESR_TE, nbtTESR);
        return nbt;
    }

    default void readFromNBT(NBTTagCompound nbt) {
        receiveRenderMessage(nbt);
    }

    default void registerRender(String key, RenderObject renderObject) {
        getRenderItems().put(key, renderObject);
    }

    default void setRender(String key, boolean isRender, BlockPos pos) {
        getRenderItems().get(key).setRender(isRender);
        markRenderClient(pos);
    }

    default boolean isRenderRegistered(String key) {
        return getRenderItems().containsKey(key);
    }

    default boolean isRender(String key) {
        RenderObject renderObject = getRenderItems().get(key);
        return renderObject != null && renderObject.isRender();
    }

    @Override
    default NBTTagCompound serializeNBT() {
        return writeRenderNbt(new NBTTagCompound());
    }

    @Override
    default void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey(ITileTESR.BIND_NBT_TESR_TE)) {
            readFromNBT(nbt);
        }
    }

    class RenderObject implements INBTSerializable<NBTTagCompound> {

        public ItemStack obj;
        public Vec3d[] translates = new Vec3d[0];
        public Vec3d[] scales = new Vec3d[0];
        public Vec3d[] rotates = new Vec3d[0];
        public float[] rotateAngles = new float[0];
        private boolean isRender = false;
        private boolean isBlock;

        private static final String BIND_NBT_TESR_RENDER = "_nbt_tesr_render_";
        private static final String BIND_NBT_TESR_RENDER_OBJ = "_nbt_tesr_render_obj_";
        private static final String BIND_NBT_TESR_RENDER_EMPTY = "_nbt_tesr_render_empty_";
        private static final String BIND_NBT_TESR_RENDER_TRANSLATES = "_nbt_tesr_render_t_";
        private static final String BIND_NBT_TESR_RENDER_SCALES = "_nbt_tesr_render_s_";
        private static final String BIND_NBT_TESR_RENDER_ROTATES = "_nbt_tesr_render_r_";
        private static final String BIND_NBT_TESR_RENDER_ROTATE_ANGLES = "_nbt_tesr_render_r_a_";
        private static final String BIND_NBT_TESR_RENDER_IS_RENDER = "_nbt_tesr_render_is_render_";
        private static final String BIND_NBT_TESR_RENDER_IS_BLOCK = "_nbt_tesr_render_is_block_";
        private static final String BIND_NBT_TESR_RENDER_VECTOR_X = "_nbt_tesr_render_vector_x_";
        private static final String BIND_NBT_TESR_RENDER_VECTOR_Y = "_nbt_tesr_render_vector_y_";
        private static final String BIND_NBT_TESR_RENDER_VECTOR_Z = "_nbt_tesr_render_vector_z_";

        public static RenderObject create(NBTTagCompound nbt) {
            if (nbt.getCompoundTag(BIND_NBT_TESR_RENDER).getBoolean(BIND_NBT_TESR_RENDER_EMPTY)) {
                return EMPTY;
            }
            RenderObject copy = EMPTY.copy();
            copy.deserializeNBT(nbt);
            return copy;
        }

        public RenderObject(ItemStack obj) {
            this.obj = obj;
            this.isBlock = false;
        }

        public RenderObject translate(double x, double y, double z) {
            translates = ArrayUtils.add(translates, new Vec3d(x, y, z));
            return this;
        }

        public RenderObject translate(Vec3d vector) {
            translates = ArrayUtils.add(translates, vector);
            return this;
        }

        public RenderObject scale(double x, double y, double z) {
            scales = ArrayUtils.add(scales, new Vec3d(x, y, z));
            return this;
        }

        public RenderObject scale(Vec3d vector) {
            scales = ArrayUtils.add(scales, vector);
            return this;
        }

        public RenderObject rotate(double x, double y, double z, float angle) {
            rotates = ArrayUtils.add(rotates, new Vec3d(x, y, z));
            rotateAngles = ArrayUtils.add(rotateAngles, angle);
            return this;
        }

        public RenderObject rotate(Vec3d vector, float angle) {
            rotates = ArrayUtils.add(rotates, vector);
            rotateAngles = ArrayUtils.add(rotateAngles, angle);
            return this;
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
            RenderObject renderObject = new RenderObject(obj.copy());
            renderObject.translates = new Vec3d[translates.length];
            for (int i = 0; i < translates.length; i++) {
                Vec3d v = translates[i];
                renderObject.translates[i] = new Vec3d(v.x, v.y, v.z);
            }
            renderObject.scales = new Vec3d[scales.length];
            for (int i = 0; i < scales.length; i++) {
                Vec3d v = scales[i];
                renderObject.scales[i] = new Vec3d(v.x, v.y, v.z);
            }
            renderObject.rotates = new Vec3d[rotates.length];
            for (int i = 0; i < rotates.length; i++) {
                Vec3d v = rotates[i];
                renderObject.rotates[i] = new Vec3d(v.x, v.y, v.z);
            }
            renderObject.rotateAngles = new float[rotateAngles.length];
            for (int i = 0; i < rotateAngles.length; i++) {
                renderObject.rotateAngles[i] = rotateAngles[i];
            }
            return renderObject;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof RenderObject)) {
                return false;
            }
            return ((RenderObject) obj).obj.equals(this.obj)
                    && ((RenderObject) obj).translates.equals(this.translates)
                    && ((RenderObject) obj).scales.equals(this.scales)
                    && ((RenderObject) obj).rotateAngles.equals(this.rotateAngles)
                    && ((RenderObject) obj).rotates.equals(this.rotates);
        }

        @Override
        public int hashCode() {
            return Objects.hash(obj, translates, scales, rotateAngles, rotates);
        }

        public NBTTagCompound writeNBT(NBTTagCompound nbt) {
            NBTTagCompound nbtRender = new NBTTagCompound();
            if (this.equals(EMPTY)) {
                nbtRender.setBoolean(BIND_NBT_TESR_RENDER_EMPTY, true);
            } else {
                nbtRender.setBoolean(BIND_NBT_TESR_RENDER_EMPTY, false);
                nbtRender.setTag(BIND_NBT_TESR_RENDER_OBJ, obj.writeToNBT(new NBTTagCompound()));
                nbtRender.setTag(BIND_NBT_TESR_RENDER_TRANSLATES, vec2nbt(translates));
                nbtRender.setTag(BIND_NBT_TESR_RENDER_ROTATES, vec2nbt(rotates));
                nbtRender.setTag(BIND_NBT_TESR_RENDER_SCALES, vec2nbt(scales));
                nbtRender.setTag(BIND_NBT_TESR_RENDER_ROTATE_ANGLES, float2nbt(rotateAngles));
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
            translates = nbt2vec((NBTTagList) nbtRender.getTag(BIND_NBT_TESR_RENDER_TRANSLATES));
            scales = nbt2vec((NBTTagList) nbtRender.getTag(BIND_NBT_TESR_RENDER_SCALES));
            rotates = nbt2vec((NBTTagList) nbtRender.getTag(BIND_NBT_TESR_RENDER_ROTATES));
            rotateAngles = nbt2float((NBTTagList) nbtRender.getTag(BIND_NBT_TESR_RENDER_ROTATE_ANGLES));
            isBlock = nbtRender.getBoolean(BIND_NBT_TESR_RENDER_IS_BLOCK);
            isRender = nbtRender.getBoolean(BIND_NBT_TESR_RENDER_IS_RENDER);
        }

        private NBTTagList vec2nbt(Vec3d[] vectors) {
            NBTTagList list = new NBTTagList();
            for (Vec3d vector : vectors) {
                NBTTagCompound nbtVector = new NBTTagCompound();
                nbtVector.setDouble(BIND_NBT_TESR_RENDER_VECTOR_X, vector.x);
                nbtVector.setDouble(BIND_NBT_TESR_RENDER_VECTOR_Y, vector.y);
                nbtVector.setDouble(BIND_NBT_TESR_RENDER_VECTOR_Z, vector.z);
                list.appendTag(nbtVector);
            }
            return list;
        }

        private NBTTagList float2nbt(float[] values) {
            NBTTagList list = new NBTTagList();
            for (float value : values) {
                list.appendTag(new NBTTagFloat(value));
            }
            return list;
        }

        private Vec3d[] nbt2vec(NBTTagList vectors) {
            int count = vectors.tagCount();
            Vec3d[] list = new Vec3d[count];
            for (int i = 0; i < count; i++) {
                NBTTagCompound nbtVector = vectors.getCompoundTagAt(i);
                double x = nbtVector.getDouble(BIND_NBT_TESR_RENDER_VECTOR_X);
                double y = nbtVector.getDouble(BIND_NBT_TESR_RENDER_VECTOR_Y);
                double z = nbtVector.getDouble(BIND_NBT_TESR_RENDER_VECTOR_Z);
                list[i] = new Vec3d(x, y, z);
            }
            return list;
        }

        private float[] nbt2float(NBTTagList values) {
            int count = values.tagCount();
            float[] fList = new float[count];
            for (int i = 0; i < count; i++) {
                fList[i] = values.getFloatAt(i);
            }
            return fList;
        }
    }
}

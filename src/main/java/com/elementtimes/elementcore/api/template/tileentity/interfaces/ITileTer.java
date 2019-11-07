package com.elementtimes.elementcore.api.template.tileentity.interfaces;

import com.elementtimes.elementcore.api.template.interfaces.INbtReadable;
import com.elementtimes.elementcore.api.template.tileentity.BaseTsr;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * 添加 TER 渲染支持
 * 实现该接口后，会自动注册 TER
 * @see BaseTsr
 * @author KSGFK create in 2019/6/12
 */
public interface ITileTer extends INbtReadable {

    List<BlockPos> RENDER_POS = new ArrayList<>();
    RenderObject EMPTY = new RenderObject(ItemStack.EMPTY);
    String BIND_NBT_TER_TE = "_nbt_ter_te_";
    String BIND_NBT_TER_TE_ITEMS = "_nbt_ter_te_items_";
    String BIND_NBT_TER_TE_ITEM_KEY = "_nbt_ter_te_item_k_";
    String BIND_NBT_TER_TE_ITEM_OBJ = "_nbt_ter_te_item_o_";
    String BIND_NBT_TER_TE_PROPERTIES = "_nbt_ter_te_properties_";

    HashMap<String, RenderObject> getRenderItems();

    @Nonnull
    CompoundNBT getRenderProperties();

    void setRenderProperties(@Nonnull CompoundNBT properties);

    default void markRenderClient(BlockPos pos) {
        if (!RENDER_POS.contains(pos)) {
            RENDER_POS.add(pos);
        }
    }

    default void receiveRenderMessage(CompoundNBT compound) {
        read(compound);
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
    default void read(@Nonnull CompoundNBT compound) {
        CompoundNBT nbtTer = compound.getCompound(BIND_NBT_TER_TE);
        ListNBT list = nbtTer.getList(BIND_NBT_TER_TE_ITEMS, Constants.NBT.TAG_COMPOUND);
        HashMap<String, RenderObject> renderItems = getRenderItems();
        renderItems.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT tag = list.getCompound(i);
            renderItems.put(tag.getString(BIND_NBT_TER_TE_ITEM_KEY), RenderObject.create(tag.getCompound(BIND_NBT_TER_TE_ITEM_OBJ)));
        }
        if (nbtTer.contains(BIND_NBT_TER_TE_PROPERTIES)) {
            setRenderProperties(nbtTer.getCompound(BIND_NBT_TER_TE_PROPERTIES));
        }
    };

    @Nonnull
    @Override
    default CompoundNBT write(@Nonnull CompoundNBT compound) {
        CompoundNBT nbtTer = new CompoundNBT();
        ListNBT items = new ListNBT();
        for (Map.Entry<String, RenderObject> renderObj : getRenderItems().entrySet()) {
            CompoundNBT nbtRender = new CompoundNBT();
            nbtRender.putString(BIND_NBT_TER_TE_ITEM_KEY, renderObj.getKey());
            nbtRender.put(BIND_NBT_TER_TE_ITEM_OBJ, renderObj.getValue().write(new CompoundNBT()));
            items.add(nbtRender);
        }
        nbtTer.put(BIND_NBT_TER_TE_ITEMS, items);
        CompoundNBT properties = getRenderProperties();
        nbtTer.put(BIND_NBT_TER_TE_PROPERTIES, properties);
        compound.put(BIND_NBT_TER_TE, nbtTer);
        return compound;
    };

    class RenderObject implements INbtReadable {

        public ItemStack obj;
        public Vec3d[] translates = new Vec3d[0];
        public Vec3d[] scales = new Vec3d[0];
        public Vec3d[] rotates = new Vec3d[0];
        public float[] rotateAngles = new float[0];
        private boolean isRender = false;
        private boolean isBlock;

        private static final String BIND_NBT_TER_RENDER = "_nbt_tesr_render_";
        private static final String BIND_NBT_TER_RENDER_OBJ = "_nbt_tesr_render_obj_";
        private static final String BIND_NBT_TER_RENDER_EMPTY = "_nbt_tesr_render_empty_";
        private static final String BIND_NBT_TER_RENDER_TRANSLATES = "_nbt_tesr_render_t_";
        private static final String BIND_NBT_TER_RENDER_SCALES = "_nbt_tesr_render_s_";
        private static final String BIND_NBT_TER_RENDER_ROTATES = "_nbt_tesr_render_r_";
        private static final String BIND_NBT_TER_RENDER_ROTATE_ANGLES = "_nbt_tesr_render_r_a_";
        private static final String BIND_NBT_TER_RENDER_IS_RENDER = "_nbt_tesr_render_is_render_";
        private static final String BIND_NBT_TER_RENDER_IS_BLOCK = "_nbt_tesr_render_is_block_";
        private static final String BIND_NBT_TER_RENDER_VECTOR_X = "_nbt_tesr_render_vector_x_";
        private static final String BIND_NBT_TER_RENDER_VECTOR_Y = "_nbt_tesr_render_vector_y_";
        private static final String BIND_NBT_TER_RENDER_VECTOR_Z = "_nbt_tesr_render_vector_z_";

        public static RenderObject create(CompoundNBT nbt) {
            if (nbt.getCompound(BIND_NBT_TER_RENDER).getBoolean(BIND_NBT_TER_RENDER_EMPTY)) {
                return EMPTY;
            }
            RenderObject copy = EMPTY.copy();
            copy.read(nbt);
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

        @Nonnull
        @Override
        public CompoundNBT write(@Nonnull CompoundNBT nbt) {
            CompoundNBT nbtRender = new CompoundNBT();
            if (this.equals(EMPTY)) {
                nbtRender.putBoolean(BIND_NBT_TER_RENDER_EMPTY, true);
            } else {
                nbtRender.putBoolean(BIND_NBT_TER_RENDER_EMPTY, false);
                nbtRender.put(BIND_NBT_TER_RENDER_OBJ, obj.write(new CompoundNBT()));
                nbtRender.put(BIND_NBT_TER_RENDER_TRANSLATES, vec2nbt(translates));
                nbtRender.put(BIND_NBT_TER_RENDER_ROTATES, vec2nbt(rotates));
                nbtRender.put(BIND_NBT_TER_RENDER_SCALES, vec2nbt(scales));
                nbtRender.put(BIND_NBT_TER_RENDER_ROTATE_ANGLES, float2nbt(rotateAngles));
                nbtRender.putBoolean(BIND_NBT_TER_RENDER_IS_RENDER, isRender);
                nbtRender.putBoolean(BIND_NBT_TER_RENDER_IS_BLOCK, isBlock);
            }
            nbt.put(BIND_NBT_TER_RENDER, nbtRender);
            return nbt;
        }

        @Override
        public void read(@Nonnull CompoundNBT nbt) {
            CompoundNBT nbtRender = nbt.getCompound(BIND_NBT_TER_RENDER);
            obj = ItemStack.read(nbtRender.getCompound(BIND_NBT_TER_RENDER_OBJ));
            translates = nbt2vec(nbtRender.getList(BIND_NBT_TER_RENDER_TRANSLATES, Constants.NBT.TAG_COMPOUND));
            scales = nbt2vec(nbtRender.getList(BIND_NBT_TER_RENDER_SCALES, Constants.NBT.TAG_COMPOUND));
            rotates = nbt2vec(nbtRender.getList(BIND_NBT_TER_RENDER_ROTATES, Constants.NBT.TAG_COMPOUND));
            rotateAngles = nbt2float(nbtRender.getList(BIND_NBT_TER_RENDER_ROTATE_ANGLES, Constants.NBT.TAG_FLOAT));
            isBlock = nbtRender.getBoolean(BIND_NBT_TER_RENDER_IS_BLOCK);
            isRender = nbtRender.getBoolean(BIND_NBT_TER_RENDER_IS_RENDER);
        }

        private ListNBT vec2nbt(Vec3d[] vectors) {
            ListNBT list = new ListNBT();
            for (Vec3d vector : vectors) {
                CompoundNBT nbtVector = new CompoundNBT();
                nbtVector.putDouble(BIND_NBT_TER_RENDER_VECTOR_X, vector.x);
                nbtVector.putDouble(BIND_NBT_TER_RENDER_VECTOR_Y, vector.y);
                nbtVector.putDouble(BIND_NBT_TER_RENDER_VECTOR_Z, vector.z);
                list.add(nbtVector);
            }
            return list;
        }

        private ListNBT float2nbt(float[] values) {
            ListNBT list = new ListNBT();
            for (float value : values) {
                list.add(new FloatNBT(value));
            }
            return list;
        }

        private Vec3d[] nbt2vec(ListNBT vectors) {
            int count = vectors.size();
            Vec3d[] list = new Vec3d[count];
            for (int i = 0; i < count; i++) {
                CompoundNBT nbtVector = vectors.getCompound(i);
                double x = nbtVector.getDouble(BIND_NBT_TER_RENDER_VECTOR_X);
                double y = nbtVector.getDouble(BIND_NBT_TER_RENDER_VECTOR_Y);
                double z = nbtVector.getDouble(BIND_NBT_TER_RENDER_VECTOR_Z);
                list[i] = new Vec3d(x, y, z);
            }
            return list;
        }

        private float[] nbt2float(ListNBT values) {
            int count = values.size();
            float[] fList = new float[count];
            for (int i = 0; i < count; i++) {
                fList[i] = values.getFloat(i);
            }
            return fList;
        }
    }
}

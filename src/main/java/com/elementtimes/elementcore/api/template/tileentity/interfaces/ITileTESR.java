package com.elementtimes.elementcore.api.template.tileentity.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.*;

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
        public List<RenderObjectTransformation> transformations = new ArrayList<>();
        
        private boolean isRender = false;
        private boolean isBlock;

        private static final String BIND_NBT_TESR_RENDER = "_nbt_tesr_render_";
        private static final String BIND_NBT_TESR_RENDER_OBJ = "_nbt_tesr_render_obj_";
        private static final String BIND_NBT_TESR_RENDER_EMPTY = "_nbt_tesr_render_empty_";
        private static final String BIND_NBT_TESR_RENDER_TRANSFORMATIONS = "_nbt_tesr_render_t_";
        private static final String BIND_NBT_TESR_RENDER_IS_RENDER = "_nbt_tesr_render_is_render_";
        private static final String BIND_NBT_TESR_RENDER_IS_BLOCK = "_nbt_tesr_render_is_block_";

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
            transformations.add(new RenderObjectTransformation(0, x, y, z));
            return this;
        }

        public RenderObject translate(Vec3d vector) {
            transformations.add(new RenderObjectTransformation(0, vector.x, vector.y, vector.z));
            return this;
        }

        public RenderObject scale(double x, double y, double z) {
            transformations.add(new RenderObjectTransformation(1, x, y, z));
            return this;
        }

        public RenderObject scale(Vec3d vector) {
            transformations.add(new RenderObjectTransformation(1, vector.x, vector.y, vector.z));
            return this;
        }

        public RenderObject rotate(double x, double y, double z, float angle) {
            transformations.add(new RenderObjectTransformation(2, x, y, z, angle));
            return this;
        }

        public RenderObject rotate(Vec3d vector, float angle) {
            transformations.add(new RenderObjectTransformation(2, vector.x, vector.y, vector.z, angle));
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
            renderObject.isBlock = isBlock;
            for (RenderObjectTransformation transformation : transformations) {
                renderObject.transformations.add(transformation.copy());
            }
            return renderObject;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }

            RenderObject that = (RenderObject) object;

            if (isRender != that.isRender) {
                return false;
            }
            if (isBlock != that.isBlock) {
                return false;
            }
            if (!Objects.equals(obj, that.obj)) {
                return false;
            }
            return Objects.equals(transformations, that.transformations);
        }

        @Override
        public int hashCode() {
            int result = obj != null ? obj.hashCode() : 0;
            result = 31 * result + (transformations != null ? transformations.hashCode() : 0);
            result = 31 * result + (isRender ? 1 : 0);
            result = 31 * result + (isBlock ? 1 : 0);
            return result;
        }

        public NBTTagCompound writeNBT(NBTTagCompound nbt) {
            NBTTagCompound nbtRender = new NBTTagCompound();
            if (this.equals(EMPTY)) {
                nbtRender.setBoolean(BIND_NBT_TESR_RENDER_EMPTY, true);
            } else {
                nbtRender.setBoolean(BIND_NBT_TESR_RENDER_EMPTY, false);
                nbtRender.setTag(BIND_NBT_TESR_RENDER_OBJ, obj.writeToNBT(new NBTTagCompound()));
                nbtRender.setBoolean(BIND_NBT_TESR_RENDER_IS_RENDER, isRender);
                nbtRender.setBoolean(BIND_NBT_TESR_RENDER_IS_BLOCK, isBlock);
                NBTTagList list = new NBTTagList();
                for (RenderObjectTransformation transformation : transformations) {
                    list.appendTag(transformation.serializeNBT());
                }
                nbtRender.setTag(BIND_NBT_TESR_RENDER_TRANSFORMATIONS, list);
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
            isBlock = nbtRender.getBoolean(BIND_NBT_TESR_RENDER_IS_BLOCK);
            isRender = nbtRender.getBoolean(BIND_NBT_TESR_RENDER_IS_RENDER);
            transformations.clear();
            NBTTagList list = nbtRender.getTagList(BIND_NBT_TESR_RENDER_TRANSFORMATIONS, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                RenderObjectTransformation transformation = new RenderObjectTransformation(0);
                transformation.deserializeNBT(list.getCompoundTagAt(i));
                transformations.add(transformation);
            }
        }
    }

    class RenderObjectTransformation implements INBTSerializable<NBTTagCompound> {

        public int type;
        public double[] params;

        public RenderObjectTransformation(int type, double... params) {
            this.type = type;
            this.params = params;
        }

        public RenderObjectTransformation copy() {
            return new RenderObjectTransformation(type, params);
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagList list = new NBTTagList();
            for (double param : params) {
                list.appendTag(new NBTTagDouble(param));
            }
            compound.setInteger("t", type);
            compound.setTag("p", list);
            return compound;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            type = nbt.getInteger("t");
            NBTTagList list = nbt.getTagList("p", Constants.NBT.TAG_DOUBLE);
            params = new double[list.tagCount()];
            for (int i = 0; i < list.tagCount(); i++) {
                params[i] = list.getDoubleAt(i);
            }
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }

            RenderObjectTransformation that = (RenderObjectTransformation) object;

            if (type != that.type) {
                return false;
            }
            return Arrays.equals(params, that.params);
        }

        @Override
        public int hashCode() {
            int result = type;
            result = 31 * result + Arrays.hashCode(params);
            return result;
        }
    }
}

package com.elementtimes.elementcore.api.utils;

import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;

public class NBTUtils {

    private static NBTUtils util = null;
    public static NBTUtils getInstance() {
        if (util == null) {
            util = new NBTUtils();
        }
        return util;
    }

    public float[] fromNbtF(NBTTagList list) {
        int count = list.tagCount();
        float[] floats = new float[count];
        for (int i = 0; i < count; i++) {
            floats[i] = list.getFloatAt(i);
        }
        return floats;
    }

    public NBTTagList toNbt(float[] floats) {
        NBTTagList list = new NBTTagList();
        for (float aFloat : floats) {
            list.appendTag(new NBTTagFloat(aFloat));
        }
        return list;
    }
}

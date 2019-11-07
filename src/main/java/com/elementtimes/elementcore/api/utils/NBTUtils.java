package com.elementtimes.elementcore.api.utils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;

import javax.annotation.Nonnull;

public class NBTUtils {

    private static NBTUtils util = null;
    public static NBTUtils getInstance() {
        if (util == null) {
            util = new NBTUtils();
        }
        return util;
    }

    public float[] fromNbtF(ListNBT list) {
        int count = list.size();
        float[] floats = new float[count];
        for (int i = 0; i < count; i++) {
            floats[i] = list.getFloat(i);
        }
        return floats;
    }

    public ListNBT toNbt(float[] floats) {
        ListNBT list = new ListNBT();
        for (float aFloat : floats) {
            list.add(new FloatNBT(aFloat));
        }
        return list;
    }

    public boolean isListEmpty(@Nonnull CompoundNBT compound, String key) {
        if (compound.contains(key)) {
            INBT list = compound.get(key);
            if (list instanceof ListNBT) {
                return ((ListNBT) list).isEmpty();
            }
        }
        return true;
    }
}

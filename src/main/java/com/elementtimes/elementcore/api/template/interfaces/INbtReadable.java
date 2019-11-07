package com.elementtimes.elementcore.api.template.interfaces;

import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;

/**
 * read, write
 * @author luqin
 */
public interface INbtReadable {

    /**
     * 从 NBT 中读取数据
     * @param compound 存有数据的 NBT
     */
    void read(@Nonnull CompoundNBT compound);

    /**
     * 将数据写入 NBT 中
     * @param compound 待写入的 NBT
     * @return 存有数据的 NBT
     */
    @Nonnull
    CompoundNBT write(@Nonnull CompoundNBT compound);
}

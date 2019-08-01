package com.elementtimes.elementcore.util;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.Type;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.TypeReferences;

import java.util.function.Supplier;

public class BlockUtil {

    public static <T extends TileEntity> TileEntityType<T> createTileEntityType(Supplier<T> supplier, String key, Block... blocks) {
        Type type = DataFixesManager
                .getDataFixer()
                .getSchema(DataFixUtils.makeKey(SharedConstants.getVersion().getWorldVersion()))
                .getChoiceType(TypeReferences.BLOCK_ENTITY, key);
        TileEntityType.Builder builder = TileEntityType.Builder.create(supplier, blocks);
        return builder.build(type);
    }
}

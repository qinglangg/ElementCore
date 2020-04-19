package com.elementtimes.elementcore.api.utils;

import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

/**
 * @author luqin2007
 */
public class StorageUtils {

    /**
     * 获取所在世界存档目录获取子目录
     * 当 childDir==null 时直接存档目录
     * @param world 所在世界
     * @param childDir 子目录
     * @return 存档目录
     */
    @Nonnull
    public File saveDir(ServerWorld world, @Nullable String childDir) {
        File root = world.getSaveHandler().getWorldDirectory();
        return childDir == null ? root : new File(root, childDir);
    }
}

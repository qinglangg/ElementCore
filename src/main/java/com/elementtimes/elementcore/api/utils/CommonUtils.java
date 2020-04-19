package com.elementtimes.elementcore.api.utils;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CommonUtils {

    public static Dist getSide() {
        return FMLLoader.getDist();
    }

    public static boolean isServer() {
        return getSide().isDedicatedServer();
    }

    public static boolean isClient() {
        return getSide().isClient();
    }

    public static List<ModFileScanData> allScanData() {
        return ModList.get().getAllScanData();
    }

    public static Stream<ModFileScanData> findScanData(Predicate<IModInfo> check) {
        return allScanData().stream()
                .filter(data -> data.getIModInfoData().stream().flatMap(info -> info.getMods().stream()).anyMatch(check));
    }

    public static Optional<ModFileScanData> findScanData(String id) {
        ModFileInfo info = ModList.get().getModFileById(id);
        return Optional.ofNullable(info == null ? null : info.getFile().getScanResult());
    }
}

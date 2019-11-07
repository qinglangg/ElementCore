package com.elementtimes.elementcore.api.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;

import javax.annotation.Nullable;

/**
 * 与 Forge 和 Minecraft 相关的工具
 * @author luqin2007
 */
public class CommonUtils {

    private static CommonUtils u = null;
    public static CommonUtils getInstance() {
        if (u == null) {
            u = new CommonUtils();
        }
        return u;
    }

    public Dist getSide() {
        return FMLLoader.getDist();
    }

    public boolean isServer() {
        return FMLLoader.getDist().isDedicatedServer();
    }

    public boolean isClient() {
        return FMLLoader.getDist().isClient();
    }

    public net.minecraft.client.world.ClientWorld getClient() {
        return net.minecraft.client.Minecraft.getInstance().world;
    }

    @Nullable
    public ServerWorld getServer() {
        IntegratedServer server = net.minecraft.client.Minecraft.getInstance().getIntegratedServer();
        if (server != null) {
            return server.getWorld(net.minecraft.client.Minecraft.getInstance().world.dimension.getType());
        }
        return null;
    }

    @Nullable
    public Iterable<ServerWorld> getServers() {
        IntegratedServer server = net.minecraft.client.Minecraft.getInstance().getIntegratedServer();
        if (server != null) {
            return server.getWorlds();
        }
        return null;
    }

    @Nullable
    public ServerWorld getServer(@Nullable PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            return ((ServerPlayerEntity) player).getServerWorld();
        } else if (player instanceof net.minecraft.client.entity.player.AbstractClientPlayerEntity) {
            MinecraftServer server = player.getServer();
            return server == null ? null : server.getWorld(player.world.dimension.getType());
        } else {
            return null;
        }
    }
}

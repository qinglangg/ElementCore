package com.elementtimes.elementcore.api.utils;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLEvent;
import net.minecraftforge.fml.common.eventhandler.IContextSetter;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class CommonUtils {

    private static CommonUtils u = null;
    public static CommonUtils getInstance() {
        if (u == null) {
            u = new CommonUtils();
        }
        return u;
    }

    public Side getSide() {
        return FMLCommonHandler.instance().getSide();
    }

    public boolean isServer() {
        return FMLCommonHandler.instance().getSide().isServer();
    }

    public boolean isClient() {
        return FMLCommonHandler.instance().getSide().isClient();
    }

    public void runWithModActive(ModContainer mod, Runnable runnable, @Nullable Object event) {
        final Loader loader = Loader.instance();
        final ModContainer activeModContainer = loader.activeModContainer();
        if (mod != activeModContainer) {
            loader.setActiveModContainer(mod);
            applyModContainerToEvent(event, mod);
            runnable.run();
            applyModContainerToEvent(event, activeModContainer);
            loader.setActiveModContainer(activeModContainer);
        } else {
            runnable.run();
        }
    }

    public <T> T runWithModActive(ModContainer mod, Supplier<T> runnable, @Nullable Object event) {
        final Loader loader = Loader.instance();
        final ModContainer activeModContainer = loader.activeModContainer();
        if (mod != activeModContainer) {
            loader.setActiveModContainer(mod);
            applyModContainerToEvent(event, mod);
            T ret = runnable.get();
            applyModContainerToEvent(event, activeModContainer);
            loader.setActiveModContainer(activeModContainer);
            return ret;
        } else {
            return runnable.get();
        }
    }

    private void applyModContainerToEvent(@Nullable Object event, ModContainer mod) {
        if (event instanceof FMLEvent) {
            ((FMLEvent) event).applyModContainer(mod);
        } else if (event instanceof IContextSetter) {
            ((IContextSetter) event).setModContainer(mod);
        }
    }
}

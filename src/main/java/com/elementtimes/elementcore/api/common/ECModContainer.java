package com.elementtimes.elementcore.api.common;

import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 将当前 mod 的信息
 * @author luqin2007
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ECModContainer {
    public static final Map<String, ECModContainer> MODS = new LinkedHashMap<>();

    public final ModContainer mod;
    public final Logger logger;
    public final ECModElements elements;
    private boolean debugEnable;

    public ECModContainer(ModContainer mod, ECModElements container, boolean debugEnable) {
        this.mod = mod;
        this.logger = LogManager.getLogger(mod.getName());
        this.elements = container;
        this.debugEnable = debugEnable;
    }

    public String name() {
        return mod.getName();
    }

    public String id() {
        return mod.getModId();
    }

    public String version() {
        return mod.getVersion();
    }

    public boolean isDebugMessageEnable() {
        return debugEnable;
    }

    public void enableDebugMessage() {
        debugEnable = true;
    }

    public void disableDebugMessage() {
        debugEnable = false;
    }

    /**
     * 发送 Warn 等级的 Log
     * @param message Log 格式化信息 {}
     * @param params Log 信息格式化成分
     */
    public void warn(String message, Object... params) {
        if (isDebugMessageEnable()) {
            logger.warn(message, params);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name(), id(), version());
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ECModContainer)
                && name().equals(((ECModContainer) obj).name())
                && id().equals(((ECModContainer) obj).id())
                && version().equals(((ECModContainer) obj).version());
    }
}

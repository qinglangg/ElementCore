package com.elementtimes.elementcore.annotation;

import com.elementtimes.elementcore.ElementContainer;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * 将当前 mod 的信息
 * @author luqin2007
 */
@SuppressWarnings("WeakerAccess")
public class ModInfo {
    public final ModContainer mod;
    public final Logger logger;
    public final ElementContainer container;

    public ModInfo(ModContainer mod, ElementContainer container) {
        this.mod = mod;
        this.logger = LogManager.getLogger(mod.getName());
        this.container = container;
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

    /**
     * 发送 Warn 等级的 Log
     * @param message Log 格式化信息 {}
     * @param params Log 信息格式化成分
     */
    public void warn(String message, Object... params) {
        if (container.isDebugMessageEnable()) {
            logger.warn("[{}] " + message, name(), params);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name(), id(), version());
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ModInfo)
                && name().equals(((ModInfo) obj).name())
                && id().equals(((ModInfo) obj).id())
                && version().equals(((ModInfo) obj).version());
    }
}

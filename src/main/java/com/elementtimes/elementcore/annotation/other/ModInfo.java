package com.elementtimes.elementcore.annotation.other;

import org.apache.logging.log4j.Logger;

/**
 * 记录 Mod 信息
 * @author luqin2007
 */
public class ModInfo {
    public Object mod;
    public String modid;
    public String pkgName;
    public Logger logger;

    public ModInfo(Object mod, Logger logger, String modid, String pkgName) {
        this.mod = mod;
        this.logger = logger;
        this.modid = modid;
        this.pkgName = pkgName;
    }
}

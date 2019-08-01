package com.elementtimes.elementcore;

import com.elementtimes.elementcore.annotation.AnnotationInitializer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("elementcore")
public class ElementCore
{
    public static AnnotationInitializer INITIALIZER;

    public ElementCore() {
        INITIALIZER = AnnotationInitializer.initialize(this,
                FMLJavaModLoadingContext.get().getModEventBus(),
                LogManager.getLogger(), "elementcore", "com.elementtimes.elementcore");
    }
}

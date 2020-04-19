package com.example.examplemod;

import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.api.ECModContainer;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("examplemod")
public class ExampleMod {
    public static ECModContainer CONTAINER;
    public ExampleMod() {
        CONTAINER = ElementCore.builder()
                .useSimpleNetwork()
                .useEventNetwork()
                .enableDebugMessage()
                .build();
    }
}

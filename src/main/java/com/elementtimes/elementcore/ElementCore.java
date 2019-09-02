package com.elementtimes.elementcore;

import com.elementtimes.elementcore.api.common.ECModContainer;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.annotation.ModCreativeTabs;
import com.elementtimes.elementcore.api.annotation.ModFluid;
import com.elementtimes.elementcore.api.annotation.ModItem;
import com.elementtimes.elementcore.common.item.Bottle;
import com.elementtimes.elementcore.common.item.DebugStick;
import com.elementtimes.elementcore.common.tab.MainTab;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * 元素核心
 * @author luqin2007
 */
@SuppressWarnings("unused")
@Mod(modid = ElementCore.MODID, name = "Element Core", version = ElementCore.VERSION)
public class ElementCore {
    private static ElementCore INSTANCE = null;

    static final String MODID = "elementcore";
    static final String VERSION = "0.1.0_1.12.2_14.23.5.2768";

    public static ECModElements.Builder builder() {
        return ECModElements.builder();
    }

    public ECModContainer container;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        container = ECModElements.builder().enableDebugMessage().build(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        for (ECModContainer mod : ECModContainer.MODS.values()) {
            mod.elements.fmlEventRegister.onInit(event);
        }
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        for (ECModContainer mod : ECModContainer.MODS.values()) {
            mod.elements.fmlEventRegister.onPostInit(event);
        }
    }

    @Mod.InstanceFactory
    public static ElementCore instance() {
        if (INSTANCE == null) {
            INSTANCE = new ElementCore();
            FluidRegistry.enableUniversalBucket();
        }
        return INSTANCE;
    }

    public static class Items {
        @ModItem(creativeTabKey = "main", itemColorClass = "com.elementtimes.elementcore.client.DebugStickColor")
        @ModItem.HasSubItem(metadatas = {0b0000, 0b0001}, models = {"minecraft:stick", "minecraft:stick"})
        public static Item debugger = new DebugStick();

        @ModItem(creativeTabKey = "main")
        public static Item bottle = new Bottle();
    }

    public static class Fluids {
        @ModFluid(density = -10, creativeTabKey = "main")
        @ModFluid.FluidBlock(creativeTabKey = "main")
        public static Fluid air = new Fluid("elementcore.air",
                new ResourceLocation(ElementCore.MODID, "fluid/air_still"),
                new ResourceLocation(ElementCore.MODID, "fluid/air_fluid"), 0xFF949494);
    }

    public static class Tabs {
        @ModCreativeTabs
        public static CreativeTabs main = new MainTab();
    }
}

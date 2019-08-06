package com.elementtimes.elementcore;

import com.elementtimes.elementcore.annotation.annotations.ModCreativeTabs;
import com.elementtimes.elementcore.annotation.annotations.ModFluid;
import com.elementtimes.elementcore.annotation.annotations.ModItem;
import com.elementtimes.elementcore.common.item.Bottle;
import com.elementtimes.elementcore.common.item.DebugStick;
import com.elementtimes.elementcore.common.tab.MainTab;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
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
@Mod(modid = ElementCore.MODID, name = "Element Core", version = "1.0")
public class ElementCore
{
    static final String MODID = "elementcore";

    public static ElementContainer initialize(FMLPreInitializationEvent event) {
        return ElementContainer.builder().disableDebugMessage().build(event);
    }

    @SuppressWarnings("WeakerAccess")
    public ElementContainer initialize;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        initialize = ElementContainer.builder().disableDebugMessage().build(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        for (ElementContainer initializer : ElementContainer.INITIALIZERS.values()) {
            initializer.fmlEventRegister.onInit();
        }
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        for (ElementContainer initializer : ElementContainer.INITIALIZERS.values()) {
            initializer.fmlEventRegister.onPostInit();
        }
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

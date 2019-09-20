package com.elementtimes.elementcore;

import com.elementtimes.elementcore.api.annotation.ModBlock;
import com.elementtimes.elementcore.api.annotation.ModCreativeTabs;
import com.elementtimes.elementcore.api.annotation.ModItem;
import com.elementtimes.elementcore.api.common.ECModContainer;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.template.tabs.CreativeTabDynamic;
import com.elementtimes.elementcore.common.block.EnergyBox;
import com.elementtimes.elementcore.common.item.DebugStick;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
@SuppressWarnings({"unused", "WeakerAccess"})
@Mod(modid = ElementCore.MODID, name = "Element Core", version = ElementCore.VERSION)
public class ElementCore {
    private static ElementCore INSTANCE = null;

    static final String MODID = "elementcore";
    static final String VERSION = "0.2.1";

    public static ECModElements.Builder builder() {
        return ECModElements.builder();
    }

    public ECModContainer container;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        container = ECModElements.builder().disableDebugMessage().build(event);
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
        @ModItem(creativeTabKey = "main")
        @ModItem.ItemColor("com.elementtimes.elementcore.client.DebugStickColor")
        @ModItem.HasSubItem(metadatas = {0b0000, 0b0001}, models = {"minecraft:stick", "minecraft:stick"})
        public static Item debugger = new DebugStick();
    }

    public static class Tabs {
        @ModCreativeTabs
        public static CreativeTabs main = new CreativeTabDynamic("elementcore.main", 20L, new ItemStack(Items.debugger, 1, 0), new ItemStack(Items.debugger, 1, 1));
    }

//    public static class Blocks {
//        @ModBlock(creativeTabKey = "main")
//        @ModBlock.TileEntity(name = "energy", clazz = "com.elementtimes.elementcore.common.block.tileentity.EnergyBox$TileEntity")
//        public static Block energy = new EnergyBox();
//    }
}

package com.elementtimes.elementcore;

import com.elementtimes.elementcore.api.annotation.ModItem;
import com.elementtimes.elementcore.api.annotation.ModTab;
import com.elementtimes.elementcore.api.annotation.part.Method2;
import com.elementtimes.elementcore.api.annotation.tools.ModColor;
import com.elementtimes.elementcore.api.common.ECModContainer;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.template.tabs.CreativeTabDynamic;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

/**
 * 元素核心
 *
 * @author luqin2007
 */
@SuppressWarnings({"unused"})
@Mod(
        modid = ElementCore.MODID,
        name = "Element Core",
        version = ElementCore.VERSION,
        updateJSON = ElementCore.UPDATE
)
public class ElementCore {
    private static ElementCore INSTANCE = null;

    static final String MODID = "elementcore";
    static final String VERSION = "@version@";
    static final String UPDATE = "https://github.com/luiqn2007/ElementCore/blob/1.12.2/update.json";

    public static ECModElements.Builder builder() {
        return ECModElements.builder();
    }

    @SidedProxy(serverSide = "com.elementtimes.elementcore.CommonProxy",
            clientSide = "com.elementtimes.elementcore.ClientProxy")
    private static CommonProxy PROXY;

    public ECModContainer container;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        PROXY.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        PROXY.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        PROXY.postInit(event);
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        PROXY.onServerStart(event);
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
        @ModColor(item = @Method2(value = "com.elementtimes.elementcore.DebugStick", name = "color"))
        @ModItem.HasSubItem(
                metadatas = {0b0000, 0b0001},
                models = {"minecraft:stick", "minecraft:stick"})
        public static Item debugger = new DebugStick();
    }

    public static class Tabs {
        @ModTab
        public static CreativeTabs main = new CreativeTabDynamic("elementcore.main", 20L,
                new ItemStack(Items.debugger, 1, 0),
                new ItemStack(Items.debugger, 1, 1));
    }
}

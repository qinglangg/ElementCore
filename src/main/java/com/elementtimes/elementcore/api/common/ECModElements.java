package com.elementtimes.elementcore.api.common;

import com.elementtimes.elementcore.api.annotation.enums.GenType;
import com.elementtimes.elementcore.api.annotation.enums.LoadState;
import com.elementtimes.elementcore.api.common.event.*;
import com.elementtimes.elementcore.other.CapabilityObject;
import com.elementtimes.elementcore.other.ModTooltip;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Triple;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.channels.NetworkChannel;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 总入口，用于注册所有事件，收集注解产物
 * @author luqin2007
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ECModElements {
    
    /**
     * CreativeTabs
     */
    public Map<String, CreativeTabs> tabs = null;

    /**
     * Class
     */
    public HashMap<String, Class> classes = new HashMap<>();

    /**
     * Block
     */
    public Map<String, Block> blocks = null;
    public Map<Block, ImmutablePair<String, Class<? extends TileEntity>>> blockTileEntities = null;
    public Map<String, List<Block>> blockOreDictionaries = null;
    public Map<GenType, List<WorldGenerator>> blockWorldGen = null;
    public Object2IntMap<Block> blockBurningTimes = null;
    public Map<Block, ModTooltip[]> blockTooltips = null;
    public boolean blockB3d = false, blockObj = false;

    /**
     * Item
     */
    public Map<String, Item> items = null;
    public Map<String, List<Item>> itemOreDictionaries = null;

    /**
     * Recipe
     */
    public List<Supplier<IRecipe[]>> recipes = null;

    /**
     * Fluid
     */
    public Map<String, Fluid> fluids = null;
    public List<Fluid> fluidBuckets = null;
    public Map<Fluid, CreativeTabs> fluidTabs = null;
    public Map<Fluid, Function<Fluid, Block>> fluidBlocks = null;
    public Map<Fluid, String> fluidBlockStates = null;
    public Object2IntMap<String> fluidBurningTimes = null;
    public List<Fluid> fluidResources = null;

    /**
     * Capability
     */
    public List<CapabilityObject> capabilities = null;

    /**
     * Network
     */
    public List<Triple<Class, Class, Side[]>> networks = null;
    public SimpleNetworkWrapper channel;

    /**
     * Enchantment
     */
    public List<Enchantment> enchantments = null;

    /**
     * Potion
     */
    public List<Potion> potions = null;

    /**
     * Element
     */
    public List<Method> staticFunction = null;
    public Table<LoadState, Class<? extends Annotation>, BiConsumer<ASMDataTable.ASMData, ECModContainer>> customAnnotation;

    /**
     * Event
     */
    public FmlRegister fmlEventRegister;

    /**
     * ModInfo
     */
    public ECModContainer container;
    public Object clientElement;
    public ASMDataTable asm;
    public Set<String> packages;

    /**
     * 通用
     */
    public List<ModTooltip> toolTips = null;

    ECModElements(FMLPreInitializationEvent event, boolean debugEnable, Table<LoadState, Class<? extends Annotation>, BiConsumer<ASMDataTable.ASMData, ECModContainer>> customAnnotation, Set<String> packages, ModContainer modContainer) {
        this.clientElement = ECUtils.common.isClient() ? new com.elementtimes.elementcore.api.client.ECModElementsClient(this) : null;
        this.customAnnotation = customAnnotation;
        this.packages = packages;
        this.container = new ECModContainer(modContainer, this, debugEnable);
        this.asm = event.getAsmData();
        this.fmlEventRegister = new FmlRegister(this);
        // channel name 最大 20
        this.channel = NetworkRegistry.INSTANCE.newSimpleChannel(newChannelName(modContainer.getModId()));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Table<LoadState, Class<? extends Annotation>, BiConsumer<ASMDataTable.ASMData, ECModContainer>> customAnnotation = HashBasedTable.create();
        private Set<String> packages = new LinkedHashSet<>();
        private boolean debugEnable = true;

        public Builder enableDebugMessage() {
            debugEnable = true;
            return this;
        }
        public Builder disableDebugMessage() {
            debugEnable = false;
            return this;
        }
        public Builder registerAnnotation(LoadState state, Class<? extends Annotation> annotationClass, BiConsumer<ASMDataTable.ASMData, ECModContainer> parser) {
            customAnnotation.put(state, annotationClass, parser);
            return this;
        }
        public Builder addSupportPackage(String... packages) {
            Collections.addAll(this.packages, packages);
            return this;
        }

        public ECModContainer build(FMLPreInitializationEvent event) {
            // newInstance
            ModContainer container = Loader.instance().getIndexedModList().get(event.getModMetadata().modId);
            packages.add(container.getMod().getClass().getPackage().getName());
            ECModElements elements = new ECModElements(event, debugEnable, customAnnotation, packages, container);
            ECModContainer.MODS.put(container.getModId(), elements.container);
            // event
            MinecraftForge.ORE_GEN_BUS.register(new OreBusRegister(elements));
            MinecraftForge.EVENT_BUS.register(new TerrainBusRegister(elements));
            MinecraftForge.EVENT_BUS.register(new ForgeRegister(elements));
            MinecraftForge.EVENT_BUS.register(new RuntimeEvent(elements));
            if (ECUtils.common.isClient()) {
                MinecraftForge.EVENT_BUS.register(new com.elementtimes.elementcore.api.client.event.ForgeBusRegisterClient(elements));
                MinecraftForge.EVENT_BUS.register(new com.elementtimes.elementcore.api.client.event.RuntimeEventClient(elements));
            }
            elements.fmlEventRegister.onPreInit(event);
            return elements.container;
        }
    }

    public ECModElements registerAnnotation(LoadState state, Class<? extends Annotation> annotationClass, BiConsumer<ASMDataTable.ASMData, ECModContainer> parser) {
        customAnnotation.put(state, annotationClass, parser);
        return this;
    }

    @SideOnly(Side.CLIENT)
    public com.elementtimes.elementcore.api.client.ECModElementsClient getClientElements() {
        return (com.elementtimes.elementcore.api.client.ECModElementsClient) clientElement;
    }

    private static String newChannelName(String modid) {
        String channelNamePrefix;
        String channelNameSuffix = "_channel";
        if (modid.length() <= 12) {
            channelNamePrefix = modid;
        } else {
            channelNamePrefix = modid.substring(0, 12);
        }
        String name = channelNamePrefix + channelNameSuffix;
        int tryId = 0;
        while (NetworkRegistry.INSTANCE.hasChannel(name, Side.CLIENT)
                || NetworkRegistry.INSTANCE.hasChannel(name, Side.SERVER)) {
            String prefix2 = tryId + channelNamePrefix;
            if (prefix2.length() > 12) {
                prefix2 = prefix2.substring(0, 12);
            }
            name = prefix2 + channelNameSuffix;
            tryId++;
        }
        return name;
    }
}

package com.elementtimes.elementcore.api;

import com.elementtimes.elementcore.api.annotation.CapabilityObject;
import com.elementtimes.elementcore.api.annotation.LoadState;
import com.elementtimes.elementcore.api.annotation.ModTooltip;
import com.elementtimes.elementcore.api.annotation.client.ModelLocation;
import com.elementtimes.elementcore.api.annotation.enums.GenType;
import com.elementtimes.elementcore.api.annotation.register.*;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Triple;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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
    public final HashMap<String, CreativeTabs> tabs = new HashMap<>();

    /**
     * Class
     */
    public final HashMap<String, Class> classes = new HashMap<>();

    /**
     * Block
     */
    public final Table<String, String, Block> blocks = HashBasedTable.create();
    public final Map<Block, ImmutablePair<String, Class<? extends TileEntity>>> blockTileEntities = new HashMap<>();
    @SideOnly(Side.CLIENT)
    public final Map<Block, net.minecraft.client.renderer.block.statemap.IStateMapper> blockStateMaps = new HashMap<>();
    public final Map<Block, ArrayList<ModelLocation>> blockStates = new HashMap<>();
    public final Map<String, List<Block>> blockOreDictionaries = new HashMap<>();
    public final Map<GenType, List<WorldGenerator>> blockWorldGen = new HashMap<>();
    public final Object2IntMap<Block> blockBurningTimes = new Object2IntArrayMap<>();
    @SideOnly(Side.CLIENT)
    public final Map<Class, net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer> blockTesr = new HashMap<>();
    @SideOnly(Side.CLIENT)
    public final Map<net.minecraft.client.renderer.color.IBlockColor, List<Block>> blockColors = new HashMap<>();
    public final Map<String, net.minecraft.client.renderer.color.IBlockColor> blockColorMap = new HashMap<>();
    @SideOnly(Side.CLIENT)
    public final Map<net.minecraft.client.renderer.color.IItemColor, List<Block>> blockItemColors = new HashMap<>();
    public final Map<String, net.minecraft.client.renderer.color.IItemColor> blockItemColorMap = new HashMap<>();
    public final Map<Block, ModTooltip[]> blockTooltips = new LinkedHashMap<>();
    public boolean blockB3d = false, blockObj = false;

    /**
     * Item
     */
    public final Table<String, String, Item> items = HashBasedTable.create();
    public final Map<String, List<Item>> itemOreDictionaries = new HashMap<>();
    public final Map<Item, ArrayList<ModelLocation>> itemSubModel = new HashMap<>();
    @SideOnly(Side.CLIENT)
    public final Map<Item, net.minecraft.client.renderer.ItemMeshDefinition> itemMeshDefinition = new HashMap<>();
    @SideOnly(Side.CLIENT)
    public final Map<net.minecraft.client.renderer.color.IItemColor, List<Item>> itemColors = new HashMap<>();
    public final Map<String, net.minecraft.client.renderer.color.IItemColor> itemColorMap = new HashMap<>();

    /**
     * Recipe
     */
    public final List<Supplier<IRecipe[]>> recipes = new ArrayList<>();

    /**
     * Fluid
     */
    public final Table<String, String, Fluid> fluids = HashBasedTable.create();
    public final List<Fluid> fluidBuckets = new LinkedList<>();
    public final Map<Fluid, CreativeTabs> fluidTabs = new HashMap<>();
    public final Map<Fluid, Function<Fluid, Block>> fluidBlocks = new HashMap<>();
    public final Map<Fluid, String> fluidBlockStates = new HashMap<>();
    public final Object2IntMap<String> fluidBurningTimes = new Object2IntArrayMap<>();
    public final List<Fluid> fluidResources = new LinkedList<>();

    /**
     * Capability
     */
    public final List<CapabilityObject> capabilities = new ArrayList<>();

    /**
     * Network
     */
    public final List<Triple<Class, Class, Side[]>> networks = new ArrayList<>();

    /**
     * Enchantment
     */
    public final SimpleNetworkWrapper channel = null;
    public final List<Enchantment> enchantments = new ArrayList<>();

    /**
     * Element
     */
    public final List<Method> staticFunction = new LinkedList<>();
    public final Table<LoadState, Class<? extends Annotation>, BiConsumer<ASMDataTable.ASMData, ECModContainer>> customAnnotation;

    /**
     * Event
     */
    public final FmlRegister fmlEventRegister;

    /**
     * ModInfo
     */
    public final ECModContainer container;
    public final ASMDataTable asm;
    public final Set<String> packages;

    /**
     * 通用
     */
    public final List<ModTooltip> toolTips = new LinkedList<>();

    ECModElements(FMLPreInitializationEvent event, boolean debugEnable, Table<LoadState, Class<? extends Annotation>, BiConsumer<ASMDataTable.ASMData, ECModContainer>> customAnnotation, Set<String> packages, ModContainer modContainer) {
        this.customAnnotation = customAnnotation;
        this.packages = packages;
        this.container = new ECModContainer(modContainer, this, debugEnable);
        this.asm = event.getAsmData();
        this.fmlEventRegister = new FmlRegister(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Table<LoadState, Class<? extends Annotation>, BiConsumer<ASMDataTable.ASMData, ECModContainer>> customAnnotation = HashBasedTable.create();
        private final Set<String> packages = new LinkedHashSet<>();
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
            final ModContainer container = Loader.instance().getIndexedModList().get(event.getModMetadata().modId);
            packages.add(container.getMod().getClass().getPackage().getName());
            final ECModElements initializer = new ECModElements(event, debugEnable, customAnnotation, packages, container);
            MinecraftForge.ORE_GEN_BUS.register(new OreBusRegister(initializer));
            MinecraftForge.EVENT_BUS.register(new TerrainBusRegister(initializer));
            MinecraftForge.EVENT_BUS.register(new ForgeRegister(initializer));
            MinecraftForge.EVENT_BUS.register(new RuntimeEvent(initializer));
            if (FMLCommonHandler.instance().getSide().isClient()) {
                MinecraftForge.EVENT_BUS.register(new com.elementtimes.elementcore.api.annotation.client.ForgeBusRegisterClient(initializer));
            }
            return initializer.container;
        }
    }

    public ECModElements registerAnnotation(LoadState state, Class<? extends Annotation> annotationClass, BiConsumer<ASMDataTable.ASMData, ECModContainer> parser) {
        customAnnotation.put(state, annotationClass, parser);
        return this;
    }
}

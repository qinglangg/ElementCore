package com.elementtimes.elementcore;

import com.elementtimes.elementcore.annotation.CapabilityObject;
import com.elementtimes.elementcore.annotation.LoadState;
import com.elementtimes.elementcore.annotation.ModInfo;
import com.elementtimes.elementcore.annotation.client.ModelLocation;
import com.elementtimes.elementcore.annotation.enums.GenType;
import com.elementtimes.elementcore.annotation.register.*;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 总入口，用于注册所有事件，收集注解产物
 * @author luqin2007
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ElementContainer {

    private boolean debugEnable = true;

    /**
     * CreativeTabs
     */
    public HashMap<String, CreativeTabs> tabs = new HashMap<>();

    /**
     * Class
     */
    public HashMap<String, Class> classes = new HashMap<>();

    /**
     * Block
     */
    public Table<String, String, Block> blocks = HashBasedTable.create();
    public Map<Block, ImmutablePair<String, Class<? extends TileEntity>>> blockTileEntities = new HashMap<>();
    @SideOnly(Side.CLIENT)
    public Map<Block, net.minecraft.client.renderer.block.statemap.IStateMapper> blockStateMaps = new HashMap<>();
    @SideOnly(Side.CLIENT)
    public Map<Block, ArrayList<ModelLocation>> blockStates = new HashMap<>();
    public Map<Block, String> blockOreDictionaries = new HashMap<>();
    public Map<GenType, List<WorldGenerator>> blockWorldGen = new HashMap<>();
    public Object2IntMap<Block> blockBurningTimes = new Object2IntArrayMap<>();
    @SideOnly(Side.CLIENT)
    public Map<Class, net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer> blockTesr = new HashMap<>();
    public boolean blockB3d = false, blockObj = false;

    /**
     * Item
     */
    public Table<String, String, Item> items = HashBasedTable.create();
    public Map<Item, String> itemOreDictionaries = new HashMap<>();
    public Map<Item, ArrayList<ModelLocation>> itemSubModel = new HashMap<>();
    public Map<Item, Object> itemColors = new HashMap<>();

    /**
     * Recipe
     */
    public List<Supplier<IRecipe[]>> recipes = new ArrayList<>();

    /**
     * Fluid
     */
    public Table<String, String, Fluid> fluids = HashBasedTable.create();
    public List<Fluid> fluidBuckets = new LinkedList<>();
    public Map<Fluid, CreativeTabs> fluidTabs = new HashMap<>();
    public Map<Fluid, Function<Fluid, Block>> fluidBlocks = new HashMap<>();
    public Map<Fluid, String> fluidBlockStates = new HashMap<>();
    public Object2IntMap<String> fluidBurningTimes = new Object2IntArrayMap<>();
    public List<Fluid> fluidResources = new LinkedList<>();

    /**
     * Capability
     */
    public List<CapabilityObject> capabilities = new ArrayList<>();

    /**
     * Network
     */
    public List<Triple<Class, Class, Side[]>> networks = new ArrayList<>();

    /**
     * Enchantment
     */
    public SimpleNetworkWrapper channel = null;
    public List<Enchantment> enchantments = new ArrayList<>();

    /**
     * Element
     */
    public List<Method> staticFunction = new LinkedList<>();
    public Table<LoadState, Class<? extends Annotation>, Consumer<ASMDataTable.ASMData>> customAnnotation;

    /**
     * Event
     */
    public FmlEventRegister fmlEventRegister;

    /**
     * ModInfo
     */
    public final ModInfo modInfo;
    public final ASMDataTable asm;
    public static final Set<ModInfo> MODS = new LinkedHashSet<>();
    public static final Map<ModInfo, ElementContainer> INITIALIZERS = new LinkedHashMap<>();

    ElementContainer(FMLPreInitializationEvent event, boolean debugEnable, Table<LoadState, Class<? extends Annotation>, Consumer<ASMDataTable.ASMData>> customAnnotation) {
        this.customAnnotation = customAnnotation;
        this.debugEnable = debugEnable;
        this.modInfo = new ModInfo(Loader.instance().getIndexedModList().get(event.getModMetadata().modId), this);
        this.asm = event.getAsmData();
        this.fmlEventRegister = new FmlEventRegister(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Table<LoadState, Class<? extends Annotation>, Consumer<ASMDataTable.ASMData>> customAnnotation = HashBasedTable.create();
        private boolean debugEnable = false;

        public Builder enableDebugMessage() {
            debugEnable = true;
            return this;
        }
        public Builder disableDebugMessage() {
            debugEnable = false;
            return this;
        }
        public Builder registerAnnotation(LoadState state, Class<? extends Annotation> annotationClass, Consumer<ASMDataTable.ASMData> parser) {
            customAnnotation.put(state, annotationClass, parser);
            return this;
        }

        public ElementContainer build(FMLPreInitializationEvent event) {
            ElementContainer initializer = new ElementContainer(event, debugEnable, customAnnotation);
            MinecraftForge.ORE_GEN_BUS.register(new OreBusRegister(initializer));
            MinecraftForge.EVENT_BUS.register(new TerrainBusRegister(initializer));
            MinecraftForge.EVENT_BUS.register(new ForgeBusRegister(initializer));
            if (FMLCommonHandler.instance().getSide().isClient()) {
                MinecraftForge.EVENT_BUS.register(new com.elementtimes.elementcore.annotation.client.ForgeBusRegisterClient(initializer));
            }
            MODS.add(initializer.modInfo);
            INITIALIZERS.put(initializer.modInfo, initializer);
            initializer.fmlEventRegister.onPreInit();
            return initializer;
        }
    }

    public boolean isDebugMessageEnable() {
        return debugEnable;
    }

    public ElementContainer enableDebugMessage() {
        debugEnable = true;
        return this;
    }
    public ElementContainer disableDebugMessage() {
        debugEnable = false;
        return this;
    }

    public ElementContainer registerAnnotation(LoadState state, Class<? extends Annotation> annotationClass, Consumer<ASMDataTable.ASMData> parser) {
        customAnnotation.put(state, annotationClass, parser);
        return this;
    }
}

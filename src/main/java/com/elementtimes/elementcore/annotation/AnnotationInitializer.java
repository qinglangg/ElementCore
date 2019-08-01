package com.elementtimes.elementcore.annotation;

import com.elementtimes.elementcore.annotation.annotations.*;
import com.elementtimes.elementcore.annotation.other.IMessageHandler;
import com.elementtimes.elementcore.annotation.other.ModInfo;
import com.elementtimes.elementcore.annotation.processor.*;
import com.elementtimes.elementcore.annotation.register.ForgeBusRegister;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

/**
 * 总入口
 * @author luqin2007
 */
public class AnnotationInitializer {

    public static final Map<Object, AnnotationInitializer> INITIALIZERS = new LinkedHashMap<>();

    public final ModInfo modInfo;
    public final HashMap<Class, ArrayList<AnnotatedElement>> elements = new HashMap<>();

    /**
     * 合成相关
     */
    public final List<Supplier<IRecipe[]>> recipes = new ArrayList<>();
    private boolean loadedRecipes = false;
    public void loadRecipes(World world) {
        if (!loadedRecipes) {
            ModRecipeLoader.load(world, this);
            warn("---> Find {} Recipe Groups", recipes.size());
            loadedRecipes = true;
        }
    }

    /**
     * 流体相关
     */
    public final List<Fluid> fluids = new ArrayList<>();
    public final Object2IntMap<Fluid> fluidBurningTimes = new Object2IntArrayMap<>();
    public final Map<ResourceLocation, Tag.Builder<Fluid>> fluidTags = new LinkedHashMap<>();
    private boolean loadedFluids = false;
    public void loadFluids() {
        if (!loadedFluids) {
            ModFluidLoader.load(this);
            warn("---> Find {} Fluids", fluids.size());
            warn("\tFluid Tags: {}", fluidTags.size());
            warn("\tFluid Burnings: {}", fluidBurningTimes.size());
            loadedFluids = true;
        }
    }

    /**
     * 能力相关
     */
    public final List<ModCapability> capabilities = new ArrayList<>();
    private boolean loadedCapabilities = false;
    public void loadCapabilities() {
        if (!loadedCapabilities) {
            ModCapabilityLoader.load(this);
            warn("---> Find {} Capabilities", capabilities.size());
            loadedCapabilities = true;
        }
    }

    /**
     * 通信相关
     */
    public SimpleChannel networkChannel = null;
    public final List<Object[]> networks = new ArrayList<>();
    private String protocolVersion = "1";
    private boolean loadedNetwork = false;
    public void loadNetwork() {
        if (!loadedNetwork) {
            ModNetworkLoader.getElements(this);
            warn("---> Find {} Network", networks.size());
            loadedNetwork = true;
        }
    }

    /**
     * 附魔相关
     */
    public final List<Enchantment> enchantments = new ArrayList<>();
    private boolean loadedEnchantments = false;
    public void loadEnchantments() {
        if (!loadedEnchantments) {
            ModEnchantmentLoader.load(this);
            warn("---> Find {} Enchantments", enchantments.size());
            loadedEnchantments = true;
        }
    }

    /**
     * 物品相关
     */
    public final List<Item> items = new ArrayList<>();
    public final Map<ResourceLocation, Tag.Builder<Item>> itemTags = new HashMap<>();
    private boolean loadedItems = false;
    public void loadItems() {
        if (!loadedItems) {
            ModItemLoader.load(this);
            warn("---> Find {} Item", items.size());
            warn("\tItem Tags: {}", itemTags.size());
            loadedItems = true;
        }
    }

    /**
     * 方块相关
     */
    public final List<Block> blocks = new LinkedList<>();
    public final List<TileEntityType> blockTileEntities = new LinkedList<>();
    public final Map<ResourceLocation, Tag.Builder<Block>> blockTags = new HashMap<>();
    public final Object2IntMap<Block> blockBurningTimes = new Object2IntArrayMap<>();
    private boolean loadedBlocks = false;
    public void loadBlocks() {
        if (!loadedBlocks) {
            ModBlockLoader.load(this);
            warn("---> Find {} Block", blocks.size());
            warn("\tTileEntity: {}", blockTileEntities.size());
            warn("\tBlock Tags: {}", blockTags.size());
            loadedBlocks = true;
        }
    }

    /**
     * Feature 相关
     */
    public final List<Feature> features = new LinkedList<>();
    private boolean loadedFeatures = false;
    public void loadFeatures() {
        if (!loadedFeatures) {
            ModFeatureLoader.load(this);
            warn("---> Find {} Feature", features.size());
            loadedFeatures = true;
        }
    }

    /**
     * 其他
     */
    public final List<Method> staticFunction = new LinkedList<>();
    private boolean loadedElements = false;
    public void loadElements() {
        if (!loadedElements) {
            ModElementLoader.getElements(this);
            warn("---> Find {} Static Functions", staticFunction.size());
            loadedElements = true;
        }
    }

    public AnnotationInitializer(ModInfo info, IEventBus modEventBus) {
        modInfo = info;
        modEventBus.addListener(this::setupCommon);
        modEventBus.addListener(this::setupClient);
        modEventBus.addListener(this::aboutToStart);
        warn("Annotation common init...");
        ModClassLoader.load(this,
                ModBlock.class, ModItem.class, ModRecipe.class, ModElement.class, ModFluid.class,
                ModCapability.class, ModNetwork.class, ModEnchantment.class, ModFeature.class);
        warn("---> Total Element: {}", elements.values().stream().mapToInt(ArrayList::size).sum());
    }

    public static AnnotationInitializer initialize(Object mod, IEventBus modEventBus, Logger logger, String modid, String packageName) {
        ModInfo modInfo = new ModInfo(mod, logger, modid, packageName);
        AnnotationInitializer initializer = new AnnotationInitializer(modInfo, modEventBus);
        INITIALIZERS.put(mod, initializer);
        // event
        modEventBus.register(new ForgeBusRegister(initializer));
        return initializer;
    }

    private void setupCommon(final FMLCommonSetupEvent event) {
        warn("[{}] {}", modInfo.modid, "setupCommon");
        // preinit
        registerCapabilities();
        // init
        invokeMethods();
        // postInit
        registerNetwork();
    }

    private void setupClient(final FMLClientSetupEvent event) {
        warn("[{}] {}", modInfo.modid, "setupClient");
        // do something that can only be done on the client
        warn("Annotation client init...");
        initClient();
    }

    private void aboutToStart(FMLServerAboutToStartEvent event) {
        warn("[{}] {}", modInfo.modid, "aboutToStart");
        // registry recipe
        loadRecipes(event.getServer().getWorlds().iterator().next());
        RecipeManager recipeManager = event.getServer().getRecipeManager();
        warn("---> Find {} Recipe", recipes.size());
        try {
            Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipes =
                    (Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>>) RecipeManager.class.getDeclaredField("recipes").get(recipeManager);
            Map<ResourceLocation, IRecipe<?>> recipeMap = recipes.get(IRecipeType.CRAFTING);
            this.recipes.stream()
                    .map(Supplier::get)
                    .flatMap(Arrays::stream)
                    .forEach(r -> recipeMap.put(r.getId(), r));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private void initClient() {
        warn("Annotation client init...");
    }

    private void invokeMethods() {
        warn("[{}] {}", modInfo.modid, "invokeMethods");
        loadElements();
        staticFunction.forEach(method -> {
            try {
                method.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                warn("Invoke Failure because {}, the method is {} in {} ", e.getMessage(), method.getName(), method.getDeclaringClass().getSimpleName());
            }
        });
    }

    private void registerCapabilities() {
        warn("[{}] {}", modInfo.modid, "registerCapabilities");
        loadCapabilities();
        capabilities.forEach(modCapability -> {
            try {
                Class type = Class.forName(modCapability.typeInterfaceClass());
                Class impl = Class.forName(modCapability.typeImplementationClass());
                Class storage = Class.forName(modCapability.storageClass());
                Capability.IStorage storageObj = (Capability.IStorage) storage.getConstructor().newInstance();
                CapabilityManager.INSTANCE.register(type, storageObj, () -> impl.getConstructor().newInstance());
            } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
                warn("Can't register the capability, because " + e.getMessage());
            }
        });
    }

    private void registerNetwork() {
        warn("[{}] {}", modInfo.modid, "registerNetwork");
        loadNetwork();
        if (!networks.isEmpty()) {
            networkChannel = NetworkRegistry.newSimpleChannel(
                    new ResourceLocation(modInfo.modid, "network_annotation"),
                    () -> protocolVersion, protocolVersion::equals, protocolVersion::equals);

            networks.forEach(objects -> {
                ModNetwork info = (ModNetwork) objects[0];
                IMessageHandler handler = (IMessageHandler) objects[1];
                Class message = (Class) objects[2];
                int id = info.id();
                networkChannel.registerMessage(id, message, handler::toBuffer, handler::fromBuffer, handler::handler);
            });
        }
    }

    public void warn(String message, Object... params) {
        modInfo.logger.warn(message, params);
    }
}

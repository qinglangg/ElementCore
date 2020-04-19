package com.elementtimes.elementcore.api.common;

import com.elementtimes.elementcore.api.annotation.enums.GenType;
import com.elementtimes.elementcore.api.annotation.enums.LoadState;
import com.elementtimes.elementcore.api.common.events.FmlRegister;
import com.elementtimes.elementcore.api.common.events.ForgeRegister;
import com.elementtimes.elementcore.api.common.events.OreBusRegister;
import com.elementtimes.elementcore.api.common.events.TerrainBusRegister;
import com.elementtimes.elementcore.api.common.loader.*;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.command.ICommand;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.spi.AbstractLogger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.*;

/**
 * 总入口，用于注册所有事件，收集注解产物
 * @author luqin2007
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ECModElements extends AbstractLogger {

    boolean isLoaded = false, isClientLoaded = false;

    /**
     * CreativeTabs
     */
    public final Map<String, CreativeTabs> tabs = new HashMap<>();

    /**
     * Class
     */
    public final Map<String, Class<?>> classes = new HashMap<>();

    /**
     * Block
     */
    public final List<Block> blocks = new ArrayList<>();
    public final Map<Block, ImmutablePair<String, Class<? extends TileEntity>>> blockTileEntities = new HashMap<>();
    public final Map<String, Class<? extends TileEntity>> blockTileEntitiesNull = new HashMap<>();
    public final Map<String, List<Block>> blockOreNames = new HashMap<>();
    public final Map<GenType, List<WorldGenerator>> blockWorldGen = new HashMap<>();

    /**
     * Item
     */
    public final List<Item> items = new ArrayList<>();
    public final Map<String, List<Item>> itemOreNames = new HashMap<>();

    /**
     * Recipe
     */
    public final List<Supplier<IRecipe[]>> recipes = new ArrayList<>();
    public final List<IRecipe> allRecipes = new ArrayList<>();

    /**
     * Fluid
     */
    public final List<Fluid> fluids = new ArrayList<>();
    public final List<Fluid> fluidBuckets = new ArrayList<>();
    public final Map<Fluid, CreativeTabs> fluidTabs = new HashMap<>();
    public final Map<Fluid, Function<Fluid, Block>> fluidBlocks = new HashMap<>();
    public final Map<Fluid, String> fluidBlockResources = new HashMap<>();
    public final List<Fluid> fluidResources = new ArrayList<>();

    /**
     * Capability
     */
    public final List<CapabilityLoader.CapabilityData> capabilities = new ArrayList<>();

    /**
     * Network
     */
    public final List<NetworkLoader.SimpleNetwork> netSimple = new ArrayList<>();
    public final List<Object> netEvent = new ArrayList<>();
    public SimpleNetworkWrapper simpleChannel;
    public FMLEventChannel eventChannel;
    public String simpleChannelName, eventChannelName;

    /**
     * Enchantment
     */
    public final List<Enchantment> enchantments = new ArrayList<>();
    public final List<EnchantmentLoader.EnchantmentBookWrapper> enchantmentBooks = new ArrayList<>();

    /**
     * Potion
     */
    public final List<Potion> potions = new ArrayList<>();
    public final List<PotionType> potionTypes = new ArrayList<>();
    public final List<PotionLoader.PotionBottle> potionBottles = new ArrayList<>();

    /**
     * GUI
     */
    public IGuiHandler guiHandler = null;

    /**
     * Command
     */
    public final List<ICommand> commands = new ArrayList<>();

    /**
     * Entity
     */
    public final List<EntityLoader.EntityData> entities = new ArrayList<>();

    /**
     * Tools
     */
    public final Map<Object, ToIntFunction<ItemStack>> burnTimes = new HashMap<>();
    public final Map<CreativeTabs, List<Consumer<NonNullList<ItemStack>>>> tabEditors = new HashMap<>();
    public final List<BiConsumer<CreativeTabs, NonNullList<ItemStack>>> tabEditorFuns = new ArrayList<>();
    public final List<Method> staticFunction = new ArrayList<>();
    public Table<LoadState, Class<? extends Annotation>, BiConsumer<ASMDataTable.ASMData, ECModContainer>> customAnnotation;
    public boolean blockB3d = false, blockObj = false;

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

    ECModElements(FMLPreInitializationEvent event, boolean debugEnable, boolean netSimple, boolean netEvent,
                  Table<LoadState, Class<? extends Annotation>, BiConsumer<ASMDataTable.ASMData, ECModContainer>> customAnnotation,
                  Set<String> packages, ModContainer modContainer, boolean otherMod, Logger logger) {
        this.customAnnotation = customAnnotation;
        this.packages = packages;
        this.container = new ECModContainer(modContainer, this, debugEnable, otherMod, logger);
        this.asm = event.getAsmData();
        this.fmlEventRegister = new FmlRegister(container);
        this.clientElement = ECUtils.common.isClient() ? new com.elementtimes.elementcore.api.client.ECModElementsClient(this) : null;
        // channel name 最大 20
        simpleChannelName = netSimple ? newChannelName(modContainer.getModId()) : null;
        this.simpleChannel = netSimple ? NetworkRegistry.INSTANCE.newSimpleChannel(simpleChannelName) : null;
        eventChannelName = netEvent ? newChannelName(modContainer.getModId()) : null;
        this.eventChannel = netEvent ? NetworkRegistry.INSTANCE.newEventDrivenChannel(eventChannelName) : null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Table<LoadState, Class<? extends Annotation>, BiConsumer<ASMDataTable.ASMData, ECModContainer>> customAnnotation = HashBasedTable.create();
        private Set<String> packages = new LinkedHashSet<>();
        private boolean debugEnable = false, netSimple = true, netEvent = false;
        private boolean otherMod = false;
        private Logger logger = null;

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
        public Builder withLogger(Logger logger) {
            this.logger = logger;
            return this;
        }
        public Builder useSimpleNetwork() {
            netSimple = true;
            return this;
        }
        public Builder noSimpleNetwork() {
            netSimple = false;
            return this;
        }
        public Builder useEventNetwork() {
            netEvent = true;
            return this;
        }
        public Builder noEventNetwork() {
            netEvent = false;
            return this;
        }
        public Builder usePackage() {
            otherMod = true;
            return this;
        }

        public ECModContainer build(FMLPreInitializationEvent event) {
            // newInstance
            ModContainer container = Loader.instance().getIndexedModList().get(event.getModMetadata().modId);
            // packages.add("");
            packages.add(container.getMod().getClass().getPackage().getName());
            ECModElements elements = new ECModElements(event, debugEnable, netSimple, netEvent, customAnnotation, packages, container, otherMod, logger);
            ECModContainer.MODS.put(container.getModId(), elements.container);
            // event
            MinecraftForge.ORE_GEN_BUS.register(new OreBusRegister(elements.container));
            MinecraftForge.EVENT_BUS.register(new TerrainBusRegister(elements.container));
            MinecraftForge.EVENT_BUS.register(new ForgeRegister(elements.container));
            if (ECUtils.common.isClient()) {
                MinecraftForge.EVENT_BUS.register(new com.elementtimes.elementcore.api.client.event.ForgeBusRegisterClient(elements.container));
                MinecraftForge.EVENT_BUS.register(new com.elementtimes.elementcore.api.client.event.RuntimeEventClient(elements.container));
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
        if (!isLoaded) {
            CommonLoader.load(this);
            isLoaded = true;
        }
        if (!isClientLoaded) {
            com.elementtimes.elementcore.api.client.loader.CommonClientLoader.load(this);
            isClientLoaded = true;
        }
        return getClientNotInit();
    }

    @SideOnly(Side.CLIENT)
    public com.elementtimes.elementcore.api.client.ECModElementsClient getClientNotInit() {
        if (!isLoaded) {
            CommonLoader.load(this);
            isLoaded = true;
        }
        return (com.elementtimes.elementcore.api.client.ECModElementsClient) clientElement;
    }

    @SideOnly(Side.CLIENT)
    public boolean sendToServer(IMessage message) {
        if (simpleChannel == null) {
            return false;
        }
        simpleChannel.sendToServer(message);
        return true;
    }

    public boolean sendToAll(IMessage message) {
        if (simpleChannel == null) {
            return false;
        }
        simpleChannel.sendToAll(message);
        return true;
    }

    public boolean sendToPlayer(IMessage message, EntityPlayerMP player) {
        if (simpleChannel == null) {
            return false;
        }
        simpleChannel.sendTo(message, player);
        return true;
    }

    public boolean sendToAround(IMessage message, NetworkRegistry.TargetPoint point) {
        if (simpleChannel == null) {
            return false;
        }
        simpleChannel.sendToAllAround(message, point);
        return true;
    }

    public boolean sendToTracking(IMessage message, NetworkRegistry.TargetPoint point) {
        if (simpleChannel == null) {
            return false;
        }
        simpleChannel.sendToAllTracking(message, point);
        return true;
    }

    public boolean sendToTracking(IMessage message, Entity entity) {
        if (simpleChannel == null) {
            return false;
        }
        simpleChannel.sendToAllTracking(message, entity);
        return true;
    }

    public boolean sendToDimension(IMessage message, int dimension) {
        if (simpleChannel == null) {
            return false;
        }
        simpleChannel.sendToDimension(message, dimension);
        return true;
    }

    @SideOnly(Side.CLIENT)
    public boolean postToServer(Consumer<ByteBuf> bufWriter) {
        if (eventChannel == null) {
            return false;
        }
        ByteBuf buf = Unpooled.buffer();
        bufWriter.accept(buf);
        eventChannel.sendToServer(new FMLProxyPacket(new PacketBuffer(buf), eventChannelName));
        return true;
    }

    public boolean postToAll(Consumer<ByteBuf> bufWriter, EntityPlayerMP player) {
        if (eventChannel == null) {
            return false;
        }
        ByteBuf buf = Unpooled.buffer();
        bufWriter.accept(buf);
        eventChannel.sendTo(new FMLProxyPacket(new PacketBuffer(buf), eventChannelName), player);
        return true;
    }

    public boolean postToPlayer(Consumer<ByteBuf> bufWriter, EntityPlayerMP player) {
        if (eventChannel == null) {
            return false;
        }
        ByteBuf buf = Unpooled.buffer();
        bufWriter.accept(buf);
        eventChannel.sendTo(new FMLProxyPacket(new PacketBuffer(buf), eventChannelName), player);
        return true;
    }

    public boolean postToAround(Consumer<ByteBuf> bufWriter, NetworkRegistry.TargetPoint point) {
        if (eventChannel == null) {
            return false;
        }
        ByteBuf buf = Unpooled.buffer();
        bufWriter.accept(buf);
        eventChannel.sendToAllAround(new FMLProxyPacket(new PacketBuffer(buf), eventChannelName), point);
        return true;
    }

    public boolean postToTracking(Consumer<ByteBuf> bufWriter, NetworkRegistry.TargetPoint point) {
        if (eventChannel == null) {
            return false;
        }
        ByteBuf buf = Unpooled.buffer();
        bufWriter.accept(buf);
        eventChannel.sendToAllTracking(new FMLProxyPacket(new PacketBuffer(buf), eventChannelName), point);
        return true;
    }

    public boolean postToTracking(Consumer<ByteBuf> bufWriter, Entity entity) {
        if (eventChannel == null) {
            return false;
        }
        ByteBuf buf = Unpooled.buffer();
        bufWriter.accept(buf);
        eventChannel.sendToAllTracking(new FMLProxyPacket(new PacketBuffer(buf), eventChannelName), entity);
        return true;
    }

    public boolean postToDimension(Consumer<ByteBuf> bufWriter, int dimension) {
        if (eventChannel == null) {
            return false;
        }
        ByteBuf buf = Unpooled.buffer();
        bufWriter.accept(buf);
        eventChannel.sendToDimension(new FMLProxyPacket(new PacketBuffer(buf), eventChannelName), dimension);
        return true;
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

    // logger
    @Override
    public boolean isEnabled(Level level, Marker marker, Message message, Throwable t) {
        return container.isDebugMessageEnable();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, CharSequence message, Throwable t) {
        return container.isDebugMessageEnable();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Throwable t) {
        return container.isDebugMessageEnable();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message) {
        return container.isDebugMessageEnable();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Object message, Throwable t) {
        return container.isDebugMessageEnable();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object... params) {
        return container.isDebugMessageEnable();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0) {
        return container.isDebugMessageEnable();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1) {
        return container.isDebugMessageEnable();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
        return container.isDebugMessageEnable();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        return container.isDebugMessageEnable();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return container.isDebugMessageEnable();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return container.isDebugMessageEnable();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return container.isDebugMessageEnable();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return container.isDebugMessageEnable();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return container.isDebugMessageEnable();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return container.isDebugMessageEnable();
    }

    @Override
    public void logMessage(String fqcn, Level level, Marker marker, Message message, Throwable t) {
        container.logger.log(level, marker, message, t);
    }

    @Override
    public Level getLevel() {
        return container.logger.getLevel();
    }
}

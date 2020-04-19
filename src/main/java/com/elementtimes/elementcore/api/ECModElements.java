package com.elementtimes.elementcore.api;

import com.elementtimes.elementcore.api.misc.data.Config;
import com.elementtimes.elementcore.api.misc.wrapper.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.client.CCustomPayloadLoginPacket;
import net.minecraft.network.login.server.SCustomPayloadLoginPacket;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.network.ICustomPacket;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.event.EventNetworkChannel;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.spi.AbstractLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 总入口，用于注册所有事件，收集注解产物
 * @author luqin2007
 */
@SuppressWarnings("unused")
public class ECModElements extends AbstractLogger {

    public boolean isLoaded = false;

    /**
     * Class
     */
    public final Map<String, Class<?>> classes = new HashMap<>();

    /**
     * IItemProvider
     */
    public final List<Block> blocks = new ArrayList<>();
    public final List<BlockItem> blockItems = new ArrayList<>();
    public final List<TileEntityType<?>> teTypes = new ArrayList<>();
    public final List<BlockTerWrapper> ters = new ArrayList<>();
    public final List<BlockFeatureWrapper> features = new ArrayList<>();
    public final List<BlockColorWrapper> blockColors = new ArrayList<>();
    public final List<Item> items = new ArrayList<>();
    public final List<ItemColorWrapper> itemColors = new ArrayList<>();

    /**
     * Fluid
     */
    public final List<Fluid> fluids = new ArrayList<>();
    public final List<Item> buckets = new ArrayList<>();

    /**
     * Capability
     */
    public final List<CapabilityWrapper> capabilities = new ArrayList<>();

    /**
     * Network
     */
    public final List<NetSimpleWrapper> netSimples = new ArrayList<>();
    public final List<NetEventWrapper> netEvents = new ArrayList<>();
    public SimpleChannel simpleChannel;
    public EventNetworkChannel eventChannel;
    public ResourceLocation simpleChannelName, eventChannelName;

    /**
     * Enchantment
     */
    public final List<Enchantment> enchantments = new ArrayList<>();
    public final List<EnchantmentBookWrapper> enchantmentBooks = new ArrayList<>();

    /**
     * Potion
     */
    public final List<Potion> potions = new ArrayList<>();
    public final List<Effect> effects = new ArrayList<>();

    /**
     * GUI
     */
    public final List<ContainerType<?>> containerTypes = new ArrayList<>();
    public final List<ScreenWrapper> containerScreens = new ArrayList<>();

    /**
     * Command
     */
    public final List<CommandNode<CommandSource>> commands = new ArrayList<>();
    public final List<LiteralArgumentBuilder<CommandSource>> commandBuilders = new ArrayList<>();

    /**
     * Entity
     */
    public final List<EntityType<?>> entities = new ArrayList<>();
    public final List<EntityRendererWrapper> entityRenders = new ArrayList<>();
    public final List<SpawnEggItem> entityEggs = new ArrayList<>();
    public final List<EntitySpawnWrapper> entitySpawns = new ArrayList<>();

    /**
     * Key
     */
    public final List<KeyWrapper> keys = new ArrayList<>();

    /**
     * Tools
     */
    public final List<BurnTimeWrapper> burnTimes = new ArrayList<>();
    public final List<TooltipsWrapper> tooltips = new ArrayList<>();
    public Config config;
    public ECModContainer container;

    /**
     * 自动生成
     */
    public final Map<Class<? extends Item>, Item> generatedItems = new HashMap<>();
    public final Map<Class<? extends Block>, Block> generatedBlocks = new HashMap<>();
    public final Map<Class<? extends Block>, BlockItem> generatedBlockItems = new HashMap<>();
    public final Map<Class<? extends TileEntity>, TileEntityType<?>> generatedTileEntityTypes = new HashMap<>();
    public final Map<Class<? extends CommandNode>, CommandNode> generatedCommandNodes = new HashMap<>();
    public final Map<Class<? extends LiteralArgumentBuilder>, LiteralArgumentBuilder> generatedCommandBuilders = new HashMap<>();
    public final Map<Class<? extends Enchantment>, Enchantment> generatedEnchantments = new HashMap<>();
    public final Map<Class<? extends Entity>, EntityType<?>> generatedEntityTypes = new HashMap<>();
    public final Map<EntityType<?>, SpawnEggItem> generatedEntityEggs = new HashMap<>();
    public final Map<Class<?>, ContainerType<?>> generatedContainerTypes = new HashMap<>();
    public final Map<Class<? extends Potion>, Potion> generatedPotions = new HashMap<>();
    public final Map<Class<? extends Effect>, Effect> generatedEffects = new HashMap<>();

    ECModElements(boolean debugEnable,
                  boolean netSimple, ResourceLocation netSimpleName, Supplier<String> simpleNetVersion, Predicate<String> simpleClientVersions, Predicate<String> simpleServerVersions,
                  boolean netEvent, ResourceLocation netEventName, Supplier<String> eventNetVersion, Predicate<String> eventClientVersions, Predicate<String> eventServerVersions,
                  Config config, ModContainer modContainer, Logger logger) {
        this.container = new ECModContainer(modContainer, this, debugEnable, logger);
        this.config = config;
        simpleChannelName = netSimple ? netSimpleName == null ? new ResourceLocation(modContainer.getModId(), "simple_channel_elementcore") : netSimpleName : null;
        simpleChannel = netSimple ? NetworkRegistry.newSimpleChannel(simpleChannelName, simpleNetVersion, simpleClientVersions, simpleServerVersions) : null;
        eventChannelName = netEvent ? netEventName == null ? new ResourceLocation(modContainer.getModId(), "event_channel_elementcore") : netEventName : null;
        eventChannel = netEvent ? NetworkRegistry.newEventChannel(eventChannelName, eventNetVersion, eventClientVersions, eventServerVersions) : null;
        new ECEventHandler(container);
        warn("Mod {} is added.", container.id());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private boolean debugEnable = false, netSimple = true, netEvent = false;
        private Logger logger = null;
        private ResourceLocation netSimpleName = null, netEventName = null;
        private Supplier<String> simpleNetVersion = () -> "", eventNetVersion = () -> "";
        private Predicate<String> simpleClientVersions = s -> true, eventClientVersions = s -> true;
        private Predicate<String> simpleServerVersions = s -> true, eventServerVersions = s -> true;
        private Config config = new Config();

        public Builder enableDebugMessage() {
            debugEnable = true;
            return this;
        }
        public Builder disableDebugMessage() {
            debugEnable = false;
            return this;
        }
        public Builder setLogger(Logger logger) {
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
        public Builder setSimpleChannelName(ResourceLocation name) {
            netSimpleName = name;
            return this;
        }
        public Builder setSimpleChannelName(String namespace, String name) {
            netSimpleName = new ResourceLocation(namespace, name);
            return this;
        }
        public Builder setSimpleNetProtocolVersion(String version) {
            simpleNetVersion = () -> version;
            return this;
        }
        public Builder setSimpleNetProtocolVersion(Supplier<String> version) {
            simpleNetVersion = version;
            return this;
        }
        public Builder setSimpleNetClientAcceptedVersion(Predicate<String> acceptedVersion) {
            this.simpleClientVersions = acceptedVersion;
            return this;
        }
        public Builder setSimpleNetClientAcceptedVersion(String... versions) {
            this.simpleClientVersions = s -> ArrayUtils.contains(versions, s);
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
        public Builder setEventChannelName(ResourceLocation name) {
            netEventName = name;
            return this;
        }
        public Builder setEventChannelName(String namespace, String name) {
            netEventName = new ResourceLocation(namespace, name);
            return this;
        }
        public Builder setEventNetProtocolVersion(String version) {
            eventNetVersion = () -> version;
            return this;
        }
        public Builder setEventNetProtocolVersion(Supplier<String> version) {
            eventNetVersion = version;
            return this;
        }
        public Builder setEventNetClientAcceptedVersion(Predicate<String> acceptedVersion) {
            this.eventClientVersions = acceptedVersion;
            return this;
        }
        public Builder setEventNetClientAcceptedVersion(String... versions) {
            this.eventClientVersions = s -> ArrayUtils.contains(versions, s);
            return this;
        }
        public Builder enableOBJModel() {
            config.useOBJ = true;
            return this;
        }
        public Builder enableB3DModel() {
            config.useB3D = true;
            return this;
        }

        public ECModContainer build() {
            // newInstance
            ModContainer container = ModLoadingContext.get().getActiveContainer();
            ECModElements elements = new ECModElements(debugEnable,
                    netSimple, netSimpleName, simpleNetVersion, simpleClientVersions, simpleServerVersions,
                    netEvent, netEventName, eventNetVersion, eventClientVersions, eventServerVersions,
                    config, container, logger);
            ECModContainer.MODS.put(container.getModId(), elements.container);
            return elements.container;
        }
    }

    // ================================================== network =================================================== //

    public boolean sendTo(Object message, NetworkManager net, NetworkDirection direction) {
        if (simpleChannel == null) {
            return false;
        }
        simpleChannel.sendTo(message, net, direction);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean sendToServer(Object message) {
        if (simpleChannel == null) {
            return false;
        }
        simpleChannel.sendToServer(message);
        return true;
    }

    public boolean sendTo(Object message, ServerPlayerEntity player) {
        return sendTo(message, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    public boolean postTo(PacketBuffer message, NetworkManager net, NetworkDirection direction) {
        if (eventChannel == null) {
            return false;
        }
        ICustomPacket<? extends IPacket<?>> packet;
        switch (direction) {
            case PLAY_TO_CLIENT:
                packet = new SCustomPayloadPlayPacket(eventChannelName, message);
                break;
            case PLAY_TO_SERVER:
                packet = new CCustomPayloadPacket(eventChannelName, message);
                break;
            case LOGIN_TO_CLIENT:
                packet = new SCustomPayloadLoginPacket();
                packet.setData(message);
                break;
            case LOGIN_TO_SERVER:
                packet = new CCustomPayloadLoginPacket(0, message);
                packet.setData(message);
                break;
            default: return false;
        }
        return NetworkHooks.onCustomPayload(packet, net);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean postToServer(PacketBuffer message) {
        return postTo(message, net.minecraft.client.Minecraft.getInstance().getConnection().getNetworkManager(), NetworkDirection.PLAY_TO_SERVER);
    }

    public boolean postTo(PacketBuffer message, ServerPlayerEntity player) {
        return postTo(message, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    // ================================================== Logger =====================================================//

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

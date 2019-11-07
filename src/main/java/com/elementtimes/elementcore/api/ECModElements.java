package com.elementtimes.elementcore.api;

import com.elementtimes.elementcore.api.event.FmlRegister;
import com.elementtimes.elementcore.api.event.ForgeRegister;
import com.elementtimes.elementcore.api.event.RuntimeEvent;
import com.elementtimes.elementcore.api.loader.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 总入口，用于注册所有事件，收集注解产物
 * @author luqin2007
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ECModElements {

    public final BlockLoader blocks;
    public final CapabilityLoader capabilities;
    public final ContainerTypeLoader containerTypes;
    public final ElementLoader elements;
    public final EnchantmentLoader enchantments;
    public final FluidLoader fluids;
    public final ItemGroupLoader itemGroups;
    public final ItemLoader items;
    public final MethodLoader methods;
    public final NetworkLoader networks;
    public final TileEntityTypeLoader tileEntityTypes;
    public final RecipeLoader recipes;
    public final PotionLoader potions;

    public final HashMap<String, Class> classes = new HashMap<>();
    public final SimpleChannel channel;
    public int nextNetworkId = 0;
    public final FMLModContainer container;
    public final ModFileScanData data;
    public final Logger logger;
    public boolean isLoad = false;
    private boolean debugEnable;
    private final Object clientElement;
    public final static Map<String, ECModElements> MODS = new HashMap<>();

    ECModElements(boolean debugEnable,
                  FMLModContainer modContainer, ModFileScanData data,
                  String netId, String netVer) {
        this.debugEnable = debugEnable;
        this.clientElement = ECUtils.common.isClient() ? new ECModElementsClient(this) : null;
        this.container = modContainer;
        this.logger = LogManager.getLogger(modContainer.getModInfo().getModId());
        this.data = data;
        this.channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(container.getModId(), netId), () -> netVer, netVer::equals, netVer::equals);

        blocks = new BlockLoader(this);
        capabilities = new CapabilityLoader(this);
        containerTypes = new ContainerTypeLoader(this);
        elements = new ElementLoader(this);
        enchantments = new EnchantmentLoader(this);
        fluids = new FluidLoader(this);
        itemGroups = new ItemGroupLoader(this);
        items = new ItemLoader(this);
        methods = new MethodLoader(this);
        networks = new NetworkLoader(this);
        tileEntityTypes = new TileEntityTypeLoader(this);
        recipes = new RecipeLoader(this);
        potions = new PotionLoader(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private boolean debugEnable = true;
        private String networkVersion = "1", networkId = "mod_network";

        public Builder enableDebugMessage() {
            debugEnable = true;
            return this;
        }
        public Builder disableDebugMessage() {
            debugEnable = false;
            return this;
        }
        public Builder withNetworkInfo(String id, String version) {
            networkId = id;
            networkVersion = version;
            return this;
        }

        public ECModElements build() {
            // newInstance
            FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
            IEventBus eventBus = context.getModEventBus();
            Logger logger = LogManager.getLogger();
            FMLModContainer container = (FMLModContainer) ModLoadingContext.get().getActiveContainer();
            ModFileScanData data = null;
            Optional<ModFileScanData> dataOptional = ECUtils.reflect.getField(FMLModContainer.class, ModFileScanData.class, container, logger);
            if (dataOptional.isPresent()) {
                data = dataOptional.get();
            }
            ECModElements elements = new ECModElements(debugEnable, container, data, networkId, networkVersion);
            MODS.put(container.getModId(), elements);
            // event
            eventBus.register(new ForgeRegister(elements));
            eventBus.register(new RuntimeEvent(elements));
            eventBus.addListener((new FmlRegister(elements))::setup);
            if (ECUtils.common.isClient()) {
                container.getEventBus().register(new com.elementtimes.elementcore.api.event.client.ForgeBusRegisterClient(elements));
                container.getEventBus().register(new com.elementtimes.elementcore.api.event.client.RuntimeEventClient(elements));
                container.getEventBus().register(new com.elementtimes.elementcore.api.event.client.FmlClientRegister(elements));
            }
            return elements;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public ECModElementsClient client() {
        return (ECModElementsClient) clientElement;
    }

    public String name() {
        return container.getModInfo().getDisplayName();
    }

    public String id() {
        return container.getModInfo().getModId();
    }

    public ArtifactVersion version() {
        return container.getModInfo().getVersion();
    }

    public boolean isDebugMessageEnable() {
        return debugEnable;
    }

    public void enableDebugMessage() {
        debugEnable = true;
    }

    public void disableDebugMessage() {
        debugEnable = false;
    }

    /**
     * 发送 Warn 等级的 Log
     * @param message Log 格式化信息 {}
     * @param params Log 信息格式化成分
     */
    public void warn(String message, Object... params) {
        if (isDebugMessageEnable()) {
            logger.warn(message, params);
        }
    }
}

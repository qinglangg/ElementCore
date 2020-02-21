package com.elementtimes.elementcore.api.common.event;

import com.elementtimes.elementcore.api.annotation.enums.LoadState;
import com.elementtimes.elementcore.api.common.ECModContainer;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.common.loader.CapabilityLoader;
import com.elementtimes.elementcore.api.common.loader.NetworkLoader;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * 对于 FML 生命周期事件的注册
 * @author luqin2007
 */
public class FmlRegister {

    private ECModContainer mContainer;

    public FmlRegister(ECModContainer container) {
        mContainer = container;
    }

    public ECModElements elements() {
        return mContainer.elements();
    }
    
    public Logger logger() {
        return mContainer.elements;
    }
    
    public void onPreInit(FMLPreInitializationEvent event) {
        ECUtils.common.runWithModActive(mContainer.mod, () -> {
            registerCapability();
            registerFluids();
            runCustomAnnotation(LoadState.PreInit);
        }, event);
    }

    public void onPostInit(FMLPostInitializationEvent event) {
        ECUtils.common.runWithModActive(mContainer.mod, () -> {
            registerNetwork();
            runCustomAnnotation(LoadState.PostInit);
        }, event);
    }

    public void onInit(FMLInitializationEvent event) {
        ECUtils.common.runWithModActive(mContainer.mod, () -> {
            registerGui();
            invokeMethods();
            runCustomAnnotation(LoadState.Init);
        }, event);
    }

    public void onServerStart(FMLServerStartingEvent event) {
        ECUtils.common.runWithModActive(mContainer.mod, () -> {
            elements().commands.forEach(event::registerServerCommand);
        }, event);
    }

    private void registerCapability() {
        for (CapabilityLoader.CapabilityData data : elements().capabilities) {
            CapabilityManager.INSTANCE.register(data.typeInterface, data.storage, data::factory);
        }
    }

    private void registerGui() {
        if (elements().guiHandler != null) {
            NetworkRegistry.INSTANCE.registerGuiHandler(mContainer.mod.getMod(), elements().guiHandler);
        }
    }

    private void invokeMethods() {
        for (Method method : elements().staticFunction) {
            try {
                logger().warn("Invoke: " + method.getName());
                method.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger().warn("Invoke Failure because {}, the method is {} in {} ", e.getMessage(), method.getName(), method.getDeclaringClass().getSimpleName());
            }
        }
    }

    private void registerFluids() {
        for (Fluid fluid : elements().fluids) {
            logger().warn("[Fluid]Register: " + fluid.getName());
            if (!FluidRegistry.registerFluid(fluid)) {
                logger().warn("The name {} has been registered to another fluid!", fluid.getName());
            }
        }

        for (Fluid fluidBucket : elements().fluidBuckets) {
            logger().warn("[Fluid]Bucket: " + fluidBucket.getName());
            FluidRegistry.addBucketForFluid(fluidBucket);
        }

        elements().fluidBlocks.forEach((fluid, fluidBlockFunction) -> {
            logger().warn("[Fluid]Block: " + fluid.getName());
            fluid.setBlock(fluidBlockFunction.apply(fluid));
        });
    }

    private void registerNetwork() {
        ECModElements elements = elements();
        for (int i = 0; i < elements.netSimple.size(); i++) {
            NetworkLoader.SimpleNetwork network = elements.netSimple.get(i);
            if (network.server) {
                elements.warn("Network[S]{}: {}", i, network.message);
                elements.simpleChannel.registerMessage(network.handler, network.message, i, Side.SERVER);
            }
            if (network.client) {
                elements.warn("Network[C]{}: {}", i, network.message);
                elements.simpleChannel.registerMessage(network.handler, network.message, i, Side.CLIENT);
            }
        }
        for (Object o : elements.netEvent) {
            elements.eventChannel.register(o);
        }
    }

    private void runCustomAnnotation(LoadState state) {
        final Map<Class<? extends Annotation>, BiConsumer<ASMDataTable.ASMData, ECModContainer>> consumerMap = elements().customAnnotation.row(state);
        if (consumerMap != null && !consumerMap.isEmpty()) {
            consumerMap.forEach((aClass, asmDataConsumer) -> {
                logger().warn("Custom Annotation: [" + state.name() + "] => " + aClass.getName());
                final Set<ASMDataTable.ASMData> asmDataSet = elements().asm.getAll(aClass.getName());
                if (asmDataSet != null) {
                    asmDataSet.forEach(set -> asmDataConsumer.accept(set, mContainer));
                }
            });
        }
    }
}

package com.elementtimes.elementcore.annotation.register;

import com.elementtimes.elementcore.ElementContainer;
import com.elementtimes.elementcore.annotation.LoadState;
import com.elementtimes.elementcore.annotation.client.ClientLoader;
import com.elementtimes.elementcore.annotation.common.ServerLoader;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Triple;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 对于 FML 生命周期事件的注册
 * @author luqin2007
 */
@SuppressWarnings("WeakerAccess")
public class FmlEventRegister {

    private ElementContainer mInitializer;

    public FmlEventRegister(ElementContainer initializer) {
        mInitializer = initializer;
    }

    public void onPreInit() {
        load();
        registerFluids();
        runCustomAnnotation(LoadState.PreInit);
    }

    public void onPostInit() {
        registerNetwork();
        runCustomAnnotation(LoadState.PostInit);
    }

    public void onInit() {
        registerCapabilities();
        invokeMethods();
        runCustomAnnotation(LoadState.Init);
    }

    public void load() {
        mInitializer.modInfo.warn("Annotation init start...");
        ServerLoader.load(mInitializer);
        if (FMLCommonHandler.instance().getSide().isClient()) {
            ClientLoader.load(mInitializer);
        }
        mInitializer.modInfo.warn("Annotation init finished...");
    }

    public void invokeMethods() {
        mInitializer.staticFunction.forEach(method -> {
            try {
                method.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                mInitializer.modInfo.warn("Invoke Failure because {}, the method is {} in {} ", e.getMessage(), method.getName(), method.getDeclaringClass().getSimpleName());
            }
        });
    }

    public void registerFluids() {
        mInitializer.fluids.values().forEach(fluid -> {
            if (!FluidRegistry.registerFluid(fluid)) {
                mInitializer.modInfo.warn("The name {} has been registered to another fluid!", fluid.getName());
            }
        });

        mInitializer.fluidBuckets.forEach(FluidRegistry::addBucketForFluid);

        mInitializer.fluidBlocks.forEach((fluid, fluidBlockFunction) ->
                fluid.setBlock(fluidBlockFunction.apply(fluid)));
    }

    public void registerCapabilities() {
        //noinspection unchecked
        mInitializer.capabilities.forEach(capability ->
                CapabilityManager.INSTANCE.register(capability.typeInterfaceClass, capability.storageInstance(), capability::newInstance));
    }

    public void registerNetwork() {
        if (!mInitializer.networks.isEmpty()) {
            SimpleNetworkWrapper channel = NetworkRegistry.INSTANCE.newSimpleChannel(mInitializer.modInfo.id() + "_network_annotation");

            for (int i = 0; i < mInitializer.networks.size(); i++) {
                Triple<Class, Class, Side[]> triple = mInitializer.networks.get(i);
                for (Side side : triple.getRight()) {
                    //noinspection unchecked
                    channel.registerMessage(triple.getLeft(), triple.getMiddle(), i, side);
                }
            }
        }
    }

    public void runCustomAnnotation(LoadState state) {
        final Map<Class<? extends Annotation>, Consumer<ASMDataTable.ASMData>> consumerMap = mInitializer.customAnnotation.row(state);
        if (consumerMap != null && !consumerMap.isEmpty()) {
            consumerMap.forEach((aClass, asmDataConsumer) -> {
                final Set<ASMDataTable.ASMData> asmDataSet = mInitializer.asm.getAll(aClass.getName());
                if (asmDataSet != null) {
                    asmDataSet.forEach(asmDataConsumer);
                }
            });
        }
    }
}

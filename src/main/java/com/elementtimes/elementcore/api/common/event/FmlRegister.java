package com.elementtimes.elementcore.api.common.event;

import com.elementtimes.elementcore.api.common.ECModContainer;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.annotation.enums.LoadState;
import com.elementtimes.elementcore.api.common.CommonLoader;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Triple;

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

    private ECModElements mElements;

    public FmlRegister(ECModElements elements) {
        mElements = elements;
    }

    public void onPreInit(FMLPreInitializationEvent event) {
        ECUtils.common.runWithModActive(mElements.container.mod, () -> {
            load();
            registerFluids();
            runCustomAnnotation(LoadState.PreInit);
        }, event);
    }

    public void onPostInit(FMLPostInitializationEvent event) {
        ECUtils.common.runWithModActive(mElements.container.mod, () -> {
            registerNetwork();
            runCustomAnnotation(LoadState.PostInit);
        }, event);
    }

    public void onInit(FMLInitializationEvent event) {
        ECUtils.common.runWithModActive(mElements.container.mod, () -> {
            invokeMethods();
            runCustomAnnotation(LoadState.Init);
        }, event);
    }

    private void load() {
        mElements.container.warn("Annotation init start...");
        CommonLoader.load(mElements);
        if (ECUtils.common.isClient()) {
            com.elementtimes.elementcore.api.client.ClientLoader.load(mElements);
        }
        mElements.container.warn("Annotation init finished...");
    }

    private void invokeMethods() {
        for (Method method : mElements.staticFunction) {
            try {
                mElements.container.warn("Invoke: " + method.getName());
                method.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                mElements.container.warn("Invoke Failure because {}, the method is {} in {} ", e.getMessage(), method.getName(), method.getDeclaringClass().getSimpleName());
            }
        }
    }

    private void registerFluids() {
        for (Fluid fluid : mElements.fluids.values()) {
            mElements.container.warn("[Fluid]Register: " + fluid.getName());
            if (!FluidRegistry.registerFluid(fluid)) {
                mElements.container.warn("The name {} has been registered to another fluid!", fluid.getName());
            }
        }

        for (Fluid fluidBucket : mElements.fluidBuckets) {
            mElements.container.warn("[Fluid]Bucket: " + fluidBucket.getName());
            FluidRegistry.addBucketForFluid(fluidBucket);
        }

        mElements.fluidBlocks.forEach((fluid, fluidBlockFunction) -> {
            mElements.container.warn("[Fluid]Block: " + fluid.getName());
            fluid.setBlock(fluidBlockFunction.apply(fluid));
        });
    }

    private void registerNetwork() {
        for (int i = 0; i < mElements.networks.size(); i++) {
            Triple<Class, Class, Side[]> triple = mElements.networks.get(i);
            for (Side side : triple.getRight()) {
                mElements.container.warn("Network: " + i + "[ " + side.name() + " ]: " + triple.getMiddle().getName());
                //noinspection unchecked
                mElements.channel.registerMessage(triple.getLeft(), triple.getMiddle(), i, side);
            }
        }
    }

    private void runCustomAnnotation(LoadState state) {
        final Map<Class<? extends Annotation>, BiConsumer<ASMDataTable.ASMData, ECModContainer>> consumerMap = mElements.customAnnotation.row(state);
        if (consumerMap != null && !consumerMap.isEmpty()) {
            consumerMap.forEach((aClass, asmDataConsumer) -> {
                mElements.container.warn("Custom Annotation: [" + state.name() + "] => " + aClass.getName());
                final Set<ASMDataTable.ASMData> asmDataSet = mElements.asm.getAll(aClass.getName());
                if (asmDataSet != null) {
                    asmDataSet.forEach(set -> asmDataConsumer.accept(set, mElements.container));
                }
            });
        }
    }
}

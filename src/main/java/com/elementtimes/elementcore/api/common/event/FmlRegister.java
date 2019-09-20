package com.elementtimes.elementcore.api.common.event;

import com.elementtimes.elementcore.api.common.ECModContainer;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.annotation.enums.LoadState;
import com.elementtimes.elementcore.api.common.CommonLoader;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
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
            registerOreNames();
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
        if (mElements.staticFunction != null) {
            mElements.staticFunction.forEach(method -> {
                try {
                    method.invoke(null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    mElements.container.warn("Invoke Failure because {}, the method is {} in {} ", e.getMessage(), method.getName(), method.getDeclaringClass().getSimpleName());
                }
            });
        }
    }

    private void registerFluids() {
        if (mElements.fluids != null) {
            mElements.fluids.values().forEach(fluid -> {
                if (!FluidRegistry.registerFluid(fluid)) {
                    mElements.container.warn("The name {} has been registered to another fluid!", fluid.getName());
                }
            });
        }

        if (mElements.fluidBuckets != null) {
            mElements.fluidBuckets.forEach(FluidRegistry::addBucketForFluid);
        }

       if (mElements.fluidBlocks != null) {
           mElements.fluidBlocks.forEach((fluid, fluidBlockFunction) ->
                   fluid.setBlock(fluidBlockFunction.apply(fluid)));
       }
    }

    private void registerNetwork() {
        if (mElements.networks != null && !mElements.networks.isEmpty()) {
            for (int i = 0; i < mElements.networks.size(); i++) {
                Triple<Class, Class, Side[]> triple = mElements.networks.get(i);
                for (Side side : triple.getRight()) {
                    //noinspection unchecked
                    mElements.channel.registerMessage(triple.getLeft(), triple.getMiddle(), i, side);
                }
            }
        }
    }

    private void registerOreNames() {
        if (mElements.blockOreDictionaries != null) {
            mElements.blockOreDictionaries.forEach((oreName, blocks) -> {
                for (Block block : blocks) {
                    OreDictionary.registerOre(oreName, block);
                }
            });
        }
        if (mElements.itemOreDictionaries != null) {
            mElements.itemOreDictionaries.forEach((oreName, items) -> {
                for (Item item : items) {
                    OreDictionary.registerOre(oreName, item);
                }
            });
        }
    }

    private void runCustomAnnotation(LoadState state) {
        final Map<Class<? extends Annotation>, BiConsumer<ASMDataTable.ASMData, ECModContainer>> consumerMap = mElements.customAnnotation.row(state);
        if (consumerMap != null && !consumerMap.isEmpty()) {
            consumerMap.forEach((aClass, asmDataConsumer) -> {
                final Set<ASMDataTable.ASMData> asmDataSet = mElements.asm.getAll(aClass.getName());
                if (asmDataSet != null) {
                    asmDataSet.forEach(set -> asmDataConsumer.accept(set, mElements.container));
                }
            });
        }
    }
}

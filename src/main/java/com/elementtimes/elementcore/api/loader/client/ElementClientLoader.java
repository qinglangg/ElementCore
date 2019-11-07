package com.elementtimes.elementcore.api.loader.client;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.LoaderHelper;
import com.elementtimes.elementcore.api.annotation.ModElement;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class ElementClientLoader {

    private boolean isLoaded = false;
    private ECModElements mElements;

    public ElementClientLoader(ECModElements elements) {
        mElements = elements;
    }

    public void load() {
        if (!isLoaded) {
            loadScreenFactories();
        }
        isLoaded = true;
    }

    private void loadScreenFactories() {
        LoaderHelper.stream(mElements, ModElement.class).forEach(data -> {
            LoaderHelper.loadClass(mElements, data.getClassType().getClassName()).ifPresent(clazz -> {
                loadScreenFactoryFromMethod(clazz);
                loadScreenFactoryFromField(clazz);
            });
        });
    }

    private void loadScreenFactoryFromMethod(Class clazz) {
        Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .filter(m -> ECUtils.reflect.checkMethodTypeAndParameters(m, Screen.class,
                        Container.class, PlayerInventory.class, ITextComponent.class))
                .peek(m -> ECUtils.reflect.setAccessible(m))
                .forEach(m -> {
                    ScreenManager.IScreenFactory factory = (container, inventory, text) -> {
                        try {
                            return (Screen) m.invoke(null, container, inventory, text);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            return null;
                        }
                    };
                    String methodName = m.getName();
                    Optional<ContainerType> typeOpt = ECUtils.reflect.getField(clazz, methodName, null, ContainerType.class, mElements.logger);
                    if (typeOpt.isPresent()) {
                        mElements.client().containers.screenFactories.put(typeOpt.get(), factory);
                    } else {
                        mElements.client().containers.screenFactories.put(methodName, factory);
                    }
                });
    }

    private void loadScreenFactoryFromField(Class clazz) {
        Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .filter(f -> ScreenManager.IScreenFactory.class.isAssignableFrom(f.getType()))
                .peek(f -> ECUtils.reflect.setAccessible(f))
                .forEach(f -> {
                    try {
                        ScreenManager.IScreenFactory factory = (ScreenManager.IScreenFactory) f.get(null);
                        mElements.client().containers.screenFactories.put(f.getName(), factory);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }
}

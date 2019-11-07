package com.elementtimes.elementcore.api.loader.client;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.LoaderHelper;
import com.elementtimes.elementcore.api.annotation.ModContainerType;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ContainerTypeClientLoader {

    private boolean isScreenFactoryLoaded = false;
    private ECModElements mElements;

    Map<Object, ScreenManager.IScreenFactory> screenFactories = new HashMap<>();

    public ContainerTypeClientLoader(ECModElements elements) {
        mElements = elements;
    }

    public Map<Object, ScreenManager.IScreenFactory> screenFactories() {
        if (!isScreenFactoryLoaded) {
            mElements.client().elements.load();
            loadScreenFactories();
        }
        return screenFactories;
    }

    private void loadScreenFactories() {
        loadScreenFactory();
        loadScreenCreator();
        isScreenFactoryLoaded = true;
    }

    private void loadScreenFactory() {
        LoaderHelper.stream(mElements, ModContainerType.ScreenFactory.class).forEach(data -> {
            LoaderHelper.loadClass(mElements, data.getClassType().getClassName()).ifPresent(clazz -> {
                String memberName = data.getMemberName();
                ECUtils.reflect.getField(clazz, memberName, null, ScreenManager.IScreenFactory.class, mElements.logger).ifPresent(factory -> {
                    String name = LoaderHelper.getDefault(data, memberName);
                    screenFactories.put(name, factory);
                });
            });
        });
    }

    private void loadScreenCreator() {
        LoaderHelper.stream(mElements, ModContainerType.ScreenCreator.class).forEach(data -> {
            LoaderHelper.loadClass(mElements, data.getClassType().getClassName()).ifPresent(clazz -> {
                String memberName = data.getMemberName();
                String methodName = memberName.substring(0, memberName.indexOf('('));
                // (Container, PlayerInventory, ITextComponent)Screen
                ScreenManager.IScreenFactory factory;
                try {
                    Method creator = clazz.getMethod(methodName, Container.class, PlayerInventory.class, ITextComponent.class);
                    if (Modifier.isStatic(creator.getModifiers()) && Screen.class.isAssignableFrom(creator.getReturnType())) {
                        ECUtils.reflect.setAccessible(creator);
                        factory = (container, inventory, text) -> {
                            try {
                                return (Screen) creator.invoke(container, inventory, text);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                                return null;
                            }
                        };
                    } else {
                        factory = null;
                    }
                } catch (NoSuchMethodException ignored) {
                    factory = null;
                }
                if (factory != null) {
                    try {
                        Field typeGetter = clazz.getDeclaredField(methodName);
                        if (Modifier.isStatic(typeGetter.getModifiers()) && ContainerType.class.isAssignableFrom(typeGetter.getType())) {
                            ECUtils.reflect.setAccessible(typeGetter);
                            ContainerType type = (ContainerType) typeGetter.get(null);
                            screenFactories.put(type, factory);
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        screenFactories.put(methodName, factory);
                    }
                }
            });
        });
    }
}

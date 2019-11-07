package com.elementtimes.elementcore.api.loader;

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ContainerTypeLoader {

    private boolean isContainerTypeLoaded = false;
    private ECModElements mElements;

    List<ContainerType> types = new ArrayList<>();

    public ContainerTypeLoader(ECModElements elements) {
        mElements = elements;
    }

    public List<ContainerType> types() {
        if (!isContainerTypeLoaded) {
            mElements.elements.load();
            loadContainerType();
        }
        return types;
    }

    private void loadContainerType() {
        LoaderHelper.stream(mElements, ModContainerType.class).forEach(data -> {
            LoaderHelper.loadClass(mElements, data.getClassType().getClassName()).ifPresent(clazz -> {
                ECUtils.reflect.getField(clazz, ContainerType.class, null, mElements.logger).ifPresent(type -> {
                    LoaderHelper.regName(mElements, type, LoaderHelper.getDefault(data, data.getMemberName()));
                    types.add(type);
                });
            });
        });
        loadContainerTypeMethod();
        isContainerTypeLoaded = true;
    }

    private void loadContainerTypeMethod() {
        LoaderHelper.stream(mElements, ModContainerType.ContainerFactory.class).forEach(data -> {
            LoaderHelper.loadClass(mElements, data.getClassType().getClassName()).ifPresent(clazz -> {
                String memberName = data.getMemberName();
                String methodName = memberName.substring(0, memberName.indexOf('('));
                Method method = null;
                try {
                    method = clazz.getDeclaredMethod(methodName, int.class, PlayerInventory.class);
                    if (!ContainerType.class.isAssignableFrom(method.getReturnType())
                            || !Modifier.isStatic(method.getModifiers())) {
                        method = null;
                    }
                } catch (NoSuchMethodException e) { }
                if (method == null) {
                    try {
                        method = clazz.getDeclaredMethod(methodName, Integer.class, PlayerInventory.class);
                        if (!ContainerType.class.isAssignableFrom(method.getReturnType())
                                || !Modifier.isStatic(method.getModifiers())) {
                            method = null;
                        }
                    } catch (NoSuchMethodException e) { }
                }
                if (method == null) {
                    try {
                        method = clazz.getMethod(methodName, int.class, PlayerInventory.class);
                        if (!ContainerType.class.isAssignableFrom(method.getReturnType())
                                || !Modifier.isStatic(method.getModifiers())) {
                            method = null;
                        }
                    } catch (NoSuchMethodException e) { }
                }
                if (method == null) {
                    try {
                        method = clazz.getMethod(methodName, Integer.class, PlayerInventory.class);
                        if (!ContainerType.class.isAssignableFrom(method.getReturnType())
                                || !Modifier.isStatic(method.getModifiers())) {
                            method = null;
                        }
                    } catch (NoSuchMethodException e) { }
                }
                if (method != null) {
                    if (!Modifier.isPublic(method.getModifiers())) {
                        method.setAccessible(true);
                    }
                    Method finalMethod = method;
                    types.add(new ContainerType<>((windowId, inventory) -> {
                        try {
                            return (Container) finalMethod.invoke(null, windowId, inventory);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }));
                }
            });
        });
    }
}
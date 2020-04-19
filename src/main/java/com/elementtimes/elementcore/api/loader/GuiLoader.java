package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.annotation.ModContainer;
import com.elementtimes.elementcore.api.annotation.part.Parts;
import com.elementtimes.elementcore.api.helper.FindOptions;
import com.elementtimes.elementcore.api.helper.ObjHelper;
import com.elementtimes.elementcore.api.misc.tool.ConstructorObj;
import com.elementtimes.elementcore.api.misc.tool.MethodObj;
import com.elementtimes.elementcore.api.misc.wrapper.AnnotationGetter;
import com.elementtimes.elementcore.api.misc.wrapper.AnnotationMethod;
import com.elementtimes.elementcore.api.misc.wrapper.ScreenWrapper;
import com.elementtimes.elementcore.api.utils.CommonUtils;
import com.elementtimes.elementcore.api.utils.ReflectUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.annotation.ElementType;

/**
 * @author luqin2007
 */
public class GuiLoader {

    public static void load(ECModElements elements) {
        loadContainerType(elements);
        if (CommonUtils.isClient()) {
            loadContainerScreen(elements);
        }
    }

    private static void loadContainerType(ECModElements elements) {
        ObjHelper.stream(elements, ModContainer.class).forEach(data -> {
            String name = ObjHelper.getDefault(data);
            switch (data.getTargetType()) {
                case METHOD:
                    ObjHelper.findClass(elements, data.getClassType()).ifPresent(aClass -> {
                        String method = ObjHelper.getMemberName(data);
                        MethodObj m = ReflectUtils.findMethod(aClass, null, method, int.class, PlayerInventory.class);
                        ContainerType<?> ct;
                        if (m.hasContent(Container.class)) {
                            ct = new ContainerType<>((IContainerFactory<?>) (id, inv, data1) -> m.<Container>get(id, inv, data1).orElseThrow(() -> new RuntimeException(String.format("[%s]Can't create Container from %s#%s", elements.container.id(), aClass.getName(), method))));
                        } else {
                            MethodObj m2 = ReflectUtils.findMethod(aClass, null, method, int.class, PlayerInventory.class, PacketBuffer.class);
                            ct = new ContainerType<>((id, inv) -> m2.<Container>get(id, inv).orElseThrow(() -> new RuntimeException(String.format("[%s]Can't create Container from %s#%s", elements.container.id(), aClass.getName(), method))));
                        }
                        ObjHelper.setRegisterName(ct, name, data, elements);
                        if (CommonUtils.isClient()) {
                            loadContainerScreen(elements, data, ct);
                        }
                        elements.containerTypes.add(ct);
                        elements.generatedContainerTypes.put(aClass, ct);
                    });
                    break;
                case FIELD:
                    FindOptions<ContainerType> option = new FindOptions<>(ContainerType.class, ElementType.FIELD);
                    ObjHelper.find(elements, data, option).ifPresent(type -> {
                        ObjHelper.setRegisterName(type, name, data, elements);
                        if (CommonUtils.isClient()) {
                            loadContainerScreen(elements, data, type);
                        }
                        elements.containerTypes.add(type);
                    });
                    break;
                case TYPE:
                    ObjHelper.findClass(elements, data.getClassType()).filter(Container.class::isAssignableFrom).ifPresent(aClass -> {
                        ConstructorObj c = ReflectUtils.findConstructor(aClass, Container.class, int.class, PlayerInventory.class, PacketBuffer.class);
                        ContainerType.IFactory<?> factory;
                        if (c.hasContent()) {
                            factory = (IContainerFactory<Container>) (id, inv, buffer) -> c.<Container>get(id, inv, buffer).orElseThrow(() -> new RuntimeException(String.format("[%s]Can't create Container from %s", elements.container.id(), c.getRefName())));
                        } else {
                            ConstructorObj c1 = ReflectUtils.findConstructor(aClass, Container.class, int.class, PlayerInventory.class);
                            if (c1.hasContent()) {
                                factory =  (id, inv) -> c1.<Container>get(id, inv).orElseThrow(() -> new RuntimeException(String.format("[%s]Can't create Container from %s", elements.container.id(), c.getRefName())));
                            } else {
                                elements.warn("[{}]Can't find constructor {}", elements.container.id(), aClass.getName());
                                return;
                            }
                        }
                        ContainerType<?> type = new ContainerType<>(factory);
                        ObjHelper.setRegisterName(type, name, data, elements);
                        if (CommonUtils.isClient()) {
                            loadContainerScreen(elements, data, type);
                        }
                        elements.containerTypes.add(type);
                        elements.generatedContainerTypes.put(aClass, type);
                    });
                    break;
                default:
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void loadContainerScreen(ECModElements elements, ModFileScanData.AnnotationData data, ContainerType<?> type) {
        AnnotationMethod screen = Parts.method(elements, data.getAnnotationData().get("screen"), Container.class, PlayerInventory.class, ITextComponent.class);
        if (screen.hasContent(net.minecraft.client.gui.screen.Screen.class)) {
            elements.containerScreens.add(new ScreenWrapper(type, (a, b, c) -> screen.get(a, b, c).orElseThrow(() -> new NullPointerException(String.format("[%s]Can't create screen for container %s(%s)", elements.container.id(), type.getRegistryName(), type.getClass().getName())))));
        } else {
            elements.warn("[{}]Can't find screen creator for {}", elements.container.id(), type.getRegistryName());
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void loadContainerScreen(ECModElements elements) {
        Class<?> screen = net.minecraft.client.gui.screen.Screen.class;
        ObjHelper.stream(elements, ModContainer.Screen.class).forEach(data -> {
            ObjHelper.findClass(elements, data.getClassType()).ifPresent(aClass -> {
                AnnotationGetter tg = Parts.getter(elements, ObjHelper.getDefault(data));
                if (!tg.hasContent(ContainerType.class)) {
                    elements.warn("[{}]Can't find ContainerType for {}", elements.container.id(), aClass.getName());
                    return;
                }
                String name = ObjHelper.getMemberName(data);
                ContainerType<?> type = tg
                        .<ContainerType<?>>get()
                        .orElseThrow(() -> new NullPointerException(String.format("[%s]Can't create type from %s for %s(%s).", elements.container.id(), tg.getRefName(), aClass.getName(), name)));
                Class<?> screenType = net.minecraft.client.gui.screen.Screen.class;
                Class<?>[] screenParameters = new Class[] {Container.class, PlayerInventory.class, ITextComponent.class};
                switch (data.getTargetType()) {
                    case METHOD:
                        MethodObj m = ReflectUtils.findMethod(aClass, null, name, screenParameters);
                        if (m.hasContent(screenType)) {
                            elements.containerScreens.add(new ScreenWrapper(type, (a, b, c) -> m.get(a, b, c).orElseThrow(() -> new RuntimeException(String.format("[%s]Can't create Screen from %s.", elements.container.id(), m.getRefName())))));
                        } else {
                            elements.warn("[{}]Can't find Screen creator method {}#{}", elements.container.id(), aClass.getName(), name);
                        }
                        break;
                    case TYPE:
                        elements.containerScreens.add(new ScreenWrapper(type, (a, b, c) -> ReflectUtils.findConstructor(aClass, screenType, screenParameters).get(a, b, c).orElseThrow(() -> new RuntimeException(String.format("[%s]Can't create Screen from %s.", elements.container.id(), aClass.getName())))));
                        break;
                    default:
                }
            });
        });
    }
}

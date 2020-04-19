package com.elementtimes.elementcore.api.misc.wrapper;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.interfaces.function.Function3;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.Supplier;

public class ScreenWrapper {

    private final Supplier<ContainerType<?>> mType;
    private final Function3<Container, PlayerInventory, ITextComponent, Object> mScreen;

    public ScreenWrapper(Supplier<ContainerType<?>> type, Function3<Container, PlayerInventory, ITextComponent, Object> screen) {
        mType = type;
        mScreen = screen;
    }

    public ScreenWrapper(ContainerType<?> type, Function3<Container, PlayerInventory, ITextComponent, Object> screen) {
        this(() -> type, screen);
    }

    @OnlyIn(Dist.CLIENT)
    public void apply(Logger logger) {
        ContainerType type = mType.get();
        logger.warn("    {}", type.getRegistryName());
        net.minecraft.client.gui.ScreenManager.registerFactory(type,
                (net.minecraft.client.gui.ScreenManager.IScreenFactory) (container, inventory, text) ->
                        (net.minecraft.client.gui.screen.Screen) mScreen.apply(container, inventory, text));
    }

    public Supplier<ContainerType<?>> getType() {
        return mType;
    }

    public static void registerAll(ECModElements elements) {
        List<ScreenWrapper> screens = elements.containerScreens;
        elements.warn("  Screens({})", screens.size());
        screens.forEach(e -> e.apply(elements));
    }
}

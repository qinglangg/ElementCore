package com.elementtimes.elementcore.api.misc.wrapper;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.misc.IAnnotationRef;
import net.minecraft.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author luqin2007
 */
public class BlockColorWrapper {

    private Supplier<Collection<Block>> mBlocks;
    private Collection<Block> mBlockCollection = null;
    private Object mColor;
    public String msg;

    public BlockColorWrapper(int color) {
        mColor = (net.minecraft.client.renderer.color.IBlockColor) (a, b, c, d) -> color;
        msg = "#" + Integer.toHexString(color);
    }

    public BlockColorWrapper(ECModElements elements, IAnnotationRef getter) {
        msg = getter.getRefName();
        if (!getter.hasContent()) {
            elements.warn("[{}]Color{} not found: {}", elements.container.id(),
                    getter.isField() ? "Field" : getter.isMethod() ? "Method" : "Class",
                    getter.getRefName());
        }
        if (getter instanceof AnnotationMethod) {
            mColor = (net.minecraft.client.renderer.color.IBlockColor) (a, b, c, d) -> {
                OptionalInt color = getter.getInt(a, b, c, d);
                if (!color.isPresent()) {
                    elements.warn("[{}]BlockColor: Can't get an Int from {}, use default value 0.", elements.container.id(), msg);
                }
                return color.orElse(0);
            };
        } else {
            Optional<Object> color = getter.get();
            if (!color.isPresent()) {
                elements.warn("[{}]BlockColor: Can't get an IBlockColor from {}, use default value 0.", elements.container.id(), msg);
            }
            mColor = color.orElse((net.minecraft.client.renderer.color.IBlockColor) (a, b, c, d) -> 0);
        }
    }

    public BlockColorWrapper bind(Supplier<Collection<Block>> blocks) {
        mBlocks = blocks;
        return this;
    }

    public BlockColorWrapper bind(Class<?> blocks) {
        return bind(() -> ForgeRegistries.BLOCKS.getValues().stream().filter(blocks::isInstance).collect(Collectors.toList()));
    }

    public BlockColorWrapper bind(Block block) {
        return bind(() -> Collections.singleton(block));
    }

    public Collection<Block> getBlocks() {
        if (mBlockCollection == null) {
            mBlockCollection = mBlocks.get();
        }
        return mBlockCollection;
    }

    @OnlyIn(Dist.CLIENT)
    public net.minecraft.client.renderer.color.IBlockColor getBlockColor() {
        return (net.minecraft.client.renderer.color.IBlockColor) mColor;
    }

    @OnlyIn(Dist.CLIENT)
    public void apply(Logger logger, net.minecraftforge.client.event.ColorHandlerEvent.Block event) {
        net.minecraft.client.renderer.color.IBlockColor color = getBlockColor();
        if (color != null) {
            for (Block block : getBlocks()) {
                logger.warn("  {} <-- {}", block.getRegistryName(), color);
                event.getBlockColors().register(color, block);
            }
        }
        logger.warn("  ==========");
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerAll(ECModElements elements, net.minecraftforge.client.event.ColorHandlerEvent.Block event) {
        List<BlockColorWrapper> colors = elements.blockColors;
        elements.warn("[{}]BlockColor({} groups)", elements.container.id(), colors.size());
        colors.forEach(c -> c.apply(elements, event));
    }
}

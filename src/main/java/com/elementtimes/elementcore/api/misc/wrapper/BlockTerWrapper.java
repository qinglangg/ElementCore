package com.elementtimes.elementcore.api.misc.wrapper;

import com.elementtimes.elementcore.api.ECModElements;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class BlockTerWrapper {

    public final Object ter;
    public final Class<? extends TileEntity> teClass;

    public BlockTerWrapper(Class<? extends TileEntity> teClass, Object ter) {
        this.ter = ter;
        this.teClass = teClass;
    }

    @OnlyIn(Dist.CLIENT)
    public net.minecraft.client.renderer.tileentity.TileEntityRenderer getTer() {
        return (net.minecraft.client.renderer.tileentity.TileEntityRenderer) ter;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isAnim() {
        return ter instanceof net.minecraftforge.client.model.animation.TileEntityRendererAnimation;
    }

    @OnlyIn(Dist.CLIENT)
    public void apply(Logger logger) {
        logger.warn("    {} <-- {}{}", teClass.getName(), isAnim() ? "[Anim]" : "", getTer());
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(teClass, getTer());
    }

    public static void registerAll(ECModElements elements) {
        List<BlockTerWrapper> ters = elements.ters;
        elements.warn("  Tile Entity Render({})", ters.size());
        ters.forEach(e -> e.apply(elements));
    }
}

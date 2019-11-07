package com.elementtimes.elementcore.api.loader.client;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.LoaderHelper;
import com.elementtimes.elementcore.api.annotation.ModBlock;
import com.elementtimes.elementcore.api.template.tileentity.BaseTsr;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.ITileTer;
import com.elementtimes.elementcore.other.ModTooltip;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.animation.TileEntityRendererAnimation;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.util.*;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class BlockClientLoader {

    private boolean isTerLoaded = false;
    private boolean isAnimTerLoaded = false;
    private boolean isInterfaceTerLoaded = false;
    private boolean isColorLoaded = false;
    private boolean isB3DLoaded = false;
    private boolean isOBJLoaded = false;
    private boolean isToolTipLoaded = false;
    private ECModElements mElements;

    private Map<Class, TileEntityRenderer> blockTer = new HashMap<>();
    private Map<IBlockColor, List<Block>> colors = new HashMap<>();
    private List<ModTooltip> tooltips = new ArrayList<>();
    private boolean b3d, obj;

    public BlockClientLoader(ECModElements elements) {
        mElements = elements;
    }

    public Map<Class, TileEntityRenderer> ter() {
        if (!isInterfaceTerLoaded) {
            loadInterfaceTer();
        }
        if (!isAnimTerLoaded) {
            loadAnimTer();
        }
        if (!isTerLoaded) {
            loadTer();
        }
        return blockTer;
    }

    private void loadInterfaceTer() {
        //noinspection unchecked
        mElements.data.getClasses().parallelStream()
                .map(data -> ECUtils.reflect.getField(ModFileScanData.ClassData.class, Set.class, data, mElements.logger))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(set -> (Set<Type>) set)
                .filter(set -> set.stream().anyMatch(typeObj -> ITileTer.class.getName().equals(typeObj.getClassName())))
                .map(data -> ECUtils.reflect.getField(ModFileScanData.ClassData.class, "clazz", data, Type.class, mElements.logger))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(type -> LoaderHelper.loadClass(mElements, type.getClassName()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(teClass -> blockTer.put(teClass, new BaseTsr()));
        isInterfaceTerLoaded = true;
    }

    private void loadTer() {
        LoaderHelper.stream(mElements, ModBlock.TER.class).forEach(data -> LoaderHelper.loadClass(mElements, data.getClassType().getClassName()).ifPresent(teClass -> {
            Type terType = LoaderHelper.getDefault(data);
            LoaderHelper.loadClass(mElements, terType.getClassName()).ifPresent(terClass -> {
                ECUtils.reflect.create(terClass, TileEntityRenderer.class, mElements.logger).ifPresent(ter -> blockTer.put(teClass, ter));
            });
        }));
        isTerLoaded = true;
    }

    private void loadAnimTer() {
        LoaderHelper.stream(mElements, ModBlock.AnimTER.class).forEach(data -> LoaderHelper.loadClass(mElements, data.getClassType().getClassName()).ifPresent(teClass -> {
            Type terType = LoaderHelper.getDefault(data);
            if (terType == null) {
                blockTer.put(teClass, new TileEntityRendererAnimation());
            } else {
                LoaderHelper.loadClass(mElements, terType.getClassName()).ifPresent(terClass ->
                        ECUtils.reflect.create(terClass, TileEntityRenderer.class, mElements.logger).ifPresent(ter -> blockTer.put(teClass, ter)));
            }
        }));
        isAnimTerLoaded = true;
    }

    public Map<IBlockColor, List<Block>> colors() {
        if (!isColorLoaded) {
            loadColor();
        }
        return colors;
    }

    private void loadColor() {
        Map<Class, IBlockColor> cacheColorMap = new HashMap<>(16);
        LoaderHelper.stream(mElements, ModBlock.BlockColor.class).forEach(data -> LoaderHelper.getBlock(mElements, data.getClassType().getClassName(), data.getMemberName()).ifPresent(block -> {
            Type renderType = LoaderHelper.getDefault(data);
            LoaderHelper.loadClass(mElements, renderType.getClassName()).ifPresent(renderClass -> {
                IBlockColor render = cacheColorMap.get(renderClass);
                if (render == null) {
                    Optional<IBlockColor> renderOpt = ECUtils.reflect.create(renderClass, IBlockColor.class, mElements.logger);
                    if (renderOpt.isPresent()) {
                        render = renderOpt.get();
                        cacheColorMap.put(renderClass, render);
                    }
                }
                if (render != null) {
                    colors.computeIfAbsent(render, (r) -> new ArrayList<>()).add(block);
                }
            });
        }));
        isColorLoaded = true;
    }

    public boolean useB3D() {
        if (!isB3DLoaded) {
            loadB3D();
        }
        return b3d;
    }

    private void loadB3D() {
        b3d = LoaderHelper.stream(mElements, ModBlock.B3D.class).count() > 0;
        isB3DLoaded = true;
    }

    public boolean useOBJ() {
        if (!isOBJLoaded) {
            loadOBJ();
        }
        return obj;
    }

    private void loadOBJ() {
        obj = LoaderHelper.stream(mElements, ModBlock.OBJ.class).count() > 0;
        isOBJLoaded = true;
    }

    public List<ModTooltip> tooltips() {
        if (!isToolTipLoaded) {
            loadTooltips();
        }
        return tooltips;
    }

    private void loadTooltips() {
        LoaderHelper.stream(mElements, ModBlock.Tooltips.class).forEach(data -> {
            LoaderHelper.getBlock(mElements, data.getClassType().getClassName(), data.getMemberName()).ifPresent(block -> {
                for (String tip : LoaderHelper.getDefault(data, Collections.<String>emptyList())) {
                    tooltips.add(new ModTooltip(block, tip));
                }
            });
        });
        isToolTipLoaded = true;
    }
}

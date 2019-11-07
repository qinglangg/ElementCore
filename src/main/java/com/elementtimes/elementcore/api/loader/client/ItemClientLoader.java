package com.elementtimes.elementcore.api.loader.client;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.LoaderHelper;
import com.elementtimes.elementcore.api.annotation.ModItem;
import com.elementtimes.elementcore.other.ModTooltip;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.objectweb.asm.Type;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ItemClientLoader {

    private boolean isItemColorLoaded = false;
    private boolean isItemTooltipLoaded = false;
    private ECModElements mElements;

    private Map<IItemColor, List<Item>> colors = new HashMap<>();
    private List<ModTooltip> tooltips = new ArrayList<>();

    public ItemClientLoader(ECModElements elements) {
        mElements = elements;
    }

    public Map<IItemColor, List<Item>> colors() {
        if (!isItemColorLoaded) {
            loadItemColors();
        }
        return colors;
    }

    private void loadItemColors() {
        Map<String, IItemColor> cacheColors = new HashMap<>(16);
        LoaderHelper.stream(mElements, ModItem.ItemColor.class).forEach(data -> {
            String className = data.getClassType().getClassName();
            LoaderHelper.getItem(mElements, className, data.getMemberName()).ifPresent(item -> {
                Type renderType = LoaderHelper.getDefault(data);
                LoaderHelper.loadClass(mElements, renderType.getClassName()).ifPresent(renderClass -> {
                    String name = renderClass.getName();
                    IItemColor render = cacheColors.get(name);
                    if (render == null) {
                        Optional<IItemColor> renderOpt = ECUtils.reflect.create(renderClass, IItemColor.class, mElements.logger);
                        if (renderOpt.isPresent()) {
                            render = renderOpt.get();
                            cacheColors.put(name, render);
                        }
                    }
                    if (render != null) {
                        colors.computeIfAbsent(render, (r) -> new ArrayList<>()).add(item);
                    }
                });
            });
        });
        isItemColorLoaded = true;
    }

    public List<ModTooltip> tooltips() {
        if (!isItemTooltipLoaded) {
            loadTooltips();
        }
        return tooltips;
    }

    private void loadTooltips() {
        LoaderHelper.stream(mElements, ModItem.Tooltips.class).forEach(data -> {
            LoaderHelper.getItem(mElements, data.getClassType().getClassName(), data.getMemberName()).ifPresent(item -> {
                for (String tip : LoaderHelper.getDefault(data, Collections.<String>emptyList())) {
                    tooltips.add(new ModTooltip(item, tip));
                }
            });
        });
        isItemTooltipLoaded = true;
    }
}

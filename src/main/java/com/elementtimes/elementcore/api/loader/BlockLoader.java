package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.annotation.ModBlock;
import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.LoaderHelper;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Block;
import net.minecraft.fluid.FlowingFluid;

import java.util.*;

public class BlockLoader {

    private boolean isBlockLoaded = false;
    private boolean isBurningTimeLoaded = false;
    private boolean isGroupLoaded = false;
    private boolean isTagLoaded = false;
    private ECModElements mElements;

    Map<String, Block> blocks = new HashMap<>();
    Object2IntMap<String> burningTimes = new Object2IntArrayMap<>();
    Map<String, String> groups = new HashMap<>();
    Map<String, List<Block>> tags = new HashMap<>();
    Map<String, List<Block>> tagItems = new HashMap<>();

    public BlockLoader(ECModElements elements) {
        mElements = elements;
    }

    public Map<String, Block> blocks() {
        if (!isBlockLoaded) {
            mElements.elements.load();
            loadBlocks();
        }
        return blocks;
    }

    private void loadBlocks() {
        LoaderHelper.stream(mElements, ModBlock.class).forEach(data -> {
            String className = data.getClassType().getClassName();
            LoaderHelper.loadClass(mElements, className).ifPresent(clazz -> {
                String memberName = data.getMemberName();
                ECUtils.reflect.getField(clazz, memberName, null, Block.class, mElements.logger).ifPresent(block -> {
                    LoaderHelper.regName(mElements, block, LoaderHelper.getDefault(data, memberName));
                    blocks.put(className + "." + memberName, block);
                });
            });
        });
        isBlockLoaded = true;
    }

    public Object2IntMap<String> burningTimes() {
        if (!isBurningTimeLoaded) {
            loadBurningTimes();
        }
        return burningTimes;
    }

    private void loadBurningTimes() {
        LoaderHelper.stream(mElements, ModBlock.BurningTime.class).forEach(data -> {
            LoaderHelper.getBlock(mElements, data.getClassType().getClassName(), data.getMemberName()).ifPresent(block -> {
                burningTimes.put(block.getRegistryName().toString(), LoaderHelper.getDefault(data));
            });
        });
        isBurningTimeLoaded = true;
    }

    public Map<String, String> groups() {
        if (!isGroupLoaded) {
            loadGroups();
        }
        return groups;
    }

    private void loadGroups() {
        LoaderHelper.stream(mElements, ModBlock.ItemGroup.class).forEach(data -> {
            LoaderHelper.getBlock(mElements, data.getClassType().getClassName(), data.getMemberName())
                    .ifPresent(block -> groups.put(block.getRegistryName().toString(), LoaderHelper.getDefault(data, "")));
        });
        isGroupLoaded = true;
    }

    public Map<String, List<Block>> tags() {
        if (!isTagLoaded) {
            loadTags();
        }
        return tags;
    }

    public Map<String, List<Block>> tagItems() {
        if (!isTagLoaded) {
            loadTags();
        }
        return tagItems;
    }

    private void loadTags() {
        LoaderHelper.stream(mElements, ModBlock.Tags.class).forEach(data -> {
            String memberName = data.getMemberName();
            LoaderHelper.getBlock(mElements, data.getClassType().getClassName(), memberName).ifPresent(block -> {
                List<String> tagText = LoaderHelper.getDefault(data, Collections.singletonList("forge:" + memberName));
                if ((boolean) data.getAnnotationData().getOrDefault("item", true)) {
                    for (String tag : tagText) {
                        String tagValue = tag.toLowerCase();
                        tags.computeIfAbsent(tagValue, (s) -> new ArrayList<>()).add(block);
                        tagItems.computeIfAbsent(tagValue, (s) -> new ArrayList<>()).add(block);
                    }
                } else {
                    for (String tag : tagText) {
                        tags.computeIfAbsent(tag.toLowerCase(), (s) -> new ArrayList<>()).add(block);
                    }
                }
            });
        });
        isTagLoaded = true;
    }
}

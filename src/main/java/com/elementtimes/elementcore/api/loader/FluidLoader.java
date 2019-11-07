package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.LoaderHelper;
import com.elementtimes.elementcore.api.annotation.ModFluid;
import com.elementtimes.elementcore.api.template.fluid.AbstractFluid;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;

import java.util.*;

public class FluidLoader {

    private boolean isFluidLoaded = false;
    private boolean isFluidTagLoaded = false;
    private boolean isFluidGroupLoaded = false;
    private boolean isFluidBurningTimeLoaded = false;
    private boolean isFluidBlockLoaded = false;
    private boolean isGasLoaded = false;
    private ECModElements mElements;

    Map<String, FlowingFluid> fluids = new HashMap<>();
    Map<String, List<FlowingFluid>> groups = new HashMap<>();
    Map<String, List<Fluid>> tags = new HashMap<>();
    Map<String, List<Fluid>> tagItems = new HashMap<>();
    Object2IntMap<FlowingFluid> burningTimes = new Object2IntArrayMap<>();
    Map<FlowingFluid, FlowingFluidBlock> blocks = new HashMap<>();
    List<FlowingFluid> gas = new ArrayList<>();

    public FluidLoader(ECModElements elements) {
        mElements = elements;
    }

    public Map<String, FlowingFluid> fluids() {
        if (!isFluidLoaded) {
            mElements.elements.load();
            loadFluids();
        }
        return fluids;
    }

    private void loadFluids() {
        LoaderHelper.stream(mElements, ModFluid.class).forEach(data -> {
            String className = data.getClassType().getClassName();
            LoaderHelper.loadClass(mElements, className).ifPresent(clazz -> {
                String memberName = data.getMemberName();
                ECUtils.reflect.getField(clazz, memberName, null, FlowingFluid.class, mElements.logger).ifPresent(fluid -> {
                    LoaderHelper.regName(mElements, fluid, LoaderHelper.getDefault(data, memberName));
                    fluids.put(className + "." + memberName, fluid);
                });
            });
        });
        isFluidLoaded = true;
    }

    public Map<String, List<FlowingFluid>> groups() {
        if (!isFluidGroupLoaded) {
            loadFluidGroups();
        }
        return groups;
    }

    private void loadFluidGroups() {
        if (!isFluidLoaded) {
            loadFluids();
        }
        LoaderHelper.stream(mElements, ModFluid.ItemGroup.class).forEach(data -> LoaderHelper.getFluid(mElements, data.getClassType().getClassName(), data.getMemberName()).ifPresent(fluid -> {
            groups.computeIfAbsent(LoaderHelper.getDefault(data), (s) -> new ArrayList<>()).add(fluid);
        }));
        isFluidLoaded = true;
    }

    public Object2IntMap<FlowingFluid> burningTimes() {
        if (!isFluidBurningTimeLoaded) {
            loadFluidBurningTimes();
        }
        return burningTimes;
    }

    private void loadFluidBurningTimes() {
        if (!isFluidLoaded) {
            loadFluids();
        }
        LoaderHelper.stream(mElements, ModFluid.BurningTime.class).forEach(data -> LoaderHelper.getFluid(mElements, data.getClassType().getClassName(), data.getMemberName()).ifPresent(fluid -> {
            burningTimes.put(fluid, (int) LoaderHelper.getDefault(data));
        }));
        isFluidBurningTimeLoaded = true;
    }

    public Map<String, List<Fluid>> tags() {
        if (!isFluidTagLoaded) {
            loadFluidTags();
        }
        return tags;
    }

    public Map<String, List<Fluid>> tagItems() {
        if (!isFluidTagLoaded) {
            loadFluidTags();
        }
        return tagItems;
    }

    private void loadFluidTags() {
        if (!isFluidLoaded) {
            loadFluids();
        }
        LoaderHelper.stream(mElements, ModFluid.Tags.class).forEach(data -> {
            String memberName = data.getMemberName();
            LoaderHelper.getFluid(mElements, data.getClassType().getClassName(), memberName).ifPresent(fluid -> {
                List<String> tagText = LoaderHelper.getDefault(data, Collections.singletonList("forge:" + memberName));
                if ((boolean) data.getAnnotationData().getOrDefault("item", true)) {
                    for (String tag : tagText) {
                        tags.computeIfAbsent(tag, (s) -> new ArrayList<>()).add(fluid);
                        tagItems.computeIfAbsent(tag, (s) -> new ArrayList<>()).add(fluid);
                    }
                } else {
                    for (String tag : tagText) {
                        tags.computeIfAbsent(tag, (s) -> new ArrayList<>()).add(fluid);
                    }
                }
            });
        });
        isFluidTagLoaded = true;
    }

    public Map<FlowingFluid, FlowingFluidBlock> blocks() {
        if (!isFluidBlockLoaded) {
            loadBlocks();
        }
        return blocks;
    }

    private void loadBlocks() {
        if (!isFluidLoaded) {
            loadFluids();
        }
        LoaderHelper.stream(mElements, ModFluid.Block.class).forEach(data -> {
            LoaderHelper.getFluid(mElements, data.getClassType().getClassName(), data.getMemberName()).ifPresent(fluid -> {
                LoaderHelper.loadClass(mElements, LoaderHelper.getDefault(data)).ifPresent(aClass -> {
                    ECUtils.reflect.create(aClass, new Object[]{fluid}, FlowingFluidBlock.class, mElements.logger).ifPresent(block -> {
                        blocks.put(fluid, block);
                        if (fluid instanceof AbstractFluid) {
                            ((AbstractFluid) fluid).setFluidBlock(block);
                        }
                    });
                });
            });
        });
        isFluidBlockLoaded = true;
    }

    public List<FlowingFluid> gas() {
        if (!isGasLoaded) {
            loadGas();
        }
        return gas;
    }

    private void loadGas() {
        if (!isFluidLoaded) {
            loadFluids();
        }
        LoaderHelper.stream(mElements, ModFluid.Gas.class).forEach(data -> {
            LoaderHelper.getFluid(mElements, data.getClassType().getClassName(), data.getMemberName()).ifPresent(fluid -> {
                gas.add(fluid);
            });
        });
        isFluidLoaded = true;
    }
}

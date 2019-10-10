package com.elementtimes.elementcore.api.common;

import com.elementtimes.elementcore.api.annotation.*;
import com.elementtimes.elementcore.api.annotation.enums.FluidBlockType;
import com.elementtimes.elementcore.api.annotation.enums.GenType;
import com.elementtimes.elementcore.api.template.SimpleOreGenerator;
import com.elementtimes.elementcore.other.CapabilityObject;
import com.elementtimes.elementcore.other.ModTooltip;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.potion.Potion;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Triple;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 服务端资源加载
 * @author luqin2007
 */
@SuppressWarnings("unchecked")
public class CommonLoader {

    public static void load(ECModElements elements) {
        elements.container.warn("common load start");
        // capability
        loadCapability(elements);

        // creativeTabs
        loadTab(elements);
        // block
        loadBlock(elements);
        loadBlockHarvestLevel(elements);
        loadBlockOre(elements);
        loadBlockTileEntity(elements);
        loadBlockState(elements);
        loadBlockWorldGenerator(elements);
        loadBlockBurningTime(elements);
        // item
        loadItem(elements);
        loadItemRetain(elements);
        loadItemOre(elements);
        loadItemSub(elements);
        loadItemDamageable(elements);
        // tooltip
        loadTooltip(elements);
        // fluid
        loadFluid(elements);
        loadFluidBlock(elements);
        // recipe
        loadRecipe(elements);
        // network
        loadNetwork(elements);
        // enchantment
        loadEnchantment(elements);
        // element
        loadStaticFunction(elements);
        // potion
        loadPotion(elements);
        elements.container.warn("common load finished");
    }

    private static void loadTab(ECModElements elements) {
        Set<ASMDataTable.ASMData> creativeTabsDataSet = elements.asm.getAll(ModCreativeTabs.class.getName());
        elements.tabs = LoaderHelper.createMap(creativeTabsDataSet);
        if (creativeTabsDataSet != null) {
            for (ASMDataTable.ASMData creativeTabsData : creativeTabsDataSet) {
                String key = (String) creativeTabsData.getAnnotationInfo().get("value");
                if (key == null || key.isEmpty()) {
                    key = creativeTabsData.getObjectName().toLowerCase();
                }
                Optional<Class> classOptional = LoaderHelper.getOrLoadClass(elements, creativeTabsData.getClassName());
                if (classOptional.isPresent()) {
                    Optional<CreativeTabs> tabOpt = ECUtils.reflect.getField(classOptional.get(), creativeTabsData.getObjectName(), null, CreativeTabs.class, elements.container.logger);
                    if (tabOpt.isPresent()) {
                        elements.tabs.put(key, tabOpt.get());
                    }
                }
            }
        }
        elements.container.warn("loadTab: {}", elements.tabs.size());
    }

    private static void loadBlock(ECModElements elements) {
        Set<ASMDataTable.ASMData> blockAsmDataSet = elements.asm.getAll(ModBlock.class.getName());
        elements.blocks = LoaderHelper.createMap(blockAsmDataSet);
        if (blockAsmDataSet != null) {
            for (ASMDataTable.ASMData blockData : blockAsmDataSet) {
                Optional<Class> classOpt = LoaderHelper.getOrLoadClass(elements, blockData.getClassName());
                if (classOpt.isPresent()) {
                    // block
                    Class clazz = classOpt.get();
                    String name = blockData.getObjectName();
                    Block block;
                    Optional<Block> optional;
                    if (name == null || name.isEmpty()) {
                        optional = ECUtils.reflect.create(clazz, Block.class, elements.container.logger);
                    } else {
                        optional = ECUtils.reflect.getField(clazz, name, null, Block.class, elements.container.logger);
                    }
                    block = optional.orElseGet(() -> new Block(Material.ROCK));
                    elements.blocks.put(blockData.getClassName() + (name == null ? "" : name), block);
                    // ModBlock
                    Map<String, Object> blockInfo = blockData.getAnnotationInfo();
                    // registryName
                    String defaultName;
                    if (name != null && !name.isEmpty()) {
                        defaultName = name.toLowerCase();
                    } else {
                        defaultName = clazz.getSimpleName().toLowerCase();
                    }
                    if (block.getRegistryName() == null) {
                        String registryName = (String) blockInfo.get("registerName");
                        if (registryName == null || registryName.isEmpty()) {
                            registryName = defaultName;
                        }
                        if (registryName.contains(":")) {
                            block.setRegistryName(registryName);
                        } else {
                            block.setRegistryName(elements.container.id(), registryName);
                        }
                    }
                    // unlocalizedName
                    if ("tile.null".equals(block.getUnlocalizedName())) {
                        String unlocalizedName = (String) blockInfo.get("unlocalizedName");
                        if (unlocalizedName != null && !unlocalizedName.isEmpty()) {
                            block.setUnlocalizedName(elements.container.id() + "." + unlocalizedName);
                        } else {
                            ResourceLocation registryName = block.getRegistryName();
                            block.setUnlocalizedName(registryName.getResourceDomain() + "." + registryName.getResourcePath());
                        }
                    }
                    // creativeTabs
                    CreativeTabs tab = LoaderHelper.getTab((String) blockInfo.get("creativeTabKey"), elements);
                    if (tab != null) {
                        block.setCreativeTab(tab);
                    }
                }
            }
        }
        elements.container.warn("loadBlock - block: {}", elements.blocks.size());
    }

    private static void loadBlockHarvestLevel(ECModElements initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModBlock.HarvestLevel.class.getName());
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData data : asmDataSet) {
                LoaderHelper.getBlock(initializer, data.getClassName(), data.getObjectName()).ifPresent(block -> {
                    Map<String, Object> info = data.getAnnotationInfo();
                    String toolClass = (String) info.getOrDefault("toolClass", "pickaxe");
                    int level = (int) info.getOrDefault("level", 2);
                    block.setHarvestLevel(toolClass, level);
                });
            }
        }
    }

    private static void loadBlockOre(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModOreDict.class.getName());
        elements.blockOreDictionaries = LoaderHelper.createMap(asmDataSet);
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData data : asmDataSet) {
                LoaderHelper.getBlock(elements, data.getClassName(), data.getObjectName()).ifPresent(block -> {
                    List<String> oreNames = (List<String>) data.getAnnotationInfo().get("value");
                    if (oreNames != null) {
                        for (String oreName : oreNames) {
                            List<Block> ores = elements.blockOreDictionaries.getOrDefault(oreName, new ArrayList<>());
                            ores.add(block);
                            elements.blockOreDictionaries.put(oreName, ores);
                        }
                    }
                });
            }
        }
        elements.container.warn("loadBlockOre: {}", elements.blockOreDictionaries.size());
    }

    private static void loadBlockTileEntity(ECModElements elements) {
        Set<ASMDataTable.ASMData> dataSet = elements.asm.getAll(ModBlock.TileEntity.class.getName());
        elements.blockTileEntities = LoaderHelper.createMap(dataSet);
        if (dataSet != null) {
            for (ASMDataTable.ASMData data : dataSet) {
                LoaderHelper.getBlock(elements, data.getClassName(), data.getObjectName()).ifPresent(block -> {
                    Map<String, Object> teInfo = data.getAnnotationInfo();
                    String name = (String) teInfo.getOrDefault("name", Objects.requireNonNull(block.getRegistryName()).getResourcePath());
                    //noinspection unchecked
                    LoaderHelper.getOrLoadClass(elements, (String) teInfo.get("clazz")).ifPresent(clazz ->
                            elements.blockTileEntities.put(block, ImmutablePair.of(name, clazz)));
                });
            }
        }
        elements.container.warn("loadBlockTileEntity: {}", elements.blockTileEntities.size());
    }

    private static void loadBlockState(ECModElements initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModBlock.StateMap.class.getName());
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                LoaderHelper.getBlock(initializer, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                    Map<String, Object> info = asmData.getAnnotationInfo();
                    boolean useB3d = (boolean) info.getOrDefault("useB3D", false);
                    boolean useObj = (boolean) info.getOrDefault("useOBJ", false);
                    initializer.blockObj |= useObj;
                    initializer.blockB3d |= useB3d;
                });
            }
        }
        initializer.container.warn("loadBlockState: OBJ={}, B3D={}", initializer.blockObj, initializer.blockB3d);
    }

    private static void loadBlockWorldGenerator(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmWorldGenDataSet = elements.asm.getAll(ModBlock.WorldGen.class.getName());
        Set<ASMDataTable.ASMData> asmWorldGenCustomDataSet = elements.asm.getAll(ModBlock.WorldGenClass.class.getName());
        elements.blockWorldGen = LoaderHelper.createMap(asmWorldGenCustomDataSet, asmWorldGenDataSet);
        if (asmWorldGenDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmWorldGenDataSet) {
                LoaderHelper.getBlock(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                    Map<String, Object> info = asmData.getAnnotationInfo();
                    int yRange = (int) info.getOrDefault("YRange", 48);
                    int yMin = (int) info.getOrDefault("YMin", 16);
                    int count = (int) info.getOrDefault("count", 8);
                    int times = (int) info.getOrDefault("times", 6);
                    float probability = (float) info.getOrDefault("probability", 0.6f);
                    int[] dimBlackList = (int[]) info.getOrDefault("dimBlackList", new int[0]);
                    int[] dimWhiteList = (int[]) info.getOrDefault("dimWhiteList", new int[0]);
                    GenType type = (GenType) info.getOrDefault("type", GenType.Ore);
                    List<WorldGenerator> worldGeneratorList = elements.blockWorldGen.getOrDefault(type, new ArrayList<>());
                    worldGeneratorList.add(new SimpleOreGenerator(yRange, yMin, count, times, probability, dimBlackList, dimWhiteList, block.getDefaultState()));
                    elements.blockWorldGen.put(type, worldGeneratorList);
                });
            }
        }
        if (asmWorldGenCustomDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmWorldGenCustomDataSet) {
                LoaderHelper.getBlock(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                    String className = (String) asmData.getAnnotationInfo().get("value");
                    ModAnnotation.EnumHolder typeValue = (ModAnnotation.EnumHolder) asmData.getAnnotationInfo().get("type");
                    GenType type;
                    if (typeValue == null) {
                        type = GenType.Ore;
                    } else {
                        type = GenType.valueOf(typeValue.getValue());
                    }
                    ECUtils.reflect.create(className, new Object[] {block}, WorldGenerator.class, elements.container.logger)
                            .ifPresent(o -> {
                                List<WorldGenerator> worldGeneratorList = elements.blockWorldGen.getOrDefault(type, new ArrayList<>());
                                worldGeneratorList.add(o);
                                elements.blockWorldGen.put(type, worldGeneratorList);
                            });
                });
            }
        }
        elements.container.warn("loadWorldGenerator: {}", elements.blockWorldGen.size());
    }

    private static void loadBlockBurningTime(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModBlock.BurningTime.class.getName());
        elements.blockBurningTimes = new Object2IntArrayMap<>(asmDataSet == null ? 0 : asmDataSet.size());
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                LoaderHelper.getBlock(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                    int burningTime = (int) asmData.getAnnotationInfo().getOrDefault("burningTime", -1);
                    if (burningTime > 0) {
                        elements.blockBurningTimes.put(block, burningTime);
                    }
                });
            }
        }
        elements.container.warn("loadBlock - burningTime: {}", elements.blockBurningTimes.size());
    }

    private static void loadItem(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModItem.class.getName());
        elements.items = LoaderHelper.createMap(asmDataSet);
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                Optional<Class> classOpt = LoaderHelper.getOrLoadClass(elements, asmData.getClassName());
                if (classOpt.isPresent()) {
                    // Item
                    Class clazz = classOpt.get();
                    String name = asmData.getObjectName();
                    Item item;
                    Optional<Item> optional;
                    if (name == null || name.isEmpty()) {
                        optional = ECUtils.reflect.create(clazz, Item.class, elements.container.logger);
                    } else {
                        optional = ECUtils.reflect.getField(clazz, name, null, Item.class, elements.container.logger);
                    }
                    item = optional.orElseGet(Item::new);
                    elements.items.put(asmData.getClassName() + (name == null ? "" : name), item);
                    // registryName
                    String defaultName;
                    if (name != null && !name.isEmpty()) {
                        defaultName = name.toLowerCase();
                    } else {
                        defaultName = clazz.getSimpleName().toLowerCase();
                    }
                    if (item.getRegistryName() == null) {
                        String registryName = (String) asmData.getAnnotationInfo().get("registerName");
                        if (registryName == null || registryName.isEmpty()) {
                            registryName = defaultName;
                        }
                        if (registryName.contains(":")) {
                            item.setRegistryName(registryName);
                        } else {
                            item.setRegistryName(elements.container.id(), registryName);
                        }
                    }
                    // unlocalizedName
                    if ("item.null".equals(item.getUnlocalizedName())) {
                        String unlocalizedName = (String) asmData.getAnnotationInfo().get("unlocalizedName");
                        if (unlocalizedName != null && !unlocalizedName.isEmpty()) {
                            item.setUnlocalizedName(elements.container.id() + "." + unlocalizedName);
                        } else {
                            ResourceLocation registryName = item.getRegistryName();
                            item.setUnlocalizedName(registryName.getResourceDomain() + "." + registryName.getResourcePath());
                        }
                    }
                    // creativeTabs
                    CreativeTabs tab = LoaderHelper.getTab((String) asmData.getAnnotationInfo().get("creativeTabKey"), elements);
                    if (tab != null) {
                        item.setCreativeTab(tab);
                    }
                }
            }
        }
        elements.container.warn("loadItem: {}", elements.items.size());
    }

    private static void loadItemRetain(ECModElements initializer) {
        final Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModItem.RetainInCrafting.class.getName());
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                LoaderHelper.getItem(initializer, asmData.getClassName(), asmData.getObjectName()).ifPresent(item -> item.setContainerItem(item));
            }
        }
    }

    private static void loadItemOre(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModOreDict.class.getName());
        elements.itemOreDictionaries = LoaderHelper.createMap(asmDataSet);
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                LoaderHelper.getItem(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(item -> {
                    List<String> oreNames = (List<String>) asmData.getAnnotationInfo().get("value");
                    for (String oreName : oreNames) {
                        List<Item> ores = elements.itemOreDictionaries.getOrDefault(oreName, new ArrayList<>());
                        ores.add(item);
                        elements.itemOreDictionaries.put(oreName, ores);
                    }
                });
            }
        }
        elements.container.warn("loadItemOre: {}", elements.itemOreDictionaries.size());
    }

    private static void loadItemSub(ECModElements initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModItem.HasSubItem.class.getName());
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                LoaderHelper.getItem(initializer, asmData.getClassName(), asmData.getObjectName()).ifPresent(item -> {
                    item.setHasSubtypes(true);
                    item.setMaxDamage(0);
                    item.setNoRepair();
                });
            }
        }
    }

    private static void loadItemDamageable(ECModElements initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModItem.Damageable.class.getName());
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                LoaderHelper.getItem(initializer, asmData.getClassName(), asmData.getObjectName()).ifPresent(item -> {
                    int damage = (int) asmData.getAnnotationInfo().getOrDefault("value", 0);
                    boolean noRepair = (boolean) asmData.getAnnotationInfo().getOrDefault("noRepair", false);

                    item.setMaxDamage(damage);
                    item.setMaxStackSize(1);
                    if (noRepair) {
                        item.setNoRepair();
                    }
                });
            }
        }
    }

    private static void loadTooltip(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmBlockTooltipSet = elements.asm.getAll(ModBlock.Tooltip.class.getName());
        Set<ASMDataTable.ASMData> asmToolTooltipSet = elements.asm.getAll(ModItem.Tooltip.class.getName());
        elements.toolTips = new ArrayList<>((asmBlockTooltipSet == null ? 0 : asmBlockTooltipSet.size())
                + (asmToolTooltipSet == null ? 0 : asmToolTooltipSet.size()));
        if (asmBlockTooltipSet != null) {
            for (ASMDataTable.ASMData asmData : asmBlockTooltipSet) {
                LoaderHelper.getBlock(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                    for (String tooltip : (List<String>) asmData.getAnnotationInfo().get("value")) {
                        elements.toolTips.add(new ModTooltip(block, tooltip));
                    }
                });
            }
        }
        if (asmToolTooltipSet != null) {
            for (ASMDataTable.ASMData asmData : asmToolTooltipSet) {
                LoaderHelper.getItem(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(item -> {
                    for (String tooltip : (List<String>) asmData.getAnnotationInfo().get("value")) {
                        elements.toolTips.add(new ModTooltip(item, tooltip));
                    }
                });
            }
        }
        elements.container.warn("loadTooltips: {}", elements.toolTips.size());
    }

    private static void loadFluid(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModFluid.class.getName());
        elements.fluids = LoaderHelper.createMap(asmDataSet);
        elements.fluidBurningTimes = new Object2IntArrayMap<>(asmDataSet == null ? 0 : asmDataSet.size());
        elements.fluidResources = new ArrayList<>(asmDataSet == null ? 0 : asmDataSet.size());
        elements.fluidTabs = LoaderHelper.createMap(asmDataSet);
        elements.fluidBuckets = new ArrayList<>(asmDataSet == null ? 0 : asmDataSet.size());
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                Optional<Class> classOpt = LoaderHelper.getOrLoadClass(elements, asmData.getClassName());
                if (classOpt.isPresent()) {
                    // fluid
                    String name = asmData.getObjectName();
                    Class clazz = classOpt.get();
                    String defaultName;
                    if (name != null && !name.isEmpty()) {
                        defaultName = name.toLowerCase();
                    } else {
                        defaultName = clazz.getSimpleName().toLowerCase();
                    }
                    Fluid fluid;
                    Optional<Fluid> optional;
                    Map<String, Object> info = asmData.getAnnotationInfo();
                    if (name == null || name.isEmpty()) {
                        optional = ECUtils.reflect.create(clazz, Fluid.class, elements.container.logger);
                    } else {
                        optional = ECUtils.reflect.getField(clazz, name, null, Fluid.class, elements.container.logger);
                    }
                    fluid = optional.orElseGet(() -> {
                        String n = (String) info.getOrDefault("name", defaultName);
                        String stillResource = (String) info.get("stillResource");
                        String flowingResource = (String) info.get("flowingResource");
                        int color = (int) info.getOrDefault("color", 0xFFFFFFFF);
                        return new Fluid(n, new ResourceLocation(elements.container.id(), stillResource), new ResourceLocation(elements.container.id(), flowingResource), color);
                    });
                    elements.fluids.put(asmData.getClassName() + (name == null ? "" : name), fluid);
                    // bucket
                    if ((boolean) info.getOrDefault("bucket", true)) {
                        elements.fluidBuckets.add(fluid);
                    }
                    // density & gaseous
                    int density = (int) info.getOrDefault("density", 1000);
                    fluid.setDensity(density);
                    fluid.setGaseous(density <= 0);
                    // creativeTabs
                    CreativeTabs tab = LoaderHelper.getTab((String) info.get("creativeTabKey"), elements);
                    if (tab != null) {
                        elements.fluidTabs.put(fluid, tab);
                    }
                    // burningTime
                    int burningTime = (int) info.getOrDefault("burningTime", -1);
                    if (burningTime > 0) {
                        elements.fluidBurningTimes.put(fluid.getName(), burningTime);
                    }
                    // texture
                    boolean loadTexture = (boolean) info.getOrDefault("loadTexture", true);
                    if (loadTexture) {
                        elements.fluidResources.add(fluid);
                    }
                }
            }
        }
        elements.container.warn("loadFluid - fluids: {}", elements.fluids.size());
        elements.container.warn("loadFluid - fluidBuckets: {}", elements.fluidBuckets.size());
        elements.container.warn("loadFluid - fluidTabs: {}", elements.fluidTabs.size());
        elements.container.warn("loadFluid - fluidBurningTimes: {}", elements.fluidBurningTimes.size());
        elements.container.warn("loadFluid - fluidResources: {}", elements.fluidResources.size());
    }

    private static void loadFluidBlock(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModFluid.FluidBlock.class.getName());
        elements.fluidBlocks = LoaderHelper.createMap(asmDataSet);
        elements.fluidBlockStates = LoaderHelper.createMap(asmDataSet);
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                LoaderHelper.getFluid(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(fluid -> {
                    int density = fluid.getDensity();
                    Function<Fluid, Block> fluidBlock = null;
                    String className = (String) asmData.getAnnotationInfo().get("className");
                    String registerName = (String) asmData.getAnnotationInfo().get("registerName");
                    String unlocalizedName = (String) asmData.getAnnotationInfo().get("unlocalizedName");
                    String creativeTabKey = (String) asmData.getAnnotationInfo().get("creativeTabKey");
                    if (className != null && !className.isEmpty()) {
                        fluidBlock = (f) -> {
                            Optional<Block> blockOpt = ECUtils.reflect.create(className, new Object[]{f}, Block.class, elements.container.logger);
                            if (blockOpt.isPresent()) {
                                Block block = blockOpt.get();
                                LoaderHelper.decorateFluidBlock(block, registerName, creativeTabKey, unlocalizedName, f.getName(), density, elements);
                                return block;
                            } else {
                                ModAnnotation.EnumHolder holder = (ModAnnotation.EnumHolder) asmData.getAnnotationInfo().get("type");
                                FluidBlockType type;
                                if (holder == null) {
                                    type = FluidBlockType.Classic;
                                } else {
                                    type = FluidBlockType.valueOf(holder.getValue());
                                }
                                Block block = type.create(f);
                                LoaderHelper.decorateFluidBlock(block, registerName, creativeTabKey, unlocalizedName, fluid.getName(), density, elements);
                                return block;
                            }
                        };
                    }

                    if (fluidBlock == null) {
                        fluidBlock = f -> {
                            ModAnnotation.EnumHolder holder = (ModAnnotation.EnumHolder) asmData.getAnnotationInfo().get("type");
                            FluidBlockType type;
                            if (holder == null) {
                                type = FluidBlockType.Classic;
                            } else {
                                type = FluidBlockType.valueOf(holder.getValue());
                            }
                            Block block = type.create(f);
                            LoaderHelper.decorateFluidBlock(block, registerName, creativeTabKey, unlocalizedName, fluid.getName(), density, elements);
                            return block;
                        };
                    }

                    String resource = (String) asmData.getAnnotationInfo().getOrDefault("resource", "fluids");
                    if (resource != null && !resource.isEmpty()) {
                        elements.fluidBlockStates.put(fluid, resource);
                    }

                    elements.fluidBlocks.put(fluid, fluidBlock);
                });
            }
        }
        elements.container.warn("loadFluidBlock - fluids: {}", elements.fluidBlocks.size());
        elements.container.warn("loadFluidBlockState - fluids: {}", elements.fluidBlockStates.size());
    }

    private static void loadRecipe(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModRecipe.class.getName());
        elements.recipes = new ArrayList<>(asmDataSet == null ? 0 : asmDataSet.size());
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                LoaderHelper.getOrLoadClass(elements, asmData.getClassName()).ifPresent(clazz -> {
                    String name = asmData.getObjectName();
                    final Optional<Object> objOpt = ECUtils.reflect.getFromFieldOrMethod(clazz, name);
                    if (objOpt.isPresent()) {
                        Object obj = objOpt.get();
                        Map<String, Object> info = asmData.getAnnotationInfo();
                        String value = (String) info.getOrDefault("value", name);
                        boolean shaped = (boolean) info.getOrDefault("shaped", true);
                        boolean ore = (boolean) info.getOrDefault("ore", true);
                        int width = (int) info.getOrDefault("width", 3);
                        int height = (int) info.getOrDefault("height", 3);
                        final String rName = value.isEmpty() ? name : value;
                        if (obj instanceof Supplier) {
                            final Supplier o = (Supplier) obj;
                            elements.recipes.add(() -> {
                                Object o2 = o.get();
                                if (o2 instanceof IRecipe) {
                                    IRecipe recipe = (IRecipe) o2;
                                    if (recipe.getRegistryName() == null) {
                                        recipe.setRegistryName(new ResourceLocation(elements.container.id(), rName));
                                    }
                                    return new IRecipe[]{recipe};
                                } else if (o2 instanceof IRecipe[]) {
                                    return (IRecipe[]) o2;
                                } else if (o2 instanceof Collection) {
                                    ArrayList<IRecipe> recipes = new ArrayList<>(((Collection) o2).size());
                                    for (Object o1 : ((Collection) o2)) {
                                        if (o1 instanceof IRecipe) {
                                            recipes.add((IRecipe) o1);
                                        }
                                    }
                                    return recipes.toArray(new IRecipe[0]);
                                } else {
                                    elements.container.warn("You annotated a Supplier but it can't provide a Recipe.");
                                    return null;
                                }
                            });
                        } else if (obj instanceof IRecipe) {
                            final IRecipe recipe = (IRecipe) obj;
                            if (recipe.getRegistryName() == null) {
                                recipe.setRegistryName(new ResourceLocation(elements.container.id(), rName));
                            }
                            elements.recipes.add(() -> new IRecipe[]{recipe});
                        } else if (obj instanceof Object[] || obj instanceof Collection) {
                            final Object[] objects;
                            if (obj instanceof Collection) {
                                objects = ((Collection) obj).toArray();
                            } else {
                                objects = (Object[]) obj;
                            }
                            if (objects.length > 0) {
                                Object obj0 = objects[0];
                                if (obj0 instanceof IRecipe || obj0 instanceof Supplier) {
                                    // 合成表数组
                                    elements.recipes.add(() -> {
                                        IRecipe[] recipes = new IRecipe[objects.length];
                                        for (int i = 0; i < objects.length; i++) {
                                            Object object = objects[i];
                                            if (object instanceof IRecipe) {
                                                recipes[i] = (IRecipe) object;
                                            } else if (object instanceof Supplier) {
                                                Object o = ((Supplier) object).get();
                                                if (o instanceof IRecipe) {
                                                    recipes[i] = (IRecipe) o;
                                                } else {
                                                    elements.container.warn("You put a Supplier into an array or collection, but it can't provider a Recipe.");
                                                }
                                            } else {
                                                elements.container.warn("You want put other element in a Recipe/Supplier array or collection.");
                                            }
                                        }
                                        return recipes;
                                    });
                                } else {
                                    // 单一合成表
                                    elements.recipes.add(() -> {
                                        ItemStack r;
                                        if (obj0 instanceof Item) {
                                            r = new ItemStack((Item) obj0);
                                        } else if (obj0 instanceof Block) {
                                            r = new ItemStack((Block) obj0);
                                        } else if (obj0 instanceof ItemStack) {
                                            r = (ItemStack) obj0;
                                        } else if (obj0 instanceof Ingredient) {
                                            r = ((Ingredient) obj0).getMatchingStacks()[0];
                                        } else if (obj0 instanceof String && ((String) obj0).contains(":")) {
                                            Item resultItem = Item.getByNameOrId((String) obj0);
                                            if (resultItem == null || resultItem == Items.AIR) {
                                                elements.container.warn("The recipe {} return an NULL item.", rName);
                                                return null;
                                            }
                                            r = new ItemStack(resultItem);
                                        } else {
                                            r = CraftingHelper.getIngredient(obj0).getMatchingStacks()[0];
                                        }
                                        IRecipe recipe;
                                        CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
                                        int size = width * height;
                                        primer.input = NonNullList.withSize(size, Ingredient.EMPTY);
                                        primer.width = width;
                                        primer.height = height;
                                        if (size < objects.length - 1) {
                                            elements.container.warn("You want to register a recipe({}) with {} items, but you put {} items in it. Some items will ignore.",
                                                    rName, size, objects.length - 1);
                                        }
                                        for (int i = 1; i < objects.length; i++) {
                                            Object o = objects[i];
                                            if (i - 1 >= size) {
                                                elements.container.warn("Ignore item[()]: {}.", i - 1, o);
                                                break;
                                            }
                                            if (o instanceof String && ((String) o).contains(":")) {
                                                Item item = Item.getByNameOrId((String) o);
                                                if (item == null || item == Items.AIR) {
                                                    elements.container.warn("The recipe {} have an NULL item.", rName);
                                                    return null;
                                                }
                                                o = new ItemStack(item);
                                            }
                                            primer.input.set(i - 1, CraftingHelper.getIngredient(o == null ? ItemStack.EMPTY : o));
                                        }
                                        if (shaped) {
                                            if (ore) {
                                                recipe = new ShapedOreRecipe(new ResourceLocation(elements.container.id(), "recipe"), r, primer);
                                            } else {
                                                recipe = new ShapedRecipes("recipe", primer.width, primer.height, primer.input, r);
                                            }
                                        } else {
                                            if (ore) {
                                                recipe = new ShapelessOreRecipe(new ResourceLocation(elements.container.id(), "recipe"), primer.input, r);
                                            } else {
                                                recipe = new ShapelessRecipes("recipe", r, primer.input);
                                            }
                                        }
                                        recipe.setRegistryName(new ResourceLocation(elements.container.id(), rName));
                                        return new IRecipe[]{recipe};
                                    });
                                }
                            }
                        }
                    } else {
                        elements.container.warn("Can't get method or field: {}, {}", clazz, name);
                    }
                });
            }
        }
        elements.container.warn("loadRecipeBuilder: {}", elements.recipes.size());
    }

    private static void loadNetwork(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModNetwork.class.getName());
        elements.networks = new ArrayList<>(asmDataSet == null ? 0 : asmDataSet.size());
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                //noinspection SimplifyOptionalCallChains
                LoaderHelper.getOrLoadClass(elements, asmData.getClassName()).ifPresent(messageClass -> LoaderHelper.getOrLoadClass(elements, (String) asmData.getAnnotationInfo().get("handlerClass")).ifPresent(handlerClass -> {
                    List<ModAnnotation.EnumHolder> sideHolders = (List<ModAnnotation.EnumHolder>) asmData.getAnnotationInfo().get("side");
                    Side[] sides;
                    if (sideHolders == null) {
                        sides = new Side[] {Side.CLIENT, Side.SERVER};
                    } else {
                        sides = sideHolders.stream()
                                .map(h -> Side.valueOf(h.getValue()))
                                .toArray(Side[]::new);
                    }
                    elements.networks.add(Triple.of(handlerClass, messageClass, sides));
                }));
            }
        }
        elements.container.warn("loadNetwork: {}", elements.networks.size());
    }

    private static void loadCapability(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModCapability.class.getName());
        elements.capabilities = new ArrayList<>(asmDataSet == null ? 0 : asmDataSet.size());
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                LoaderHelper.getOrLoadClass(elements, asmData.getClassName()).ifPresent(clazz -> {
                    Map<String, Object> info = asmData.getAnnotationInfo();
                    String typeInterfaceClass = (String) info.get("typeInterfaceClass");
                    String typeImplementationClass = (String) info.get("typeImplementationClass");
                    String storageClass = (String) info.get("storageClass");
                    Optional<Class> typeInterfaceClassOpt = LoaderHelper.getOrLoadClass(elements, typeInterfaceClass);
                    Optional<Class> typeImplementationClassOpt = LoaderHelper.getOrLoadClass(elements, typeImplementationClass);
                    Optional<Class> storageClassOpt = LoaderHelper.getOrLoadClass(elements, storageClass);
                    if (typeImplementationClassOpt.isPresent() && typeInterfaceClassOpt.isPresent() && storageClassOpt.isPresent()) {
                        CapabilityObject capability = new CapabilityObject(typeInterfaceClassOpt.get(), typeImplementationClassOpt.get(), storageClassOpt.get());
                        elements.capabilities.add(capability);
                        CapabilityManager.INSTANCE.register(capability.typeInterfaceClass, capability.storageInstance(), capability::newInstance);
                    }
                });
            }
        }
        elements.container.warn("loadCapability: {}", elements.capabilities.size());
    }

    private static void loadEnchantment(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModEnchantment.class.getName());
        elements.enchantments = new ArrayList<>(asmDataSet == null ? 0 : asmDataSet.size());
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                LoaderHelper.getOrLoadClass(elements, asmData.getClassName()).ifPresent(clazz -> {
                    // enchantment
                    String objectName = asmData.getObjectName();
                    Enchantment enchantment;
                    Optional<Enchantment> optional;
                    if (objectName == null || objectName.isEmpty()) {
                        optional = ECUtils.reflect.create(clazz, Enchantment.class, elements.container.logger);
                    } else {
                        optional = ECUtils.reflect.getField(clazz, objectName, null, Enchantment.class, elements.container.logger);
                    }
                    if (optional.isPresent()) {
                        enchantment = optional.get();
                    } else {
                        elements.container.warn("Element {} is not an Enchantment", asmData.getAnnotationInfo().get("name"));
                        return;
                    }
                    elements.enchantments.add(enchantment);
                    // name
                    String name = (String) asmData.getAnnotationInfo().get("name");
                    if (name == null || name.isEmpty()) {
                        name = objectName == null ? clazz.getSimpleName().toLowerCase() : objectName.toLowerCase();
                    }
                    if (!name.contains(".")) {
                        name = elements.container.id() + "." + name;
                    }
                    if (enchantment.getName().equals("enchantment." + null)) {
                        enchantment.setName(name);
                    }
                    // registryName
                    if (enchantment.getRegistryName() == null) {
                        String rName = (String) asmData.getAnnotationInfo().get("registerName");
                        if (rName == null || rName.isEmpty()) {
                            enchantment.setRegistryName(elements.container.id(), objectName == null ? clazz.getSimpleName().toLowerCase() : objectName.toLowerCase());
                        } else {
                            enchantment.setRegistryName(elements.container.id(), rName);
                        }
                    }
                });
            }
        }
        elements.container.warn("loadEnchantment: {}", elements.enchantments.size());
    }

    private static void loadPotion(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModPotion.class.getName());
        elements.potions = new ArrayList<>(asmDataSet == null ? 0 : asmDataSet.size());
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                LoaderHelper.getOrLoadClass(elements, asmData.getClassName()).ifPresent(clazz -> {
                    Map<String, Object> annotationInfo = asmData.getAnnotationInfo();
                    String objectName = asmData.getObjectName();
                    String registerName = (String) annotationInfo.getOrDefault("value", objectName);
                    String potionName = (String) annotationInfo.getOrDefault("name", objectName);
                    ECUtils.reflect.getField(clazz, objectName, null, Potion.class, elements.container.logger).ifPresent(potion -> {
                        if (potion.getRegistryName() == null) {
                            if (registerName.contains(":")) {
                                potion.setRegistryName(registerName);
                            } else {
                                potion.setRegistryName(elements.container.id(), registerName);
                            }
                        }

                        if (potion.getName().isEmpty()) {
                            potion.setPotionName(potionName);
                        }

                        elements.potions.add(potion);
                    });
                });
            }
        }
    }

    private static void loadStaticFunction(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModInvokeStatic.class.getName());
        elements.staticFunction = new ArrayList<>(asmDataSet == null ? 0 : asmDataSet.size());
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                LoaderHelper.getOrLoadClass(elements, asmData.getClassName()).ifPresent(clazz -> {
                    String value = (String) asmData.getAnnotationInfo().get("value");
                    Method method = null;
                    try {
                        method = clazz.getMethod(value);
                        if (method == null) {
                            method = clazz.getDeclaredMethod(value);
                            if (method != null) {
                                method.setAccessible(true);
                            }
                        }
                    } catch (NoSuchMethodException ignored) { }
                    if (method != null) {
                        elements.staticFunction.add(method);
                    } else {
                        elements.container.warn("Skip Function: {} from {}", value, asmData.getClassName());
                    }
                });
            }
        }
        elements.container.warn("loadStaticFunction: {}", elements.staticFunction.size());
    }
}

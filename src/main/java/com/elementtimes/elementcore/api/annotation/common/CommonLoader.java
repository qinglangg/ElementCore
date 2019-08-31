package com.elementtimes.elementcore.api.annotation.common;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.annotation.CapabilityObject;
import com.elementtimes.elementcore.api.annotation.ModTooltip;
import com.elementtimes.elementcore.api.annotation.annotations.*;
import com.elementtimes.elementcore.api.annotation.enums.FluidBlockType;
import com.elementtimes.elementcore.api.annotation.enums.GenType;
import com.elementtimes.elementcore.api.template.SimpleOreGenerator;
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
        loadBlockTooltip(elements);
        // item
        loadItem(elements);
        loadItemRetain(elements);
        loadItemOre(elements);
        loadItemSub(elements);
        loadItemDamageable(elements);
        loadItemTooltip(elements);
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
        elements.container.warn("common load finished");
    }

    private static void loadTab(ECModElements elements) {
        Set<ASMDataTable.ASMData> creativeTabsDataSet = elements.asm.getAll(ModCreativeTabs.class.getName());
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
                    elements.blocks.put(blockData.getClassName(), name == null ? "" : name, block);
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
                    // burningTime
                    int burningTime = (int) blockInfo.getOrDefault("burningTime", -1);
                    if (burningTime > 0) {
                        elements.blockBurningTimes.put(block, burningTime);
                    }
                }
            }
        }
        elements.container.warn("loadBlock - block: {}", elements.blocks.size());
        elements.container.warn("loadBlock - burningTime: {}", elements.blockBurningTimes.size());
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

    private static void loadBlockOre(ECModElements initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModOreDict.class.getName());
        final Map<String, List<Block>> blockOreDictionaries = initializer.blockOreDictionaries;
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData data : asmDataSet) {
                LoaderHelper.getBlock(initializer, data.getClassName(), data.getObjectName()).ifPresent(block -> {
                    List<String> oreNames = (List<String>) data.getAnnotationInfo().get("value");
                    if (oreNames != null) {
                        for (String oreName : oreNames) {
                            List<Block> ores = blockOreDictionaries.getOrDefault(oreName, new LinkedList<>());
                            ores.add(block);
                            blockOreDictionaries.put(oreName, ores);
                        }
                    }
                });
            }
        }
        initializer.container.warn("loadBlockOre: {}", blockOreDictionaries.size());
    }

    private static void loadBlockTileEntity(ECModElements initializer) {
        Set<ASMDataTable.ASMData> dataSet = initializer.asm.getAll(ModBlock.TileEntity.class.getName());
        if (dataSet != null) {
            for (ASMDataTable.ASMData data : dataSet) {
                LoaderHelper.getBlock(initializer, data.getClassName(), data.getObjectName()).ifPresent(block -> {
                    Map<String, Object> teInfo = data.getAnnotationInfo();
                    String name = (String) teInfo.getOrDefault("name", Objects.requireNonNull(block.getRegistryName()).getResourcePath());
                    //noinspection unchecked
                    LoaderHelper.getOrLoadClass(initializer, (String) teInfo.get("clazz")).ifPresent(clazz ->
                            initializer.blockTileEntities.put(block, ImmutablePair.of(name, clazz)));
                });
            }
        }
        initializer.container.warn("loadBlockTileEntity: {}", initializer.blockTileEntities.size());
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
                    List<WorldGenerator> worldGeneratorList = elements.blockWorldGen.getOrDefault(type, new LinkedList<>());
                    worldGeneratorList.add(new SimpleOreGenerator(yRange, yMin, count, times, probability, dimBlackList, dimWhiteList, block.getDefaultState()));
                    elements.blockWorldGen.put(type, worldGeneratorList);
                });
            }
        }
        Set<ASMDataTable.ASMData> asmWorldGenCustomDataSet = elements.asm.getAll(ModBlock.WorldGenClass.class.getName());
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
                                List<WorldGenerator> worldGeneratorList = elements.blockWorldGen.getOrDefault(type, new LinkedList<>());
                                worldGeneratorList.add(o);
                                elements.blockWorldGen.put(type, worldGeneratorList);
                            });
                });
            }
        }
        elements.container.warn("loadWorldGenerator: {}", elements.blockWorldGen.size());
    }

    private static void loadBlockTooltip(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmWorldGenDataSet = elements.asm.getAll(ModBlock.Tooltip.class.getName());
        if (asmWorldGenDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmWorldGenDataSet) {
                LoaderHelper.getBlock(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                    for (String tooltip : (List<String>) asmData.getAnnotationInfo().get("value")) {
                        elements.toolTips.add(new ModTooltip(block, tooltip));
                    }
                });
            }
        }
        elements.container.warn("loadTooltip(Block): {}", elements.toolTips.size());
    }

    private static void loadItem(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModItem.class.getName());
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
                    elements.items.put(asmData.getClassName(), name == null ? "" : name, item);
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

    private static void loadItemOre(ECModElements initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModOreDict.class.getName());
        final Map<String, List<Item>> itemOreDictionaries = initializer.itemOreDictionaries;
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                LoaderHelper.getItem(initializer, asmData.getClassName(), asmData.getObjectName()).ifPresent(item -> {
                    List<String> oreNames = (List<String>) asmData.getAnnotationInfo().get("value");
                    for (String oreName : oreNames) {
                        List<Item> ores = itemOreDictionaries.getOrDefault(oreName, new LinkedList<>());
                        ores.add(item);
                        itemOreDictionaries.put(oreName, ores);
                    }
                });
            }
        }
        initializer.container.warn("loadItemOre: {}", itemOreDictionaries.size());
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

    private static void loadItemTooltip(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmWorldGenDataSet = elements.asm.getAll(ModBlock.Tooltip.class.getName());
        if (asmWorldGenDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmWorldGenDataSet) {
                LoaderHelper.getItem(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(item -> {
                    for (String tooltip : (List<String>) asmData.getAnnotationInfo().get("value")) {
                        elements.toolTips.add(new ModTooltip(item, tooltip));
                    }
                });
            }
        }
        elements.container.warn("loadTooltips(Item+Block): {}", elements.toolTips.size());
    }

    private static void loadFluid(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModFluid.class.getName());
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
                    elements.fluids.put(asmData.getClassName(), name == null ? "" : name, fluid);
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
                        Block block = ECUtils.reflect.create(className, new Object[]{fluid}, Block.class, elements.container.logger).orElse(null);
                        // fluid
                        LoaderHelper.decorateFluidBlock(block, registerName, creativeTabKey, unlocalizedName, fluid.getName(), density, elements);
                        if (block != null) {
                            fluidBlock = (f) -> block;
                        }
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
    }

    private static void loadRecipe(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModRecipe.class.getName());
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
                                    return (IRecipe[]) ((Collection) o2).stream()
                                            .filter(or -> or instanceof IRecipe)
                                            .toArray();
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
                        elements.container.warn("loadRecipe: {}", elements.recipes.size());
                    }
                });
            }
        }
        elements.container.warn("loadRecipeBuilder: {}", elements.recipes.size());
    }

    private static void loadNetwork(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModNetwork.class.getName());
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

    private static void loadCapability(ECModElements initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModCapability.class.getName());
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                LoaderHelper.getOrLoadClass(initializer, asmData.getClassName()).ifPresent(clazz -> {
                    Map<String, Object> info = asmData.getAnnotationInfo();
                    String typeInterfaceClass = (String) info.get("typeInterfaceClass");
                    String typeImplementationClass = (String) info.get("typeImplementationClass");
                    String storageClass = (String) info.get("storageClass");
                    Optional<Class> typeInterfaceClassOpt = LoaderHelper.getOrLoadClass(initializer, typeInterfaceClass);
                    Optional<Class> typeImplementationClassOpt = LoaderHelper.getOrLoadClass(initializer, typeImplementationClass);
                    Optional<Class> storageClassOpt = LoaderHelper.getOrLoadClass(initializer, storageClass);
                    if (typeImplementationClassOpt.isPresent() && typeInterfaceClassOpt.isPresent() && storageClassOpt.isPresent()) {
                        final CapabilityObject capability = new CapabilityObject(typeInterfaceClassOpt.get(), typeImplementationClassOpt.get(), storageClassOpt.get());
                        initializer.capabilities.add(capability);
                        CapabilityManager.INSTANCE.register(capability.typeInterfaceClass, capability.storageInstance(), capability::newInstance);
                    }
                });
            }
        }
        initializer.container.warn("loadCapability: {}", initializer.capabilities.size());
    }

    private static void loadEnchantment(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModEnchantment.class.getName());
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

    private static void loadStaticFunction(ECModElements initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModInvokeStatic.class.getName());
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                LoaderHelper.getOrLoadClass(initializer, asmData.getClassName()).ifPresent(clazz -> {
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
                        initializer.staticFunction.add(method);
                    } else {
                        initializer.container.warn("Skip Function: {} from {}", value, asmData.getClassName());
                    }
                });
            }
        }
        initializer.container.warn("loadStaticFunction: {}", initializer.staticFunction.size());
    }
}

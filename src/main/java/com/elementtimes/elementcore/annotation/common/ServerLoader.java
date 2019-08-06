package com.elementtimes.elementcore.annotation.common;

import com.elementtimes.elementcore.ElementContainer;
import com.elementtimes.elementcore.annotation.CapabilityObject;
import com.elementtimes.elementcore.template.SimpleOreGenerator;
import com.elementtimes.elementcore.annotation.annotations.*;
import com.elementtimes.elementcore.annotation.enums.FluidBlockType;
import com.elementtimes.elementcore.annotation.enums.GenType;
import com.elementtimes.elementcore.util.ReflectUtil;
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
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Triple;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 服务端资源加载
 * @author luqin2007
 */
@SuppressWarnings("unchecked")
public class ServerLoader {

    public static void load(ElementContainer initializer) {
        initializer.modInfo.warn("server load start");
        loadTab(initializer);
        // block
        loadBlock(initializer);
        loadBlockHarvestLevel(initializer);
        loadBlockOre(initializer);
        loadBlockTileEntity(initializer);
        loadBlockState(initializer);
        loadBlockWorldGenerator(initializer);
        // item
        loadItem(initializer);
        loadItemRetain(initializer);
        loadItemOre(initializer);
        loadItemSub(initializer);
        loadItemDamageable(initializer);
        // fluid
        loadFluid(initializer);
        loadFluidBlock(initializer);
        // recipe
        loadRecipe(initializer);
        // network
        loadNetwork(initializer);
        // capability
        loadCapability(initializer);
        // enchantment
        loadEnchantment(initializer);
        // element
        loadStaticFunction(initializer);
        initializer.modInfo.warn("server load finished");
    }

    private static void loadTab(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> creativeTabsDataSet = initializer.asm.getAll(ModCreativeTabs.class.getName());
        if (creativeTabsDataSet != null) {
            for (ASMDataTable.ASMData creativeTabsData : creativeTabsDataSet) {
                String key = (String) creativeTabsData.getAnnotationInfo().get("value");
                if (key == null || key.isEmpty()) {
                    key = creativeTabsData.getObjectName().toLowerCase();
                }
                Optional<Class> classOptional = LoaderHelper.getOrLoadClass(initializer, creativeTabsData.getClassName());
                if (classOptional.isPresent()) {
                    Optional<Object> tabOpt = ReflectUtil.getField(classOptional.get(), creativeTabsData.getObjectName(), null, initializer).filter(o -> o instanceof CreativeTabs);
                    if (tabOpt.isPresent()) {
                        initializer.tabs.put(key, (CreativeTabs) tabOpt.get());
                    }
                }
            }
        }

        initializer.modInfo.warn("loadTab: {}", initializer.tabs.size());
    }

    private static void loadBlock(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> blockAsmDataSet = initializer.asm.getAll(ModBlock.class.getName());
        for (ASMDataTable.ASMData blockData : blockAsmDataSet) {
            Optional<Class> classOpt = LoaderHelper.getOrLoadClass(initializer, blockData.getClassName());
            if (classOpt.isPresent()) {
                // block
                Class clazz = classOpt.get();
                String name = blockData.getObjectName();
                Block block;
                Optional optional;
                if (name == null || name.isEmpty()) {
                    optional = ReflectUtil.create(clazz, initializer).filter(o -> o instanceof Block);
                } else {
                    optional = ReflectUtil.getField(clazz, name, null, initializer).filter(o -> o instanceof Block);
                }
                if (optional.isPresent()) {
                    block = (Block) optional.get();
                } else {
                    block = new Block(Material.ROCK);
                }
                initializer.blocks.put(blockData.getClassName(), name == null ? "" : name, block);
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
                        block.setRegistryName(initializer.modInfo.id(), registryName);
                    }
                }
                // unlocalizedName
                if ("tile.null".equals(block.getUnlocalizedName())) {
                    String unlocalizedName = (String) blockInfo.get("unlocalizedName");
                    if (unlocalizedName != null && !unlocalizedName.isEmpty()) {
                        block.setUnlocalizedName(initializer.modInfo.id() + "." + unlocalizedName);
                    } else {
                        ResourceLocation registryName = block.getRegistryName();
                        block.setUnlocalizedName(registryName.getResourceDomain() + "." + registryName.getResourcePath());
                    }
                }
                // creativeTabs
                CreativeTabs tab = LoaderHelper.getTab((String) blockInfo.get("creativeTabKey"), initializer);
                if (tab != null) {
                    block.setCreativeTab(tab);
                }
                // burningTime
                int burningTime = (int) blockInfo.getOrDefault("burningTime", -1);
                if (burningTime > 0) {
                    initializer.blockBurningTimes.put(block, burningTime);
                }
            }
        }

        initializer.modInfo.warn("loadBlock - block: {}", initializer.blocks.size());
        initializer.modInfo.warn("loadBlock - burningTime: {}", initializer.blockBurningTimes.size());
    }

    private static void loadBlockHarvestLevel(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModBlock.HarvestLevel.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData data : asmDataSet) {
            LoaderHelper.getBlock(initializer, data.getClassName(), data.getObjectName()).ifPresent(block -> {
                Map<String, Object> info = data.getAnnotationInfo();
                String toolClass = (String) info.getOrDefault("toolClass", "pickaxe");
                int level = (int) info.getOrDefault("level", 2);
                block.setHarvestLevel(toolClass, level);
            });
        }
    }

    private static void loadBlockOre(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModOreDict.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData data : asmDataSet) {
            LoaderHelper.getBlock(initializer, data.getClassName(), data.getObjectName()).ifPresent(block -> {
                String oreName = (String) data.getAnnotationInfo().get("value");
                if (oreName != null && !oreName.isEmpty()) {
                    initializer.blockOreDictionaries.put(block, oreName);
                }
            });
        }

        initializer.modInfo.warn("loadBlockOre: {}", initializer.blockOreDictionaries.size());
    }

    private static void loadBlockTileEntity(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> dataSet = initializer.asm.getAll(ModBlock.TileEntity.class.getName());
        if (dataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData data : dataSet) {
            LoaderHelper.getBlock(initializer, data.getClassName(), data.getObjectName()).ifPresent(block -> {
                Map<String, Object> teInfo = data.getAnnotationInfo();
                String name = (String) teInfo.getOrDefault("name", Objects.requireNonNull(block.getRegistryName()).getResourcePath());
                //noinspection unchecked
                LoaderHelper.getOrLoadClass(initializer, (String) teInfo.get("clazz")).ifPresent(clazz ->
                        initializer.blockTileEntities.put(block, ImmutablePair.of(name, clazz)));
            });
        }

        initializer.modInfo.warn("loadBlockTileEntity: {}", initializer.blockTileEntities.size());
    }

    private static void loadBlockState(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModBlock.StateMap.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            LoaderHelper.getBlock(initializer, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                Map<String, Object> info = asmData.getAnnotationInfo();
                boolean useB3D = (boolean) info.getOrDefault("useB3D", false);
                boolean useOBJ = (boolean) info.getOrDefault("useOBJ", false);
                initializer.blockObj |= useOBJ;
                initializer.blockB3d |= useB3D;
            });
        }

        initializer.modInfo.warn("loadBlockState: OBJ={}, B3D={}", initializer.blockObj, initializer.blockB3d);
    }

    private static void loadBlockWorldGenerator(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> asmWorldGenDataSet = initializer.asm.getAll(ModBlock.WorldGen.class.getName());
        if (asmWorldGenDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmWorldGenDataSet) {
                LoaderHelper.getBlock(initializer, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                    Map<String, Object> info = asmData.getAnnotationInfo();
                    int yRange = (int) info.getOrDefault("YRange", 48);
                    int yMin = (int) info.getOrDefault("YMin", 16);
                    int count = (int) info.getOrDefault("count", 8);
                    int times = (int) info.getOrDefault("times", 6);
                    float probability = (float) info.getOrDefault("probability", 0.6);
                    int[] dimBlackList = (int[]) info.getOrDefault("dimBlackList", new int[0]);
                    int[] dimWhiteList = (int[]) info.getOrDefault("dimWhiteList", new int[0]);
                    GenType type = (GenType) info.getOrDefault("type", GenType.Ore);
                    List<WorldGenerator> worldGeneratorList = initializer.blockWorldGen.getOrDefault(type, new LinkedList<>());
                    worldGeneratorList.add(new SimpleOreGenerator(yRange, yMin, count, times, probability, dimBlackList, dimWhiteList, block.getDefaultState()));
                    initializer.blockWorldGen.put(type, worldGeneratorList);
                });
            }
        }
        Set<ASMDataTable.ASMData> asmWorldGenCustomDataSet = initializer.asm.getAll(ModBlock.WorldGenClass.class.getName());
        if (asmWorldGenCustomDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmWorldGenCustomDataSet) {
                LoaderHelper.getBlock(initializer, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                    String className = (String) asmData.getAnnotationInfo().get("value");
                    GenType type = (GenType) asmData.getAnnotationInfo().getOrDefault("type", GenType.Ore);
                    ReflectUtil.create(className, new Object[] {block}, initializer)
                            .filter(o -> o instanceof WorldGenerator)
                            .ifPresent(o -> {
                                List<WorldGenerator> worldGeneratorList = initializer.blockWorldGen.getOrDefault(type, new LinkedList<>());
                                worldGeneratorList.add((WorldGenerator) o);
                                initializer.blockWorldGen.put(type, worldGeneratorList);
                            });
                });
            }
        }
    }

    private static void loadItem(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModItem.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            Optional<Class> classOpt = LoaderHelper.getOrLoadClass(initializer, asmData.getClassName());
            if (classOpt.isPresent()) {
                // Item
                Class clazz = classOpt.get();
                String name = asmData.getObjectName();
                Item item;
                Optional optional;
                if (name == null || name.isEmpty()) {
                    optional = ReflectUtil.create(clazz, initializer).filter(o -> o instanceof Item);
                } else {
                    optional = ReflectUtil.getField(clazz, name, null, initializer).filter(o -> o instanceof Item);
                }
                if (optional.isPresent()) {
                    item = (Item) optional.get();
                } else {
                    item = new Item();
                }
                initializer.items.put(asmData.getClassName(), name == null ? "" : name, item);
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
                        item.setRegistryName(initializer.modInfo.id(), registryName);
                    }
                }
                // unlocalizedName
                if ("item.null".equals(item.getUnlocalizedName())) {
                    String unlocalizedName = (String) asmData.getAnnotationInfo().get("unlocalizedName");
                    if (unlocalizedName != null && !unlocalizedName.isEmpty()) {
                        item.setUnlocalizedName(initializer.modInfo.id() + "." + unlocalizedName);
                    } else {
                        ResourceLocation registryName = item.getRegistryName();
                        item.setUnlocalizedName(registryName.getResourceDomain() + "." + registryName.getResourcePath());
                    }
                }
                // creativeTabs
                CreativeTabs tab = LoaderHelper.getTab((String) asmData.getAnnotationInfo().get("creativeTabKey"), initializer);
                if (tab != null) {
                    item.setCreativeTab(tab);
                }
            }
        }

        initializer.modInfo.warn("loadItem: {}", initializer.items.size());
    }

    private static void loadItemRetain(ElementContainer initializer) {
        final Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModItem.RetainInCrafting.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            LoaderHelper.getItem(initializer, asmData.getClassName(), asmData.getObjectName()).ifPresent(item -> item.setContainerItem(item));
        }
    }

    private static void loadItemOre(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModOreDict.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            LoaderHelper.getItem(initializer, asmData.getClassName(), asmData.getObjectName()).ifPresent(item -> {
                String oreName = (String) asmData.getAnnotationInfo().get("value");
                initializer.itemOreDictionaries.put(item, oreName);
            });
        }

        initializer.modInfo.warn("loadItemOre: {}", initializer.itemOreDictionaries.size());
    }

    private static void loadItemSub(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModOreDict.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            LoaderHelper.getItem(initializer, asmData.getClassName(), asmData.getObjectName()).ifPresent(item -> {
                item.setHasSubtypes(true);
                item.setMaxDamage(0);
                item.setNoRepair();
            });
        }
    }

    private static void loadItemDamageable(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModItem.Damageable.class.getName());
        if (asmDataSet == null) {
            return;
        }
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

    private static void loadFluid(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModFluid.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            Optional<Class> classOpt = LoaderHelper.getOrLoadClass(initializer, asmData.getClassName());
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
                Optional optional;
                Map<String, Object> info = asmData.getAnnotationInfo();
                if (name == null || name.isEmpty()) {
                    optional = ReflectUtil.create(clazz, initializer).filter(o -> o instanceof Fluid);
                } else {
                    optional = ReflectUtil.getField(clazz, name, null, initializer).filter(o -> o instanceof Fluid);
                }
                if (optional.isPresent()) {
                    fluid = (Fluid) optional.get();
                } else {
                    String n = (String) info.getOrDefault("name", defaultName);
                    String stillResource = (String) info.get("stillResource");
                    String flowingResource = (String) info.get("flowingResource");
                    int color = (int) info.getOrDefault("color", 0xFFFFFFFF);
                    fluid = new Fluid(n, new ResourceLocation(initializer.modInfo.id(), stillResource), new ResourceLocation(initializer.modInfo.id(), flowingResource), color);
                }
                initializer.fluids.put(asmData.getClassName(), name == null ? "" : name, fluid);
                // bucket
                if ((boolean) info.getOrDefault("bucket", true)) {
                    initializer.fluidBuckets.add(fluid);
                }
                // density & gaseous
                int density = (int) info.getOrDefault("density", 1000);
                fluid.setDensity(density);
                fluid.setGaseous(density <= 0);
                // creativeTabs
                CreativeTabs tab = LoaderHelper.getTab((String) info.get("creativeTabKey"), initializer);
                if (tab != null) {
                    initializer.fluidTabs.put(fluid, tab);
                }
                // burningTime
                int burningTime = (int) info.getOrDefault("burningTime", -1);
                if (burningTime > 0) {
                    initializer.fluidBurningTimes.put(fluid.getName(), burningTime);
                }
                // texture
                boolean loadTexture = (boolean) info.getOrDefault("loadTexture", true);
                if (loadTexture) {
                    initializer.fluidResources.add(fluid);
                }
            }
        }

        initializer.modInfo.warn("loadFluid - fluids: {}", initializer.fluids.size());
        initializer.modInfo.warn("loadFluid - fluidBuckets: {}", initializer.fluidBuckets.size());
        initializer.modInfo.warn("loadFluid - fluidTabs: {}", initializer.fluidTabs.size());
        initializer.modInfo.warn("loadFluid - fluidBurningTimes: {}", initializer.fluidBurningTimes.size());
        initializer.modInfo.warn("loadFluid - fluidResources: {}", initializer.fluidResources.size());
    }

    private static void loadFluidBlock(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModFluid.FluidBlock.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            LoaderHelper.getFluid(initializer, asmData.getClassName(), asmData.getObjectName()).ifPresent(fluid -> {
                int density = fluid.getDensity();
                Function<Fluid, Block> fluidBlock = null;
                String className = (String) asmData.getAnnotationInfo().get("className");
                String registerName = (String) asmData.getAnnotationInfo().get("registerName");
                String unlocalizedName = (String) asmData.getAnnotationInfo().get("unlocalizedName");
                String creativeTabKey = (String) asmData.getAnnotationInfo().get("creativeTabKey");
                if (className != null && !className.isEmpty()) {
                    Optional<Object> o = ReflectUtil.create(className, new Object[]{fluid}, initializer);
                    Block block = (Block) o.filter(obj -> obj instanceof Block).orElse(null);
                    // fluid
                    LoaderHelper.decorateFluidBlock(block, registerName, creativeTabKey, unlocalizedName, fluid.getName(), density, initializer);
                    if (block != null) {
                        fluidBlock = (f) -> block;
                    }
                }

                if (fluidBlock == null) {
                    fluidBlock = f -> {
                        FluidBlockType type = (FluidBlockType) asmData.getAnnotationInfo().getOrDefault("type", FluidBlockType.Classic);
                        Block block = type.create(f);
                        LoaderHelper.decorateFluidBlock(block, registerName, creativeTabKey, unlocalizedName, fluid.getName(), density, initializer);
                        return block;
                    };
                }

                String resource = (String) asmData.getAnnotationInfo().getOrDefault("resource", "fluids");
                if (resource != null && !resource.isEmpty()) {
                    initializer.fluidBlockStates.put(fluid, resource);
                }

                initializer.fluidBlocks.put(fluid, fluidBlock);
            });
        }

        initializer.modInfo.warn("loadFluidBlock - fluids: {}", initializer.fluidBlocks.size());
    }

    private static void loadRecipe(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModRecipe.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            LoaderHelper.getOrLoadClass(initializer, asmData.getClassName()).ifPresent(clazz -> {
                String name = asmData.getObjectName();
                Object obj = null;
                try {
                    Method method = clazz.getMethod(name);
                    if (method == null) {
                        method = clazz.getDeclaredMethod(name);
                    }
                    if (method != null && Modifier.isStatic(method.getModifiers())) {
                        method.setAccessible(true);
                        obj = method.invoke(null);
                    } else {
                        Field field = clazz.getField(name);
                        if (field == null) {
                            field = clazz.getDeclaredField(name);
                        }
                        if (field != null && Modifier.isStatic(field.getModifiers())) {
                            field.setAccessible(true);
                            obj = field.get(null);
                        }
                    }
                } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
                    initializer.modInfo.warn("Can't find static field or method named {} in {}", asmData.getObjectName(), asmData.getClassName());
                }
                if (obj != null) {
                    Map<String, Object> info = asmData.getAnnotationInfo();
                    String value = (String) info.getOrDefault("value", name);
                    boolean shaped = (boolean) info.getOrDefault("shaped", true);
                    boolean ore = (boolean) info.getOrDefault("ore", true);
                    int width = (int) info.getOrDefault("width", 3);
                    int height = (int) info.getOrDefault("height", 3);
                    final String rName = value.isEmpty() ? name : value;
                    if (obj instanceof Supplier) {
                        final Supplier o = (Supplier) obj;
                        initializer.recipes.add(() -> {
                            Object o2 = o.get();
                            if (o2 instanceof IRecipe) {
                                IRecipe recipe = (IRecipe) o2;
                                if (recipe.getRegistryName() == null) {
                                    recipe.setRegistryName(new ResourceLocation(initializer.modInfo.id(), rName));
                                }
                                return new IRecipe[]{recipe};
                            } else if (o2 instanceof IRecipe[]) {
                                return (IRecipe[]) o2;
                            } else if (o2 instanceof Collection) {
                                return (IRecipe[]) ((Collection) o2).stream()
                                        .filter(or -> or instanceof IRecipe)
                                        .toArray();
                            } else {
                                initializer.modInfo.warn("You annotated a Supplier but it can't provide a Recipe.");
                                return null;
                            }
                        });
                    } else if (obj instanceof IRecipe) {
                        final IRecipe recipe = (IRecipe) obj;
                        if (recipe.getRegistryName() == null) {
                            recipe.setRegistryName(new ResourceLocation(initializer.modInfo.id(), rName));
                        }
                        initializer.recipes.add(() -> new IRecipe[]{recipe});
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
                                initializer.recipes.add(() -> {
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
                                                initializer.modInfo.warn("You put a Supplier into an array or collection, but it can't provider a Recipe.");
                                            }
                                        } else {
                                            initializer.modInfo.warn("You want put other element in a Recipe/Supplier array or collection.");
                                        }
                                    }
                                    return recipes;
                                });
                            } else {
                                // 单一合成表
                                initializer.recipes.add(() -> {
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
                                            initializer.modInfo.warn("The recipe {} return an NULL item.", rName);
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
                                        initializer.modInfo.warn("You want to register a recipe({}) with {} items, but you put {} items in it. Some items will ignore.",
                                                rName, size, objects.length - 1);
                                    }
                                    for (int i = 1; i < objects.length; i++) {
                                        Object o = objects[i];
                                        if (i - 1 >= size) {
                                            initializer.modInfo.warn("Ignore item[()]: {}.", i - 1, o);
                                            break;
                                        }
                                        if (o instanceof String && ((String) o).contains(":")) {
                                            Item item = Item.getByNameOrId((String) o);
                                            if (item == null || item == Items.AIR) {
                                                initializer.modInfo.warn("The recipe {} have an NULL item.", rName);
                                                return null;
                                            }
                                            o = new ItemStack(item);
                                        }
                                        primer.input.set(i - 1, CraftingHelper.getIngredient(o == null ? ItemStack.EMPTY : o));
                                    }
                                    if (shaped) {
                                        if (ore) {
                                            recipe = new ShapedOreRecipe(new ResourceLocation(initializer.modInfo.id(), "recipe"), r, primer);
                                        } else {
                                            recipe = new ShapedRecipes("recipe", primer.width, primer.height, primer.input, r);
                                        }
                                    } else {
                                        if (ore) {
                                            recipe = new ShapelessOreRecipe(new ResourceLocation(initializer.modInfo.id(), "recipe"), primer.input, r);
                                        } else {
                                            recipe = new ShapelessRecipes("recipe", r, primer.input);
                                        }
                                    }
                                    recipe.setRegistryName(new ResourceLocation(initializer.modInfo.id(), rName));
                                    return new IRecipe[]{recipe};
                                });
                            }
                        }
                    }
                } else {
                    initializer.modInfo.warn("You want to convert an EMPTY array/collection to IRecipe!!!");
                }
            });
        }

        initializer.modInfo.warn("loadRecipe: {}", initializer.recipes.size());
    }

    private static void loadNetwork(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModNetwork.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            //noinspection SimplifyOptionalCallChains
            LoaderHelper.getOrLoadClass(initializer, asmData.getClassName()).ifPresent(messageClass -> LoaderHelper.getOrLoadClass(initializer, (String) asmData.getAnnotationInfo().get("handlerClass")).ifPresent(handlerClass -> {
                Side[] sides = (Side[]) asmData.getAnnotationInfo().get("side");
                if (sides == null) {
                    sides = new Side[] {Side.CLIENT, Side.SERVER};
                }
                initializer.networks.add(Triple.of(handlerClass, messageClass, sides));
            }));
        }

        initializer.modInfo.warn("loadNetwork: {}", initializer.networks.size());
    }

    private static void loadCapability(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModCapability.class.getName());
        if (asmDataSet == null) {
            return;
        }
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
                    initializer.capabilities.add(new CapabilityObject(typeInterfaceClassOpt.get(), typeImplementationClassOpt.get(), storageClassOpt.get()));
                }
            });
        }

        initializer.modInfo.warn("loadCapability: {}", initializer.capabilities.size());
    }

    private static void loadEnchantment(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModEnchantment.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            LoaderHelper.getOrLoadClass(initializer, asmData.getClassName()).ifPresent(clazz -> {
                // enchantment
                String objectName = asmData.getObjectName();
                Enchantment enchantment;
                Optional optional;
                if (objectName == null || objectName.isEmpty()) {
                    optional = ReflectUtil.create(clazz, initializer).filter(o -> o instanceof Enchantment);
                } else {
                    optional = ReflectUtil.getField(clazz, objectName, null, initializer).filter(o -> o instanceof Enchantment);
                }
                if (optional.isPresent()) {
                    enchantment = (Enchantment) optional.get();
                } else {
                    initializer.modInfo.warn("Element {} is not an Enchantment", asmData.getAnnotationInfo().get("name"));
                    return;
                }
                initializer.enchantments.add(enchantment);
                // name
                String name = (String) asmData.getAnnotationInfo().get("name");
                if (name == null || name.isEmpty()) {
                    name = objectName == null ? clazz.getSimpleName().toLowerCase() : objectName.toLowerCase();
                }
                if (!name.contains(".")) {
                    name = initializer.modInfo.id() + "." + name;
                }
                if (enchantment.getName().equals("enchantment." + null)) {
                    enchantment.setName(name);
                }
                // registryName
                if (enchantment.getRegistryName() == null) {
                    String rName = (String) asmData.getAnnotationInfo().get("registerName");
                    if (rName == null || rName.isEmpty()) {
                        enchantment.setRegistryName(initializer.modInfo.id(), objectName == null ? clazz.getSimpleName().toLowerCase() : objectName.toLowerCase());
                    } else {
                        enchantment.setRegistryName(initializer.modInfo.id(), rName);
                    }
                }
            });
        }

        initializer.modInfo.warn("loadEnchantment: {}", initializer.enchantments.size());
    }

    private static void loadStaticFunction(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModInvokeStatic.class.getName());
        if (asmDataSet == null) {
            return;
        }
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
                    initializer.modInfo.warn("Skip Function: {} from {}", value, asmData.getClassName());
                }
            });
        }

        initializer.modInfo.warn("loadStaticFunction: {}", initializer.staticFunction.size());
    }
}

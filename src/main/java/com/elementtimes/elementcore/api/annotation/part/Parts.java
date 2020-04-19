package com.elementtimes.elementcore.api.annotation.part;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.annotation.enums.ValueType;
import com.elementtimes.elementcore.api.helper.ObjHelper;
import com.elementtimes.elementcore.api.misc.wrapper.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;
import org.objectweb.asm.Type;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * part 注解类处理方法
 * @author luqin2007
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Parts {

    /**
     * @see com.elementtimes.elementcore.api.annotation.part.Biome
     */
    public static Supplier<Optional<Biome>> biome(Object biome, Object object, ECModElements elements) {
        Map<String, Object> map = ObjHelper.getAnnotationMap(biome);
        if (map == null) {
            return Optional::empty;
        }
        switch (ObjHelper.getEnum(ValueType.class, map.get("type"), ValueType.VALUE)) {
            case OBJECT:
                return getter(elements, map.get("object"))::get;
            case METHOD:
                Class<?> pt = object instanceof Block ? Block.class : EntityType.class;
                return method(elements, map.get("method"), pt)::get;
            case VALUE:
                String name = (String) map.getOrDefault("value", "");
                ResourceLocation location = new ResourceLocation(name);
                return () -> Optional.ofNullable(ForgeRegistries.BIOMES.getValue(location));
            default: return Optional::empty;
        }
    }

    /**
     * @see com.elementtimes.elementcore.api.annotation.part.Material
     */
    public static Optional<Material> material(Object material, ECModElements elements) {
        Map<String, Object> map = ObjHelper.getAnnotationMap(material);
        if (map == null) {
            return Optional.empty();
        }
        switch (ObjHelper.getEnum(ValueType.class, map.get("type"), ValueType.OBJECT)) {
            case OBJECT: return getter(elements, map.get("object")).get();
            case METHOD: return method(elements, map.get("method")).get();
            case VALUE:
                MaterialColor color = MaterialColor.COLORS[MathHelper.clamp((int) map.getOrDefault("colorIndex", 11), 0, 63)];
                boolean isLiquid = (boolean) map.getOrDefault("isLiquid", false);
                boolean isSolid = (boolean) map.getOrDefault("isSolid", true);
                boolean isBlockMovement = (boolean) map.getOrDefault("isBlockMovement", true);
                boolean isOpaque = (boolean) map.getOrDefault("isOpaque", true);
                boolean requiresTool = (boolean) map.getOrDefault("requiresTool", false);
                boolean flammable = (boolean) map.getOrDefault("flammable", false);
                boolean replaceable = (boolean) map.getOrDefault("replaceable", false);
                PushReaction pushReaction = ObjHelper.getEnum(PushReaction.class, map.get("pushReaction"), PushReaction.NORMAL);
                Material m = new Material(color, isLiquid, isSolid, isBlockMovement, isOpaque, !requiresTool, flammable, replaceable, pushReaction);
                return Optional.of(m);
            default: return Optional.empty();
        }
    }

    /**
     * @see BlockProps
     */
    public static Optional<Block.Properties> propertiesBlock(Object prop, ECModElements elements) {
        Map<String, Object> map = ObjHelper.getAnnotationMap(prop);
        if (map == null) {
            return Optional.empty();
        }
        switch (ObjHelper.getEnum(ValueType.class, map.get("type"), ValueType.OBJECT)) {
            case OBJECT:
                return getter(elements, map.get("object")).get()
                        .map(obj -> obj instanceof Block ? Block.Properties.from((Block) obj) : obj)
                        .filter(obj -> obj instanceof Block.Properties).map(o -> (Block.Properties) o);
            case METHOD:
                return method(elements, map.get("method")).get()
                        .map(obj -> obj instanceof Block ? Block.Properties.from((Block) obj) : obj)
                        .filter(obj -> obj instanceof Block.Properties).map(o -> (Block.Properties) o);
            case VALUE:
                Optional<Material> materialOptional = material(map.get("material"), elements);
                if (materialOptional.isPresent()) {
                    Block.Properties properties;
                    if (map.containsKey("colorIndex")) {
                        int colorIndex = MathHelper.clamp((int) map.get("colorIndex"), 0, 63);
                        properties = Block.Properties.create(materialOptional.get(), MaterialColor.COLORS[colorIndex]);
                    } else if (map.containsKey("colorDye")) {
                        int colorDye = MathHelper.clamp((int) map.get("colorDye"), 0, 15);
                        properties = Block.Properties.create(materialOptional.get(), DyeColor.values()[colorDye]);
                    } else {
                        properties = Block.Properties.create(materialOptional.get());
                    }
                    if ((boolean) map.getOrDefault("doesNotBlockMovement", false)) {
                        properties.doesNotBlockMovement();
                    }
                    Float slipperiness = (Float) map.get("slipperiness");
                    if (slipperiness != null) {
                        properties.slipperiness(slipperiness);
                    }
                    if ((boolean) map.getOrDefault("doesNotBlockMovement", false)) {
                        properties.doesNotBlockMovement();
                    }
                    getter(elements, map.get("soundType")).<SoundType>get().ifPresent(properties::sound);
                    Integer lightValue = (Integer) map.get("lightValue");
                    if (lightValue != null) {
                        properties.lightValue(lightValue);
                    }
                    Float hardness = (Float) map.get("hardness");
                    Float resistance = (Float) map.get("resistance");
                    if (hardness != null || resistance != null) {
                        properties.hardnessAndResistance(hardness == null ? 0f : hardness, resistance == null ? 0f : resistance);
                    }
                    if ((boolean) map.getOrDefault("ticksRandomly", false)) {
                        properties.tickRandomly();
                    }
                    if ((boolean) map.getOrDefault("variableOpacity", false)) {
                        properties.variableOpacity();
                    }
                    if (map.containsKey("harvest")) {
                        Map<String, Object> harvestMap = (Map<String, Object>) map.get("harvest");
                        getter(elements, harvestMap.get("tool")).<ToolType>get().ifPresent(type -> {
                            properties.harvestTool(type);
                            properties.harvestLevel((int) harvestMap.getOrDefault("level", -1));
                        });
                    }
                    if ((boolean) map.getOrDefault("noDrops", false)) {
                        properties.noDrops();
                    }
                    getter(elements, map.get("loot")).<Block>get().ifPresent(properties::lootFrom);
                    return Optional.of(properties);
                }
                return Optional.empty();
            default:
                return Optional.empty();
        }
    }

    /**
     * @see Color
     */
    @OnlyIn(Dist.CLIENT)
    public static Optional<ItemColorWrapper> colorItem(Object color, ECModElements elements) {
        Map<String, Object> map = ObjHelper.getAnnotationMap(color);
        if (map == null || map.isEmpty()) {
            return Optional.empty();
        }
        switch (ObjHelper.getEnum(ValueType.class, map.get("type"), ValueType.VALUE)) {
            case VALUE:
                return Optional.of(new ItemColorWrapper((int) map.getOrDefault("value", 0)));
            case METHOD:
                return Optional.of(new ItemColorWrapper(elements, method(elements, map.get("method"), ItemStack.class, int.class)));
            case OBJECT:
                return Optional.of(new ItemColorWrapper(elements, getter(elements, map.get("object"))));
            default: return Optional.empty();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static Optional<BlockColorWrapper> colorBlock(Object color, ECModElements elements) {
        Map<String, Object> map = ObjHelper.getAnnotationMap(color);
        if (map == null || map.isEmpty()) {
            return Optional.empty();
        }
        switch (ObjHelper.getEnum(ValueType.class, map.get("type"), ValueType.VALUE)) {
            case VALUE:
                return Optional.of(new BlockColorWrapper((int) map.getOrDefault("value", 0)));
            case METHOD:
                return Optional.of(new BlockColorWrapper(elements, method(elements, map.get("method"), BlockState.class, IEnviromentBlockReader.class, BlockPos.class, int.class)));
            case OBJECT:
                return Optional.of(new BlockColorWrapper(elements, getter(elements, map.get("object"))));
            default: return Optional.empty();
        }
    }

    /**
     * @see Food
     */
    public static Optional<net.minecraft.item.Food> food(Object food, ECModElements elements) {
        Map<String, Object> map = ObjHelper.getAnnotationMap(food);
        if (map == null) {
            return Optional.empty();
        }
        int hunger = (int) map.getOrDefault("hunger", -1);
        if (hunger == -1) {
            return Optional.empty();
        }
        net.minecraft.item.Food.Builder foodBuilder =
                new net.minecraft.item.Food.Builder().hunger(hunger).saturation((float) map.getOrDefault("saturation", 0f));
        if ((boolean) map.getOrDefault("meat", false)) {
            foodBuilder.meat();
        }
        if ((boolean) map.getOrDefault("alwaysEdible", false)) {
            foodBuilder.setAlwaysEdible();
        }
        if ((boolean) map.getOrDefault("fastToEat", false)) {
            foodBuilder.fastToEat();
        }
        List<Map<String, Object>> effects = (List<Map<String, Object>>) map.getOrDefault("effect", Collections.emptyList());
        for (Map<String, Object> e : effects) {
            effectInstance(e.get("instance"), elements).ifPresent(effectInstance -> {
                float probability = (float) e.getOrDefault("probability", 1f);
                foodBuilder.effect(effectInstance, probability);
            });
        }
        return Optional.of(foodBuilder.build());
    }

    /**
     * @see ItemProps
     */
    public static Optional<Item.Properties> propertiesItem(Object prop, ECModElements elements) {
        Map<String, Object> map = ObjHelper.getAnnotationMap(prop);
        if (map == null) {
            return Optional.empty();
        }
        Item.Properties properties = new Item.Properties();
        Optional<net.minecraft.item.Food> food = getter(elements, map.get("foodGetter")).get();
        if (food.isPresent()) {
            properties.food(food.get());
        } else {
            food(map.get("food"), elements).ifPresent(properties::food);
        }
        if (map.containsKey("maxDamage")) {
            int maxDamage = (int) map.get("maxDamage");
            if (maxDamage != -1) {
                properties.maxDamage(maxDamage);
            }
        }
        if (map.containsKey("containerItem")) {
            getter(elements, map.get("containerItem")).<Item>get().ifPresent(properties::containerItem);
        }
        getter(elements, map.get("group")).<ItemGroup>get().ifPresent(properties::group);
        if (map.containsKey("rarity")) {
            properties.rarity(ObjHelper.getEnum(Rarity.class, map.get("rarity"), Rarity.COMMON));
        }
        if (map.containsKey("noRepair") && (boolean) map.getOrDefault("noRepair", false)) {
            properties.setNoRepair();
        }
        if (map.containsKey("toolType")) {
            List<Map<String, Object>> types = (List<Map<String, Object>>) map.getOrDefault("toolType", Collections.emptyList());
            for (Map<String, Object> typeMap : types) {
                getter(elements, typeMap.get("tool")).<ToolType>get().ifPresent(type -> {
                    int level = (int) typeMap.getOrDefault("level", 0);
                    properties.addToolType(type, level);
                });
            }
        }
        if (map.containsKey("teisr")) {
            AnnotationGetter teisr = getter(elements, map.get("teisr"));
            if (teisr.hasContent()) {
                properties.setTEISR(() -> () -> (net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer) teisr.get().orElse(null));
            }
        }
        return Optional.of(properties);
    }

    /**
     * @see com.elementtimes.elementcore.api.annotation.part.EffectInstance
     */
    public static Optional<EffectInstance> effectInstance(Object effect, ECModElements elements) {
        Map<String, Object> map = ObjHelper.getAnnotationMap(effect);
        if (map == null) {
            return Optional.empty();
        }
        Optional<Effect> effectOpt = getter(elements, map.get("effect")).get();
        if (effectOpt.isPresent()) {
            Effect e = effectOpt.get();
            int duration = (int) map.getOrDefault("duration", 0);
            int amplifier = (int) map.getOrDefault("amplifier", 0);
            boolean ambient = (boolean) map.getOrDefault("ambient", false);
            boolean showParticles = (boolean) map.getOrDefault("showParticles", true);
            EffectInstance instance = new EffectInstance(e, duration, amplifier, ambient, showParticles);
            return Optional.of(instance);
        }
        return Optional.empty();
    }

    /**
     * @see EntitySpawn
     */
    public static Optional<EntitySpawnWrapper> entitySpawn(Object spawn, EntityType<?> type, ECModElements elements) {
        Map<String, Object> map = ObjHelper.getAnnotationMap(spawn);
        if (map == null || type == null) {
            return Optional.empty();
        }
        Supplier<Optional<Biome>> biome = biome(map.get("biome"), type, elements);
        int weight = (int) map.getOrDefault("weight", 10);
        int min = (int) map.getOrDefault("minCount", 4);
        int max = (int) map.getOrDefault("maxCount", 4);
        Biome.SpawnListEntry entry = new Biome.SpawnListEntry(type, weight, min, max);
        EntityClassification classification = ObjHelper.getEnum(EntityClassification.class, map.get("classification"), EntityClassification.CREATURE);
        return Optional.of(new EntitySpawnWrapper(entry, biome, classification));
    }

    /**
     * @see com.elementtimes.elementcore.api.annotation.part.Feature
     */
    public static Optional<BlockFeatureWrapper> feature(Object feature, ECModElements elements, Block block) {
        return feature(feature, elements, () -> Collections.singleton(block));
    }

    /**
     * @see com.elementtimes.elementcore.api.annotation.part.Feature
     */
    public static Optional<BlockFeatureWrapper> feature(Object feature, ECModElements elements, Class<?> block) {
        return feature(feature, elements, () -> ForgeRegistries.BLOCKS.getValues().stream().filter(block::isInstance).collect(Collectors.toList()));
    }

    private static Optional<BlockFeatureWrapper> feature(Object feature, ECModElements elements, Supplier<Collection<Block>> blocks) {
        Map<String, Object> map = ObjHelper.getAnnotationMap(feature);
        if (map == null) {
            return Optional.empty();
        }
        GenerationStage.Decoration decoration = ObjHelper.getEnum(GenerationStage.Decoration.class, map.get("decoration"), GenerationStage.Decoration.UNDERGROUND_ORES);
        Function<Block, Optional<Biome>> biome = b -> biome(map.get("biome"), b, elements).get();
        switch (ObjHelper.getEnum(ValueType.class, map.get("type"), ValueType.VALUE)) {
            case OBJECT:
                AnnotationGetter getterObj = getter(elements, map.get("object"));
                return Optional.of(new BlockFeatureWrapper(decoration, biome, b -> getterObj.<ConfiguredFeature>get().orElse(null), blocks));
            case METHOD:
                AnnotationMethod invokerObj = method(elements, map.get("method"), Block.class);
                return Optional.of(new BlockFeatureWrapper(decoration, biome, b -> invokerObj.<ConfiguredFeature>get(b).orElse(null), blocks));
            case VALUE:
                AnnotationGetter featureSupplier = getter(elements, map.get("feature"));
                AnnotationMethod featureConfigInvoker = method(elements, map.get("featureConfig"), Block.class, Feature.class);
                AnnotationGetter placementSupplier = getter(elements, map.get("placement"));
                AnnotationMethod placementConfigInvoker = method(elements, map.get("placementConfig"), Block.class, Placement.class);
                BlockFeatureWrapper wrapperFeature = new BlockFeatureWrapper(decoration, biome, b -> {
                    Feature f = featureSupplier.<Feature>get().orElse(null);
                    Placement p = placementSupplier.<Placement>get().orElse(null);
                    IFeatureConfig fc = featureConfigInvoker.<IFeatureConfig>get(b, f).orElse(null);
                    IPlacementConfig pc = placementConfigInvoker.<IPlacementConfig>get(b, p).orElse(null);
                    return Biome.createDecoratedFeature(f, fc, p, pc);
                }, blocks);
                return Optional.of(wrapperFeature);
            default: return Optional.empty();

        }
    }

    /**
     * @see Getter
     * @see Getter2
     */
    public static AnnotationGetter getter(ECModElements elements, Object getter) {
        Map<String, Object> getterMap = ObjHelper.getAnnotationMap(getter);
        if (getterMap == null || !getterMap.containsKey("value") || "".equals(getterMap.get("name"))) {
            return AnnotationGetter.EMPTY;
        }
        Object type = getterMap.get("value");
        Optional<Class<?>> optional;
        if (type instanceof String) {
            optional = ObjHelper.findClass(elements, (String) type);
        } else if (type instanceof Type) {
            optional = ObjHelper.findClass(elements, (Type) type);
        } else {
            return AnnotationGetter.EMPTY;
        }
        if (optional.isPresent()) {
            Class<?> aClass = optional.get();
            String name = (String) getterMap.getOrDefault("name", "<init>");
            return new AnnotationGetter(aClass, name);
        } else {
            return AnnotationGetter.EMPTY;
        }
    }

    /**
     * @see Method
     * @see Method2
     */
    public static AnnotationMethod method(ECModElements elements, Object method, Class<?>... argTypes) {
        Map<String, Object> methodMap = ObjHelper.getAnnotationMap(method);
        if (methodMap == null) {
            return AnnotationMethod.EMPTY;
        }
        String className;
        Object container = methodMap.get("value");
        if (container instanceof String) {
            className = (String) container;
            if (className.isEmpty()) {
                return AnnotationMethod.EMPTY;
            }
        } else if (container instanceof Type) {
            className = ((Type) container).getClassName();
            if (Method.class.getName().equals(className)) {
                return AnnotationMethod.EMPTY;
            }
        } else {
            return AnnotationMethod.EMPTY;
        }
        String methodName = (String) methodMap.getOrDefault("name", "<init>");
        if (StringUtils.isNullOrEmpty(methodName)) {
            return AnnotationMethod.EMPTY;
        }
        Optional<Class<?>> optional = ObjHelper.findClass(elements, className);
        if (optional.isPresent()) {
            Class<?> aClass = optional.get();
            return new AnnotationMethod(aClass, methodName, getter(elements, methodMap.get("holder")), argTypes);
        }
        return AnnotationMethod.EMPTY;
    }
}

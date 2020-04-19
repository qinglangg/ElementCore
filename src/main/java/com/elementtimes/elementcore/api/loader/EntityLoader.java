package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.annotation.ModEntity;
import com.elementtimes.elementcore.api.annotation.part.Parts;
import com.elementtimes.elementcore.api.helper.FindOptions;
import com.elementtimes.elementcore.api.helper.ObjHelper;
import com.elementtimes.elementcore.api.misc.wrapper.AnnotationMethod;
import com.elementtimes.elementcore.api.misc.wrapper.EntityRendererWrapper;
import com.elementtimes.elementcore.api.utils.CommonUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.annotation.ElementType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author luqin2007
 */
public class EntityLoader {

    public static void load(ECModElements elements) {
        loadEntity(elements);
        loadEntityType(elements);
        loadEggs(elements);
        loadSpawn(elements);
    }

    private static void loadEntity(ECModElements elements) {
        ObjHelper.stream(elements, ModEntity.class).forEach(data -> {
            loadEntityType(elements, data);
            if (CommonUtils.isClient()) {
                loadEntityRenderer(elements, data);
            }
        });
    }

    private static void loadEntityType(ECModElements elements, ModFileScanData.AnnotationData data) {
        ObjHelper.findClass(elements, data.getClassType()).ifPresent(aClass -> {
            Map<String, Object> objectMap = data.getAnnotationData();
            AnnotationMethod creator = Parts.method(elements, objectMap.get("create"), EntityType.class, World.class);
            EntityClassification classification = ObjHelper.getEnum(EntityClassification.class, objectMap.get("classification"), EntityClassification.CREATURE);
            EntityType.Builder<Entity> builder = EntityType.Builder.create((type, world) -> creator.<Entity>get(type, world).orElseThrow(() -> new NullPointerException(String.format("[%s]Can't create entity from %s", elements.container.id(), creator.getRefName()))), classification);
            if ((boolean) objectMap.getOrDefault("disableSummoning", false)) {
                builder.disableSummoning();
            }
            if ((boolean) objectMap.getOrDefault("disableSerialization", false)) {
                builder.disableSerialization();
            }
            if ((boolean) objectMap.getOrDefault("immuneToFire", false)) {
                builder.immuneToFire();
            }
            builder.setShouldReceiveVelocityUpdates((boolean) objectMap.getOrDefault("velocityUpdates", true));
            int trackingRange = (int) objectMap.getOrDefault("trackerRange", -1);
            if (trackingRange >= 0) {
                builder.setTrackingRange(trackingRange);
            }
            int updateInterval = (int) objectMap.getOrDefault("updateInterval", -1);
            if (updateInterval >= 0) {
                builder.setUpdateInterval(updateInterval);
            }
            float width = (float) objectMap.getOrDefault("width", 0.6f);
            float height = (float) objectMap.getOrDefault("height", 1.8f);
            builder.size(width, height);
            if (objectMap.containsKey("clientFactory")) {
                AnnotationMethod factory = Parts.method(elements, objectMap.get("clientFactory"), FMLPlayMessages.SpawnEntity.class, World.class);
                builder.setCustomClientFactory((spawnEntity, world) -> factory.<Entity>get(spawnEntity, world).orElseGet(() -> {
                    elements.warn("[{}]Can't create entity from {}, use default value.", elements.container.id(), creator.getRefName());
                    return spawnEntity.getEntity();
                }));
            }
            EntityType<?> type = builder.build((String) objectMap.getOrDefault("id", elements.container.id() + "." + ObjHelper.getMemberName(data)));
            ObjHelper.setRegisterName(type, (String) objectMap.get("id"), data, elements);
            elements.entities.add(type);
            elements.generatedEntityTypes.put((Class<? extends Entity>) aClass, type);
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void loadEntityRenderer(ECModElements elements, ModFileScanData.AnnotationData data) {
        ObjHelper.findClass(elements, data.getClassType()).ifPresent(entityClass -> {
            Class<?> manager = net.minecraft.client.renderer.entity.EntityRendererManager.class;
            AnnotationMethod renderer = Parts.method(elements, data.getAnnotationData().get("renderer"), manager);
            if (renderer.hasContent()) {
                elements.entityRenders.add(new EntityRendererWrapper(m -> renderer.get(m).orElseThrow(() -> new NullPointerException(String.format("Can't create EntityRenderer by %s for %s", renderer.getRefName(), entityClass.getName()))), entityClass));
            } else {
                elements.warn("[{}]Can't find EntityRenderer creator for {}", elements.container.id(), entityClass.getName());
            }
        });
    }

    private static void loadEntityType(ECModElements elements) {
        ObjHelper.stream(elements, ModEntity.Type.class).forEach(data -> {
            ObjHelper.find(elements, data, new FindOptions<>(EntityType.class, ElementType.FIELD)).ifPresent(type -> {
                ObjHelper.setRegisterName(type, ObjHelper.getDefault(data), data, elements);
                elements.entities.add(type);
            });
        });
    }

    private static void loadEggs(ECModElements elements) {
        ObjHelper.stream(elements, ModEntity.Egg.class).forEach(data -> {
            EntityType<?> type = findEntityType(elements, data);
            if (type != null) {
                Map<String, Object> map = data.getAnnotationData();
                int primaryColor = (int) map.getOrDefault("primary", 0x00000000);
                int secondaryColor = (int) map.getOrDefault("secondary", 0x00000000);
                Item.Properties properties = Parts.propertiesItem(map.get("prop"), elements).orElseGet(Item.Properties::new);
                SpawnEggItem egg = new SpawnEggItem(type, primaryColor, secondaryColor, properties);
                egg.setRegistryName(type.getRegistryName());
                elements.entityEggs.add(egg);
                elements.generatedEntityEggs.put(type, egg);
            }
        });
    }

    private static void loadSpawn(ECModElements elements) {
        ObjHelper.stream(elements, ModEntity.Spawn.class).forEach(data -> {
            EntityType<?> type = findEntityType(elements, data);
            if (type != null) {
                List<Map<String, Object>> spawners = ObjHelper.getDefault(data);
                for (Map<String, Object> spawner : spawners) {
                    Parts.entitySpawn(spawner, type, elements).ifPresent(elements.entitySpawns::add);
                }
            }
        });
    }

    private static EntityType<?> findEntityType(ECModElements elements, ModFileScanData.AnnotationData data) {
        if (data.getTargetType() == ElementType.FIELD) {
            return ObjHelper.find(elements, data, new FindOptions<>(EntityType.class, ElementType.FIELD)).orElse(null);
        } else {
            Optional<Class<?>> classOptional = ObjHelper.findClass(elements, data.getClassType());
            return classOptional.map(elements.generatedEntityTypes::get).orElse(null);
        }
    }
}

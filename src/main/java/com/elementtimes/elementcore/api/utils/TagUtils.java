package com.elementtimes.elementcore.api.utils;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.*;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class TagUtils {

    private static TagUtils u = null;
    public static TagUtils getInstance() {
        if (u == null) {
            u = new TagUtils();
        }
        return u;
    }

    public <T> Map<ResourceLocation, Tag.Builder<T>> toRegisterMap(Map<String, List<T>> elements) {
        Map<ResourceLocation, Tag.Builder<T>> map = new HashMap<>(elements.size() * 4 / 3);
        elements.forEach((name, list) -> {
            Tag.Builder<T> builder = new Tag.Builder<>();
            builder.add(new Tag.ListEntry<>(list));
            map.put(new ResourceLocation(name), builder);
        });
        return map;
    }

    public Tag<Item> findItems(String name) {
        return findItems(new ResourceLocation(name.toLowerCase()));
    }

    public Tag<Item> findItems(ResourceLocation name) {
        if (name.getNamespace().equals("minecraft")) {
            switch (name.getPath()) {
                case "wool": return ItemTags.WOOL;
                case "planks": return ItemTags.PLANKS;
                case "stone_bricks": return ItemTags.STONE_BRICKS;
                case "wooden_buttons": return ItemTags.WOODEN_BUTTONS;
                case "buttons": return ItemTags.BUTTONS;
                case "carpets": return ItemTags.CARPETS;
                case "wooden_doors": return ItemTags.WOODEN_DOORS;
                case "wooden_stairs": return ItemTags.WOODEN_STAIRS;
                case "wooden_slabs": return ItemTags.WOODEN_SLABS;
                case "wooden_fences": return ItemTags.WOODEN_FENCES;
                case "wooden_pressure_plates": return ItemTags.WOODEN_PRESSURE_PLATES;
                case "wooden_trapdoors": return ItemTags.WOODEN_TRAPDOORS;
                case "doors": return ItemTags.DOORS;
                case "saplings": return ItemTags.SAPLINGS;
                case "logs": return ItemTags.LOGS;
                case "dark_oak_logs": return ItemTags.DARK_OAK_LOGS;
                case "oak_logs": return ItemTags.OAK_LOGS;
                case "birch_logs": return ItemTags.BIRCH_LOGS;
                case "acacia_logs": return ItemTags.ACACIA_LOGS;
                case "jungle_logs": return ItemTags.JUNGLE_LOGS;
                case "spruce_logs": return ItemTags.SPRUCE_LOGS;
                case "banners": return ItemTags.BANNERS;
                case "sand": return ItemTags.SAND;
                case "stairs": return ItemTags.STAIRS;
                case "slabs": return ItemTags.SLABS;
                case "walls": return ItemTags.WALLS;
                case "anvil": return ItemTags.ANVIL;
                case "rails": return ItemTags.RAILS;
                case "leaves": return ItemTags.LEAVES;
                case "trapdoors": return ItemTags.TRAPDOORS;
                case "small_flowers": return ItemTags.SMALL_FLOWERS;
                case "beds": return ItemTags.BEDS;
                case "fences": return ItemTags.FENCES;
                case "boats": return ItemTags.BOATS;
                case "fishes": return ItemTags.FISHES;
                case "signs": return ItemTags.SIGNS;
                case "music_discs": return ItemTags.MUSIC_DISCS;
                case "coals": return ItemTags.COALS;
                case "arrows": return ItemTags.ARROWS;
                default:
            }
        }
        return new ItemTags.Wrapper(name);
    }

    public Tag<Block> findBlocks(String name) {
        return findBlocks(new ResourceLocation(name.toLowerCase()));
    }

    public Tag<Block> findBlocks(ResourceLocation name) {
        if (name.getNamespace().equals("minecraft")) {
            switch (name.getPath()) {
                case "wool": return BlockTags.WOOL;
                case "planks": return BlockTags.PLANKS;
                case "stone_bricks": return BlockTags.STONE_BRICKS;
                case "wooden_buttons": return BlockTags.WOODEN_BUTTONS;
                case "buttons": return BlockTags.BUTTONS;
                case "carpets": return BlockTags.CARPETS;
                case "wooden_doors": return BlockTags.WOODEN_DOORS;
                case "wooden_stairs": return BlockTags.WOODEN_STAIRS;
                case "wooden_slabs": return BlockTags.WOODEN_SLABS;
                case "wooden_fences": return BlockTags.WOODEN_FENCES;
                case "wooden_pressure_plates": return BlockTags.WOODEN_PRESSURE_PLATES;
                case "wooden_trapdoors": return BlockTags.WOODEN_TRAPDOORS;
                case "doors": return BlockTags.DOORS;
                case "saplings": return BlockTags.SAPLINGS;
                case "logs": return BlockTags.LOGS;
                case "dark_oak_logs": return BlockTags.DARK_OAK_LOGS;
                case "oak_logs": return BlockTags.OAK_LOGS;
                case "birch_logs": return BlockTags.BIRCH_LOGS;
                case "acacia_logs": return BlockTags.ACACIA_LOGS;
                case "jungle_logs": return BlockTags.JUNGLE_LOGS;
                case "spruce_logs": return BlockTags.SPRUCE_LOGS;
                case "banners": return BlockTags.BANNERS;
                case "sand": return BlockTags.SAND;
                case "stairs": return BlockTags.STAIRS;
                case "slabs": return BlockTags.SLABS;
                case "walls": return BlockTags.WALLS;
                case "anvil": return BlockTags.ANVIL;
                case "rails": return BlockTags.RAILS;
                case "leaves": return BlockTags.LEAVES;
                case "trapdoors": return BlockTags.TRAPDOORS;
                case "small_flowers": return BlockTags.SMALL_FLOWERS;
                case "beds": return BlockTags.BEDS;
                case "fences": return BlockTags.FENCES;
                case "flower_pots": return BlockTags.FLOWER_POTS;
                case "enderman_holdable": return BlockTags.ENDERMAN_HOLDABLE;
                case "ice": return BlockTags.ICE;
                case "valid_spawn": return BlockTags.VALID_SPAWN;
                case "impermeable": return BlockTags.IMPERMEABLE;
                case "underwater_bonemeals": return BlockTags.UNDERWATER_BONEMEALS;
                case "coral_blocks": return BlockTags.CORAL_BLOCKS;
                case "wall_corals": return BlockTags.WALL_CORALS;
                case "coral_plants": return BlockTags.CORAL_PLANTS;
                case "corals": return BlockTags.CORALS;
                case "bamboo_plantable_on": return BlockTags.BAMBOO_PLANTABLE_ON;
                case "dirt_like": return BlockTags.DIRT_LIKE;
                case "standing_signs": return BlockTags.STANDING_SIGNS;
                case "wall_signs": return BlockTags.WALL_SIGNS;
                case "signs": return BlockTags.SIGNS;
                case "dragon_immune": return BlockTags.DRAGON_IMMUNE;
                case "wither_immune": return BlockTags.WITHER_IMMUNE;
                default:
            }
        }
        return new BlockTags.Wrapper(name);
    }

    public List<ItemStack> findStacks(String name, int count) {
        return findStacks(new ResourceLocation(name), count);
    }

    public List<ItemStack> findStacks(ResourceLocation name, int count) {
        List<Item> items = new ArrayList<>(findItems(name).getAllElements());
        for (Block block : findBlocks(name).getAllElements()) {
            Item item = block.asItem();
            if (!items.contains(item)) {
                items.add(item);
            }
        }
        List<ItemStack> stacks = new ArrayList<>(items.size());
        for (Item item : items) {
            stacks.add(new ItemStack(item, count));
        }
        return stacks;
    }

    public Tag<Fluid> findFluids(String name) {
        return findFluids(new ResourceLocation(name.toLowerCase()));
    }

    public Tag<Fluid> findFluids(ResourceLocation name) {
        if (name.getNamespace().equals("minecraft")) {
            switch (name.getPath()) {
                case "water": return FluidTags.WATER;
                case "lava": return FluidTags.LAVA;
                default:
            }
        }
        return new FluidTags.Wrapper(name);
    }

    public Tag<EntityType<?>> findEntity(String name) {
        return findEntity(new ResourceLocation(name.toLowerCase()));
    }

    public Tag<EntityType<?>> findEntity(ResourceLocation name) {
        if (name.getNamespace().equals("minecraft")) {
            switch (name.getPath()) {
                case "skeletons": return EntityTypeTags.SKELETONS;
                case "raiders": return EntityTypeTags.RAIDERS;
                default:
            }
        }
        return new EntityTypeTags.Wrapper(name);
    }
}

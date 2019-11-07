package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.LoaderHelper;
import com.elementtimes.elementcore.api.annotation.ModElement;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

public class ElementLoader {

    private boolean isLoad = false;
    private ECModElements mElements;

    public ElementLoader(ECModElements elements) {
        mElements = elements;
    }

    private String className = "";

    public void load() {
        if (!isLoad) {
            LoaderHelper.stream(mElements, ModElement.class).forEach(data -> LoaderHelper.loadClass(mElements, data.getClassType().getClassName()).ifPresent(clazz -> {
                className = data.getClassType().getClassName();
                Arrays.stream(clazz.getDeclaredFields())
                        .filter(field -> Modifier.isStatic(field.getModifiers()))
                        .filter(this::supportType)
                        .filter(this::notSkip)
                        .map(this::toPair)
                        .filter(Objects::nonNull)
                        .forEach(this::loadField);
            }));
            isLoad = true;
        }
    }

    private void loadField(Pair<String, Object> object) {
        String name = object.getLeft();
        Object obj = object.getRight();
        if (obj instanceof Block) {
            Block block = (Block) obj;
            LoaderHelper.regName(mElements, block, name);
            mElements.blocks.blocks.put(className + "." + name, block);
        } else if (obj instanceof TileEntityType) {
            TileEntityType type = (TileEntityType) obj;
            LoaderHelper.regName(mElements, type, name);
            mElements.tileEntityTypes.types.add(type);
        } else if (obj instanceof ContainerType) {
            ContainerType type = (ContainerType) obj;
            LoaderHelper.regName(mElements, type, name);
            mElements.containerTypes.types.add(type);
        } else if (obj instanceof Enchantment) {
            Enchantment enchantment = (Enchantment) obj;
            LoaderHelper.regName(mElements, enchantment, name);
            mElements.enchantments.enchantments.add(enchantment);
        } else if (obj instanceof Item) {
            Item item = (Item) obj;
            LoaderHelper.regName(mElements, item, name);
            mElements.items.items.put(className + "." + name, item);
        } else if (obj instanceof FlowingFluid) {
            FlowingFluid fluid = (FlowingFluid) obj;
            LoaderHelper.regName(mElements, fluid, name);
            mElements.fluids.fluids.put(className + "." + name, fluid);
        } else if (obj instanceof ItemGroup) {
            mElements.itemGroups.groups.put(name, (ItemGroup) obj);
        } else if (obj instanceof IRecipeSerializer) {
            IRecipeSerializer serializer = (IRecipeSerializer) obj;
            LoaderHelper.regName(mElements, serializer, name);
            mElements.recipes.serializers.add(serializer);
        } else if (obj instanceof Effect) {
            Effect effect = (Effect) obj;
            LoaderHelper.regName(mElements, effect, name);
            mElements.potions.effects.add(effect);
        } else if (obj instanceof Potion) {
            Potion potion = (Potion) obj;
            LoaderHelper.regName(mElements, potion, name);
            mElements.potions.potions.add(potion);
        }
    }

    private boolean supportType(Field field) {
        Class<?> type = field.getType();
        return Block.class.isAssignableFrom(type)
                || TileEntityType.class.isAssignableFrom(type)
                || ContainerType.class.isAssignableFrom(type)
                || Enchantment.class.isAssignableFrom(type)
                || Item.class.isAssignableFrom(type)
                || Fluid.class.isAssignableFrom(type)
                || Fluid.class.isAssignableFrom(type)
                || IRecipeSerializer.class.isAssignableFrom(type)
                || Effect.class.isAssignableFrom(type)
                || Potion.class.isAssignableFrom(type)
                || ItemGroup.class.isAssignableFrom(type);
    }

    private boolean notSkip(Field field) {
        return !field.isAnnotationPresent(ModElement.Skip.class);
    }

    private Pair<String, Object> toPair(Field field) {
        try {
            if (!Modifier.isPublic(field.getModifiers())) {
                field.setAccessible(true);
            }
            Object o = field.get(null);
            if (o != null) {
                return ImmutablePair.of(field.getName(), o);
            }
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}

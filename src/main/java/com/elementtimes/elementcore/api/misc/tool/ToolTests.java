package com.elementtimes.elementcore.api.misc.tool;

import com.elementtimes.elementcore.api.ECModContainer;
import com.elementtimes.elementcore.api.utils.CommonUtils;
import com.elementtimes.elementcore.api.utils.FluidUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IItemProvider;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Iterator;

public class ToolTests {

    public static boolean testItem(ItemStack stack, IItemProvider item) {
        return stack.getItem() == item.asItem();
    }

    public static boolean testFluid(ItemStack stack, Fluid fluid) {
        return FluidUtils.hasFluid(stack, fluid);
    }

    public static boolean testEnchantment(ItemStack stack, Enchantment enchantment) {
        return EnchantmentHelper.getEnchantments(stack).containsKey(enchantment);
    }

    public static boolean testEntity(ItemStack stack, EntityType<?> entity) {
        Item item = stack.getItem();
        CompoundNBT tag = stack.getTag();
        if (item instanceof SpawnEggItem) {
            return entity == ((SpawnEggItem) item).getType(tag);
        } else {
            if (tag != null && tag.contains("EntityTag", 10)) {
                CompoundNBT compoundnbt = tag.getCompound("EntityTag");
                if (compoundnbt.contains("id", 8)) {
                    return entity == EntityType.byKey(compoundnbt.getString("id")).orElse(null);
                }
            }
        }
        return false;
    }

    public static boolean testBlockClass(ItemStack stack, Class<?> block) {
        return block.isInstance(Block.getBlockFromItem(stack.getItem()));
    }

    public static boolean testItemClass(ItemStack stack, Class<?> item) {
        return item.isInstance(stack.getItem());
    }

    public static boolean testFluidClass(ItemStack stack, Class<?> fluid) {
        LazyOptional<IFluidHandlerItem> optional = FluidUtil.getFluidHandler(stack);
        if (optional.isPresent()) {
            IFluidHandlerItem handlerItem = optional.orElse(null);
            for (int i = 0; i < handlerItem.getTanks(); i++) {
                if (fluid.isInstance(handlerItem.getFluidInTank(i).getFluid())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean testEnchantmentClass(ItemStack stack, Class<?> enchantment) {
        return EnchantmentHelper.getEnchantments(stack).keySet().stream().anyMatch(enchantment::isInstance);
    }

    public static boolean testEntityTypeClass(ItemStack stack, Class<?> entity) {
        Item item = stack.getItem();
        CompoundNBT tag = stack.getTag();
        if (item instanceof SpawnEggItem) {
            return entity.isInstance(((SpawnEggItem) item).getType(tag));
        } else {
            if (tag != null && tag.contains("EntityTag", 10)) {
                CompoundNBT compoundnbt = tag.getCompound("EntityTag");
                if (compoundnbt.contains("id", 8)) {
                    return entity.isInstance(EntityType.byKey(compoundnbt.getString("id")).orElse(null));
                }
            }
        }
        return false;
    }

    public static boolean testEntityClass(ItemStack stack, Class<?> entity) {
        Item item = stack.getItem();
        EntityType<?> type;
        CompoundNBT tag = stack.getTag();
        if (item instanceof SpawnEggItem) {
            type = ((SpawnEggItem) item).getType(tag);
        } else {
            if (tag != null && tag.contains("EntityTag", 10)) {
                CompoundNBT compoundnbt = tag.getCompound("EntityTag");
                if (compoundnbt.contains("id", 8)) {
                    type = EntityType.byKey(compoundnbt.getString("id")).orElse(null);
                } else {
                    type = null;
                }
            } else {
                type = null;
            }
        }
        if (type != null) {
            try {
                if (ECModContainer.MODS.values().stream()
                        .map(ECModContainer::elements)
                        .map(e -> e.generatedEntityTypes)
                        .map(map -> map.get(entity))
                        .anyMatch(tet -> tet == type)) {
                    return true;
                }
                World world;
                if (CommonUtils.isClient()) {
                    world = Minecraft.getInstance().world;
                } else {
                    Iterator<ServerWorld> iterator = ServerLifecycleHooks.getCurrentServer().getWorlds().iterator();
                    world = iterator.hasNext() ? iterator.next() : null;
                }
                return entity.isInstance(type.create(world));
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public static boolean testStack(Object testObject, ItemStack stack, boolean defValue) {
        if (testObject instanceof IItemProvider) {
            return testItem(stack, (IItemProvider) testObject);
        } else if (testObject instanceof Fluid) {
            return testFluid(stack, (Fluid) testObject);
        } else if (testObject instanceof Enchantment) {
            return testEnchantment(stack, (Enchantment) testObject);
        } else if (testObject instanceof EntityType<?>) {
            return testEntity(stack, (EntityType<?>) testObject);
        } else if (testObject instanceof Class) {
            Class<?> c = (Class<?>) testObject;
            if (Block.class.isAssignableFrom(c)) {
                return testBlockClass(stack, (Class<?>) testObject);
            } else if (IItemProvider.class.isAssignableFrom(c)) {
                return testItemClass(stack, (Class<?>) testObject);
            } else if (Fluid.class.isAssignableFrom(c)) {
                return testFluidClass(stack, (Class<?>) testObject);
            } else if (Enchantment.class.isAssignableFrom(c)) {
                return testEnchantmentClass(stack, (Class<?>) testObject);
            } else if (EntityType.class.isAssignableFrom(c)) {
                return testEntityTypeClass(stack, (Class<?>) testObject);
            } else if (net.minecraft.entity.Entity.class.isAssignableFrom(c)) {
                return testEntityClass(stack, (Class<?>) testObject);
            }
        }
        return defValue;
    }
}

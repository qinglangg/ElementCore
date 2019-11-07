package com.elementtimes.elementcore.api.template.groups;

import com.elementtimes.elementcore.api.ECUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * 创造模式物品栏
 * 物品栏物品会变化
 * @author luqin2007
 */
@SuppressWarnings("unused")
public class ItemGroupDynamic extends ItemGroup {
    public static final long DEFAULT_TICK = 20;

    private long dynamicChangeTick;
    private long markTime = 0;
    private int iconIndex = 0;
    private Supplier<List<ItemStack>> dynamicItemSupplier;
    private List<ItemStack> dynamicItems = null;
    private ItemStack stack = ItemStack.EMPTY;

    public ItemGroupDynamic(long tick, String label) {
        super(label);
        dynamicChangeTick = tick;
        dynamicItemSupplier = Collections::emptyList;
    }

    // Object 接受:
    //  ItemStack, IItemProvider, Fluid
    //  Tag<Item>
    //  String, ResourceLocation
    //  Supplier<Object> (Object 同样符合之前要求)
    // 注意 这里 Object 中 String/ResourceLocation 均解释为 RegistryName，而非 Tag
    public ItemGroupDynamic(String label, long tick, Object... objects) {
        super(label);
        dynamicChangeTick = tick;
        dynamicItemSupplier = () -> {
            List<ItemStack> list = new ArrayList<>();
            for (Object object : objects) {
                readObject(list, object);
            }
            return list;
        };
    }

    public ItemGroupDynamic(String label, long tick, boolean isTag, String... names) {
        super(label);
        dynamicChangeTick = tick;
        if (isTag) {
            dynamicItemSupplier = () -> {
                List<Item> list = new ArrayList<>();
                for (String name : names) {
                    Tag<Item> itemTag = ItemTags.getCollection().get(new ResourceLocation(name.toLowerCase()));
                    if (itemTag != null) {
                        for (Item item : itemTag.getAllElements()) {
                            if (!list.contains(item)) {
                                list.add(item);
                            }
                        }
                    }
                }
                List<ItemStack> stacks = new ArrayList<>(list.size());
                for (Item item : list) {
                    stacks.add(new ItemStack(item));
                }
                return stacks;
            };
        } else {
            dynamicItemSupplier = () -> {
                List<ItemStack> stacks = new ArrayList<>(names.length);
                for (String name : names) {
                    Item item = ECUtils.item.getItem(name);
                    if (item != Items.AIR) {
                        stacks.add(new ItemStack(item));
                    }
                }
                return stacks;
            };
        }
    }

    public ItemGroupDynamic(String label, Object... objects) {
        super(label);
        dynamicChangeTick = DEFAULT_TICK;
        dynamicItemSupplier = () -> {
            List<ItemStack> list = new ArrayList<>();
            for (Object object : objects) {
                readObject(list, object);
            }
            return list;
        };
    }

    public ItemGroupDynamic(String label, boolean isTag, String... names) {
        this(label, DEFAULT_TICK, isTag, names);
    }

    private void readObject(List<ItemStack> stacks, Object object) {
        if (object instanceof ItemStack) {
            stacks.add((ItemStack) object);
        } else if (object instanceof IItemProvider) {
            stacks.add(new ItemStack((IItemProvider) object));
        } else if (object instanceof Fluid) {
            ((Fluid) object).getFilledBucket();
        } else if (object instanceof Tag) {
            for (Object element : ((Tag) object).getAllElements()) {
                readObject(stacks, element);
            }
        } else if (object instanceof String) {
            Item item = ECUtils.item.getItem((String) object);
            readObject(stacks, item);
        } else if (object instanceof ResourceLocation) {
            Item item = ECUtils.item.getItem((ResourceLocation) object);
            readObject(stacks, item);
        } else if (object instanceof Supplier) {
            readObject(stacks, ((Supplier) object).get());
        } else if (object instanceof Iterable) {
            ((Collection) object).forEach(obj -> readObject(stacks, obj));
        } else if (object instanceof Object[]) {
            for (Object obj : ((Object[]) object)) {
                readObject(stacks, obj);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public ItemStack getIcon() {
        return createIcon();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public ItemStack createIcon() {
        if (dynamicItems == null) {
            dynamicItems = dynamicItemSupplier.get();
            if (dynamicItems == null || dynamicItems.isEmpty()) {
                dynamicItems = NonNullList.create();
                fill((NonNullList<ItemStack>) dynamicItems);
            }
        }

        long worldTime = Minecraft.getInstance().world.getGameTime();
        if (worldTime - markTime >= dynamicChangeTick) {
            iconIndex++;
            iconIndex %= dynamicItems.size();
            stack = dynamicItems.get(iconIndex);
            markTime = worldTime;
        }
        return stack;
    }
}

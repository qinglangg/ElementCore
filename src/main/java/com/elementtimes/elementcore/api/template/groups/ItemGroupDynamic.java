package com.elementtimes.elementcore.api.template.groups;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 创造模式物品栏
 * 物品栏物品会变化
 * @author luqin2007
 */
public class ItemGroupDynamic extends ItemGroup {
    private static final long DEFAULT_TICK = 20;

    private long dynamicChangeTick;
    private long markTime = 0;
    private int iconIndex = 0;
    private List<ItemStack> dynamicItems;
    private ItemStack stack = ItemStack.EMPTY;

    public ItemGroupDynamic(String label, long changeTick, List<ItemStack> dynamicItems) {
        super(label);
        this.dynamicChangeTick = changeTick;
        this.dynamicItems = dynamicItems;
    }

    /**
     * 切换图标
     * @param label 标签
     * @param changeTick 时间间隔
     * @param elements 显示物品，接受 ItemStack, Item 或 Block
     */
    public ItemGroupDynamic(String label, long changeTick, Object... elements) {
        this(label, changeTick, Arrays.stream(elements)
                .map(e -> {
                    if (e instanceof ItemStack) {
                        return (ItemStack) e;
                    } else if (e instanceof IItemProvider) {
                        return new ItemStack((IItemProvider) e);
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    public ItemGroupDynamic(String label, long tick, ItemStack... itemStacks) {
        this(label, tick, Lists.newArrayList(itemStacks));
    }

    public ItemGroupDynamic(String label, long changeTick) {
        this(label, changeTick, new ArrayList<>());
    }

    public ItemGroupDynamic(String label, ItemStack... stacks) {
        this(label, DEFAULT_TICK, Lists.newArrayList(stacks));
    }

    public ItemGroupDynamic(String label, IItemProvider... items) {
        this(label, DEFAULT_TICK, Arrays.stream(items).map(ItemStack::new).collect(Collectors.toList()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ItemStack createIcon() {
        if (dynamicItems == null) {
            dynamicItems = new ArrayList<>();
        }
        if (dynamicItems.size() == 0) {
            stack = ItemStack.EMPTY;
        } else {
            long worldTime = net.minecraft.client.Minecraft.getInstance().world.getGameTime();
            if (worldTime - markTime >= dynamicChangeTick) {
                iconIndex++;
                iconIndex %= dynamicItems.size();
                stack = dynamicItems.get(iconIndex);
                markTime = worldTime;
            }
        }
        return stack;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fill(NonNullList<ItemStack> items) {
        super.fill(items);
        boolean isEmpty = dynamicItems == null || dynamicItems.isEmpty();
        if (isEmpty) {
            dynamicItems = items;
        }
    }
}

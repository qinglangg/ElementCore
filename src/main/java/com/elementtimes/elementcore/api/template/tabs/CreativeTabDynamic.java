package com.elementtimes.elementcore.api.template.tabs;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
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
public class CreativeTabDynamic extends CreativeTabs {
    private static final long DEFAULT_TICK = 20;

    private long dynamicChangeTick;
    private long markTime = 0;
    private int iconIndex = 0;
    private List<ItemStack> dynamicItems;
    private ItemStack stack = ItemStack.EMPTY;
    private List<Block> blocks = new ArrayList<>();

    public CreativeTabDynamic(String label) {
        super(label);
        dynamicChangeTick = DEFAULT_TICK;
        dynamicItems = new ArrayList<>();
    }

    public CreativeTabDynamic(String label, long tick, ItemStack... itemStacks) {
        super(label);
        dynamicChangeTick = tick;
        dynamicItems = Arrays.asList(itemStacks);

    }

    public CreativeTabDynamic(String label, long changeTick) {
        super(label);
        dynamicChangeTick = changeTick;
        dynamicItems = new ArrayList<>();
    }

    public CreativeTabDynamic(String label, ItemStack... stacks) {
        super(label);
        dynamicChangeTick = DEFAULT_TICK;
        dynamicItems = Arrays.asList(stacks);
    }

    /**
     * 切换图标
     * @param label 标签
     * @param changeTick 时间间隔
     * @param elements 显示物品，接受 ItemStack, Item 或 Block
     */
    public CreativeTabDynamic(String label, long changeTick, Object... elements) {
        super(label);
        dynamicChangeTick = changeTick;
        dynamicItems = Arrays.stream(elements)
                .map(e -> {
                    if (e instanceof ItemStack) {
                        return (ItemStack) e;
                    } else if (e instanceof Item) {
                        return new ItemStack((Item) e);
                    } else if (e instanceof Block) {
                        blocks.add((Block) e);
                        return null;
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Nonnull
    @SideOnly (Side.CLIENT)
    public ItemStack getTabIconItem() {
        if (dynamicItems == null) {
            dynamicItems = new ArrayList<>();
        }
        if (!blocks.isEmpty()) {
            for (Block block : blocks) {
                dynamicItems.add(new ItemStack(block));
            }
            blocks.clear();
        }
        if (dynamicItems.size() == 0) {
            stack = ItemStack.EMPTY;
        } else {
            long worldTime = FMLClientHandler.instance().getWorldClient().getWorldTime();
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
    @SideOnly(Side.CLIENT)
    public void displayAllRelevantItems(@Nonnull NonNullList<ItemStack> list) {
        super.displayAllRelevantItems(list);
        boolean isEmpty = blocks.isEmpty() && (dynamicItems == null || dynamicItems.isEmpty());
        if (isEmpty) {
            dynamicItems = list;
        }
    }

    @Override
    @Nonnull
    @SideOnly (Side.CLIENT)
    public ItemStack getIconItemStack() {
        return getTabIconItem();
    }
}

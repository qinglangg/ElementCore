package com.elementtimes.elementcore.other;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 对 Tooltip 的封装
 * @author luqin2007
 */
public class ModTooltip {

    private final String tooltip;
    private final int[] count;
    private final String[][] nbtOr;
    private final String[][] nbtAnd;
    private final Object itemOrBlock;

    public Item item = null;

    public ModTooltip(Object itemOrBlock, String tooltip) {
        this.itemOrBlock = itemOrBlock;
        final int split = tooltip.indexOf("->");
        if (split < 0) {
            this.tooltip = tooltip;
            count = new int[0];
            nbtOr = new String[0][];
            nbtAnd = new String[0][];
        } else if (split == 0) {
            this.tooltip = tooltip.substring(2);
            count = new int[0];
            nbtOr = new String[0][];
            nbtAnd = new String[0][];
        } else if (split > 0 && (tooltip.startsWith("@n") || tooltip.startsWith("@c"))) {
            String conditionStr = tooltip.substring(0, split);
            this.tooltip = tooltip.substring(split + 2);
            final String[] conditions = conditionStr.split("@");
            count = buildConditions(conditions, "c");
            nbtAnd = buildConditions(conditions, true);
            nbtOr = buildConditions(conditions, false);
        } else {
            throw new RuntimeException("Wrong Tooltip: " + tooltip);
        }
    }

    public boolean match(ItemStack stack) {
        if (item == null) {
            if (itemOrBlock instanceof Item) {
                item = (Item) itemOrBlock;
            } else if (itemOrBlock instanceof Block) {
                item = Item.getItemFromBlock((Block) itemOrBlock);
            } else {
                return false;
            }
        }

        if (stack.getItem() == item) {
            if (count.length == 0 || ArrayUtils.contains(count, stack.getCount())) {
                boolean checkNbt = false;
                INBT nbt;
                for (String[] strings : nbtOr) {
                    checkNbt |= checkNbt(stack, strings);
                    if (checkNbt) {
                        break;
                    }
                }
                for (String[] strings : nbtAnd) {
                    checkNbt &= checkNbt(stack, strings);
                    if (!checkNbt) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public String buildTooltip(ItemStack itemStack) {
        return getNbt(itemStack, tooltip
                .replace("@c", String.valueOf(itemStack.getCount())));
    }

    public void addTooltip(ItemStack itemStack, List<ITextComponent> tooltips) {
        if (match(itemStack)) {
            tooltips.add(new StringTextComponent(buildTooltip(itemStack)));
        }
    }

    private boolean checkNbt(ItemStack itemStack, String[] strings) {
        INBT nbt = itemStack.getTag();
        for (int i = 0; i < strings.length - 1; i++) {
            String value = strings[i];
            if (nbt instanceof CompoundNBT && ((CompoundNBT) nbt).contains(value)) {
                nbt = ((CompoundNBT) nbt).get(value);
            } else {
                return false;
            }
        }
        return strings[strings.length - 1].equals(nbtToString(nbt));
    }

    private String getNbt(ItemStack itemStack, String tooltip) {
        if (itemStack.getTag() == null) {
            return tooltip;
        }
        int left = tooltip.indexOf("@n{");
        int right;
        if (left < 0) {
            return tooltip;
        } else {
            right = tooltip.indexOf("}", left);
            if (right < 0) {
                return tooltip;
            }
        }
        StringBuilder builder = new StringBuilder();
        while (left > 0 && right > 0) {
            // cut
            builder.append(tooltip, 0, left);
            tooltip = tooltip.substring(right + 1);
            // find
            String[] paths = tooltip.substring(left + 3, right).split(".");
            INBT nbt = itemStack.getTag();
            boolean error = false;
            for (String path : paths) {
                if (nbt instanceof CompoundNBT && ((CompoundNBT) nbt).contains(path)) {
                    nbt = ((CompoundNBT) nbt).get(path);
                } else {
                    error = true;
                    break;
                }
            }
            // append
            if (!error) {
                builder.append(nbtToString(nbt));
            }
            // next
            left = tooltip.indexOf("@n{");
            right = tooltip.indexOf("}", left);
        }
        return builder.toString();
    }

    private String nbtToString(INBT nbt) {
        String n;
        if (nbt instanceof ByteNBT) {
            n = String.valueOf(((ByteNBT) nbt).getByte());
        } else if (nbt instanceof DoubleNBT) {
            n = String.valueOf(((DoubleNBT) nbt).getDouble());
        } else if (nbt instanceof FloatNBT) {
            n = String.valueOf(((FloatNBT) nbt).getFloat());
        } else if (nbt instanceof IntNBT) {
            n = String.valueOf(((IntNBT) nbt).getInt());
        } else if (nbt instanceof LongNBT) {
            n = String.valueOf(((LongNBT) nbt).getLong());
        } else if (nbt instanceof ShortNBT) {
            n = String.valueOf(((ShortNBT) nbt).getShort());
        } else {
            n = nbt.toString();
        }
        return n;
    }

    private String[][] buildConditions(String[] conditions, boolean isAnd) {
        return Arrays.stream(conditions)
                .filter(Objects::nonNull)
                .filter(s -> isAnd ? s.startsWith("n&{") : s.startsWith("n{"))
                .map(s -> s.substring(2, s.length() - 1))
                .map(s -> s.replace('=', '.'))
                .map(s -> s.split("."))
                .toArray(String[][]::new);
    }

    private int[] buildConditions(String[] conditions, String div) {
        return Arrays.stream(conditions)
                .filter(Objects::nonNull)
                .filter(s -> s.startsWith(div))
                .map(s -> s.substring(div.length()))
                .map(s -> s.replaceFirst("=", ""))
                .mapToInt(Integer::parseInt)
                .toArray();
    }
}

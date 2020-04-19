package com.elementtimes.elementcore.api.misc.wrapper;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.misc.tool.ToolTests;
import com.elementtimes.elementcore.api.utils.CommonUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TooltipsWrapper {

    private static ItemStack before = null;

    private Object mObject;
    private Object mValues;

    public TooltipsWrapper(Object values, Object testObj) {
        mValues = values;
        mObject = testObj;
    }

    public void apply(ECModElements elements, ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        if (ToolTests.testStack(mObject, itemStack, true)) {
            boolean changed = false;
            List<ITextComponent> toolTip = event.getToolTip();
            int size = toolTip.size();
            if (mValues instanceof String) {
                toolTip.add(new TranslationTextComponent((String) mValues));
                changed = itemStack == before;
            } else if (mValues instanceof ITextComponent) {
                toolTip.add((ITextComponent) mValues);
                changed = itemStack == before;
            } else if (mValues instanceof String[]) {
                List<TranslationTextComponent> list = Arrays.stream(((String[]) mValues))
                        .map(s -> new TranslationTextComponent(s))
                        .collect(Collectors.toList());
                toolTip.addAll(list);
                changed = itemStack == before && ((String[]) mValues).length != 0;
            } else if (mValues instanceof ITextComponent[]) {
                Collections.addAll(toolTip, (ITextComponent[]) mValues);
                changed = itemStack == before && ((ITextComponent[]) mValues).length != 0;
            } else if (mValues instanceof Collection) {
                List<ITextComponent> list = ((Collection<?>) mValues).stream().map(o -> {
                    ITextComponent t;
                    if (o instanceof String) {
                        t = new TranslationTextComponent((String) o);
                    } else if (o instanceof ITextComponent) {
                        t = (ITextComponent) o;
                    } else if (o != null) {
                        t = new StringTextComponent(o.toString());
                    } else {
                        t = null;
                    }
                    return t;
                }).filter(Objects::nonNull).collect(Collectors.toList());
                toolTip.addAll(list);
                changed = itemStack == before && ((Collection<?>) mValues).size() != 0;
            } else if (mValues instanceof Consumer) {
                List<ITextComponent> copy = new ArrayList<>(toolTip);
                ((Consumer<ItemTooltipEvent>) mValues).accept(event);
                changed = itemStack == before && copy.size() != toolTip.size();
                if (!changed && itemStack != before) {
                    for (int i = 0; i < copy.size(); i++) {
                        if (!copy.get(i).equals(toolTip.get(i))) {
                            changed = true;
                            break;
                        }
                    }
                }
            }
            if (changed) {
                before = itemStack;
                elements.warn("[{}]Tooltips: {} {} -> {}", elements.container.id(), name(itemStack), size, toolTip.size());
            }
        }
    }

    private String name(ItemStack stack) {
        if (stack.getCount() == 0) {
            return "EMPTY";
        } else {
            if (CommonUtils.isClient()) {
                return stack.getItem().getDisplayName(stack).getFormattedText() + "(" + stack.getDamage() + ")";
            } else {
                return stack.getItem().getRegistryName() + "(" + stack.getDamage() + ")";
            }
        }
    }
}

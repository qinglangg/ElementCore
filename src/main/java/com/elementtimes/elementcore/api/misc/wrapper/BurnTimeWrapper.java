package com.elementtimes.elementcore.api.misc.wrapper;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.interfaces.invoker.IntInvoker;
import com.elementtimes.elementcore.api.misc.tool.ToolTests;
import com.elementtimes.elementcore.api.utils.CommonUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;

import java.util.function.Supplier;

/**
 * @author luqin2007
 */
public class BurnTimeWrapper {

    private Object mTime;
    private Object mObject;

    public BurnTimeWrapper(Object burnTime, Object testObj) {
        mTime = burnTime;
        mObject = testObj;
    }

    public void apply(ECModElements elements, FurnaceFuelBurnTimeEvent event) {
        if (ToolTests.testStack(mObject, event.getItemStack(), true)) {
            if (mTime instanceof Integer) {
                int time = (Integer) mTime;
                if (time >= 0) {
                    elements.warn("[{}]Burn Time: {}: {} -> {}", elements.container.id(), name(event.getItemStack()), event.getBurnTime(), time);
                    event.setBurnTime(time);
                }
            } else if (mTime instanceof AnnotationMethod) {
                int time = ((AnnotationMethod) mTime).getInt(event).orElse(-1);
                if (time >= 0) {
                    elements.warn("[{}]Burn Time: {}: {} -> {}", elements.container.id(), name(event.getItemStack()), event.getBurnTime(), time);
                    event.setBurnTime(time);
                }
            } else if (mTime instanceof AnnotationGetter) {
                int time = ((AnnotationGetter) mTime).getInt().orElse(-1);
                if (time >= 0) {
                    elements.warn("[{}]Burn Time: {}: {} -> {}", elements.container.id(), name(event.getItemStack()), event.getBurnTime(), time);
                    event.setBurnTime(time);
                }
            }
        }
    }

    private String name(ItemStack stack) {
        if (stack.isEmpty()) {
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

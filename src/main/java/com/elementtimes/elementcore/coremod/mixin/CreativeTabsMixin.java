package com.elementtimes.elementcore.coremod.mixin;

import com.elementtimes.elementcore.api.common.ECModContainer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

/**
 * 对 CreativeTabs 的注入
 * @author luqin2007
 */
@Mixin(CreativeTabs.class)
public abstract class CreativeTabsMixin {

    @Inject(method = "displayAllRelevantItems", at = @At("TAIL"))
    private void inject_displayAllRelevantItems(NonNullList<ItemStack> items, CallbackInfo ci) {
        ECModContainer.MODS.values().forEach(container -> {
            List<Consumer<NonNullList<ItemStack>>> consumers = container.elements.tabEditors.get((CreativeTabs) (Object) this);
            if (consumers != null && !consumers.isEmpty()) {
                consumers.forEach(editor -> editor.accept(items));
            }
        });
    }
}

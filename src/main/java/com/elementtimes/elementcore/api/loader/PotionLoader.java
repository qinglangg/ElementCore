package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.annotation.ModPotion;
import com.elementtimes.elementcore.api.annotation.part.Parts;
import com.elementtimes.elementcore.api.helper.FindOptions;
import com.elementtimes.elementcore.api.helper.ObjHelper;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.annotation.ElementType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author luqin2007
 */
public class PotionLoader {

    public static void load(ECModElements elements) {
        loadEffect(elements);
        loadPotion(elements);
    }

    private static void loadPotion(ECModElements elements) {
        ObjHelper.stream(elements, ModPotion.class).forEach(data -> {
            FindOptions<Potion> options = new FindOptions<>(Potion.class, ElementType.FIELD, ElementType.TYPE);
            ObjHelper.find(elements, data, options).ifPresent(potion -> {
                String name = ObjHelper.getDefault(data);
                ObjHelper.setRegisterName(potion, name, data, elements);
                elements.potions.add(potion);
                ObjHelper.saveResult(options, elements.generatedPotions);
            });
        });
    }

    private static void loadEffect(ECModElements elements) {
        ObjHelper.stream(elements, ModPotion.Effect.class).forEach(data -> {
            FindOptions<Effect> options = new FindOptions<>(Effect.class, ElementType.FIELD, ElementType.TYPE);
            ObjHelper.find(elements, data, options).ifPresent(effect -> {
                String name = ObjHelper.getDefault(data);
                ObjHelper.setRegisterName(effect, name, data, elements);
                elements.effects.add(effect);
                ObjHelper.saveResult(options, elements.generatedEffects);
            });
        });
    }
}

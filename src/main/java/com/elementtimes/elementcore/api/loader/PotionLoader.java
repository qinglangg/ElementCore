package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.LoaderHelper;
import com.elementtimes.elementcore.api.annotation.ModEffect;
import com.elementtimes.elementcore.api.annotation.ModPotion;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;

import java.util.ArrayList;
import java.util.List;

public class PotionLoader {

    private boolean isPotionLoaded = false;
    private boolean isEffectLeaded = false;
    private ECModElements mElements;

    List<Potion> potions = new ArrayList<>();
    List<Effect> effects = new ArrayList<>();

    public PotionLoader(ECModElements elements) {
        mElements = elements;
    }

    public List<Potion> potions() {
        if (!isPotionLoaded) {
            mElements.elements.load();
            loadPotions();
        }
        return potions;
    }

    private void loadPotions() {
        LoaderHelper.stream(mElements, ModPotion.class).forEach(data -> {
            LoaderHelper.loadClass(mElements, data.getClassType().getClassName()).ifPresent(aClass -> {
                String memberName = data.getMemberName();
                ECUtils.reflect.getField(aClass, memberName, null, Potion.class, mElements.logger).ifPresent(potion -> {
                    LoaderHelper.regName(mElements, potion, LoaderHelper.getDefault(data, memberName));
                    potions.add(potion);
                });
            });
        });
        isPotionLoaded = true;
    }

    public List<Effect> effects() {
        if (!isEffectLeaded) {
            mElements.elements.load();
            loadEffects();
        }
        return effects;
    }

    private void loadEffects() {
        LoaderHelper.stream(mElements, ModEffect.class).forEach(data -> {
            LoaderHelper.loadClass(mElements, data.getClassType().getClassName()).ifPresent(aClass -> {
                String memberName = data.getMemberName();
                ECUtils.reflect.getField(aClass, memberName, null, Effect.class, mElements.logger).ifPresent(effect -> {
                    LoaderHelper.regName(mElements, effect, LoaderHelper.getDefault(data, memberName));
                    effects.add(effect);
                });
            });
        });
        isEffectLeaded = true;
    }
}

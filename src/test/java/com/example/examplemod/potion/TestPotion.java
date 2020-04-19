package com.example.examplemod.potion;

import com.elementtimes.elementcore.api.annotation.ModPotion;
import com.example.examplemod.ExampleMod;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;

@ModPotion
public class TestPotion extends Potion {

    public TestPotion() {
        super("Test", new EffectInstance(ExampleMod.CONTAINER.elements.generatedEffects.get(TestEffect.class), 1000));
    }

}

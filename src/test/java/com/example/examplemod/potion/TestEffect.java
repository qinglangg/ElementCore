package com.example.examplemod.potion;

import com.elementtimes.elementcore.api.annotation.ModPotion;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

@ModPotion.Effect
public class TestEffect extends Effect {

    protected TestEffect() {
        super(EffectType.NEUTRAL, 0xFFAABBCC);
    }
}

package com.example.examplemod.potion;

import com.elementtimes.elementcore.api.annotation.ModItem;
import com.example.examplemod.ExampleMod;
import com.example.examplemod.group.Groups;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.potion.Potion;
import net.minecraft.util.ActionResultType;

@ModItem
public class PotionItem extends Item {

    public PotionItem() {
        super(new Properties().group(Groups.main));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if (!context.getWorld().isRemote) {
            PlayerEntity player = context.getPlayer();
            assert player != null;
            Potion potion = ExampleMod.CONTAINER.elements.generatedPotions.get(TestPotion.class);
            player.addPotionEffect(potion.getEffects().get(0));
        }
        return ActionResultType.PASS;
    }
}

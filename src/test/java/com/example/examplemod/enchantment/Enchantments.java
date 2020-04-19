package com.example.examplemod.enchantment;

import com.elementtimes.elementcore.api.annotation.ModEnchantment;
import com.elementtimes.elementcore.api.annotation.tools.ModTooltips;
import com.example.examplemod.ExampleMod;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;

@Mod.EventBusSubscriber
public class Enchantments {

    @ModEnchantment("ench2")
    @ModTooltips("Tooltips: Enchantment test2(level3)")
    public static Enchantment testEnchantment2 = new TestEnchantment2(3);

    @ModEnchantment
    @ModTooltips("Tooltips: Enchantment test2(level5)")
    public static Enchantment testEnchantment3 = new TestEnchantment2(5);

    @SubscribeEvent
    public static void onAdd(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof PlayerEntity) {
            World world = event.getWorld();
            BlockPos pos = event.getEntity().getPosition();
            spawnBook(world, pos, ExampleMod.CONTAINER.elements.generatedEnchantments.get(TestEnchantment.class));
            spawnBook(world, pos, testEnchantment2, testEnchantment3);
        }
    }

    private static void spawnBook(World world, BlockPos pos, Enchantment... enchantments) {
        ItemStack enchantmentBook = new ItemStack(Items.ENCHANTED_BOOK);
        HashMap<Enchantment, Integer> enchantmentMap = new HashMap<>();
        for (Enchantment enchantment : enchantments) {
            enchantmentMap.put(enchantment, enchantment.getMaxLevel());
        }
        EnchantmentHelper.setEnchantments(enchantmentMap, enchantmentBook);
        Block.spawnAsEntity(world, pos, enchantmentBook);
    }
}

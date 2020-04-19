package com.example.examplemod.block;

import com.elementtimes.elementcore.api.annotation.ModBlock;
import com.elementtimes.elementcore.api.annotation.ModItem;
import com.elementtimes.elementcore.api.annotation.part.Getter;
import com.elementtimes.elementcore.api.annotation.part.ItemProps;
import com.example.examplemod.group.Groups;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

/**
 * 测试 ModBlock 注解
 * @see ModBlock
 */
public class TestModBlock {

    /**
     * 全部默认参数，id: elementcore:block0
     */
    @ModBlock
    public static Block block = new Block(Block.Properties.create(Material.ROCK));

    /**
     * value：自定义方块 id
     * item：方块对应物品
     */
    @ModBlock(value = "blockcn", item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    public static Block blockCustomName = new Block(Block.Properties.create(Material.ROCK));

    /**
     * noItem：不注册对应物品
     * item：方块对应物品
     */
    @ModBlock(noItem = true, item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    public static Block blockNoItem = new Block(Block.Properties.create(Material.ROCK));
    @ModItem
    public static Item blockForNoItem = new BlockItem(blockNoItem, new Item.Properties().group(Groups.main));

    /**
     * 类上注解测试
     */
    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    public static class BlockClass extends Block {

        public BlockClass() {
            super(Properties.create(Material.ROCK));
        }

        @Override
        public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
            if (!worldIn.isRemote) {
                player.sendMessage(new StringTextComponent("Player created by class"));
            }
            return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
        }
    }
}

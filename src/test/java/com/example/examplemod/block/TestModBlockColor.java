package com.example.examplemod.block;

import com.elementtimes.elementcore.api.annotation.ModBlock;
import com.elementtimes.elementcore.api.annotation.enums.ValueType;
import com.elementtimes.elementcore.api.annotation.part.*;
import com.elementtimes.elementcore.api.annotation.tools.ModTooltips;
import com.example.examplemod.group.Groups;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * 测试 ModBlock.Colors 注解
 * @see ModBlock.Colors
 */
public class TestModBlockColor {

    public static final String CLASS = "com.example.examplemod.block.TestModBlockColor";
    public static final String COLOR = "com.example.examplemod.block.TestModBlockColor$ColorRandom";

    /**
     * block - value 测试
     *  item - value 测试
     */
    @ModBlock.Colors(block = @Color(0xFFAABBCC), item = @Color(0xFFCCBBAA))
    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    public static Block blockColor = new Block(Block.Properties.create(Material.ROCK));

    /**
     * block - method 测试
     *  item - method 测试
     */
    @ModBlock.Colors(
            block = @Color(type = ValueType.METHOD, method = @Method2(value = CLASS, name = "colorById")),
             item = @Color(type = ValueType.METHOD, method = @Method2(value = CLASS, name = "colorById")))
    @ModTooltips({"BlockColor = ClientParts.blockColor()", "ItemColor = ClientParts$ItemColor"})
    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    public static Block blockColorMethod = new Block(Block.Properties.create(Material.ROCK));

    /**
     * block - object 测试
     *  item - object 测试
     */
    @ModBlock.Colors(
            block = @Color(type = ValueType.OBJECT, object = @Getter2(COLOR)),
             item = @Color(type = ValueType.OBJECT, object = @Getter2(COLOR)))
    @ModTooltips({"BlockColor = ClientParts.blockColor()", "ItemColor = ClientParts$ItemColor"})
    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    public static Block blockColorObject = new Block(Block.Properties.create(Material.ROCK));

    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    public static Block blockColor0 = new ColorBlock(0xFF000000);
    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    public static Block blockColor1 = new ColorBlock(0xFF444444);
    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    public static Block blockColor2 = new ColorBlock(0xFF888888);
    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    public static Block blockColor3 = new ColorBlock(0xFFCCCCCC);
    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    public static Block blockColor4 = new ColorBlock(0xFFFFFFFF);

    /**
     * 测试：对同一类方块染色
     */
    @ModBlock.Colors(
            block = @Color(type = ValueType.METHOD, method = @Method2(value = CLASS, name = "colorType")),
             item = @Color(type = ValueType.METHOD, method = @Method2(value = CLASS, name = "colorType")))
    public static class ColorBlock extends Block {

        public final int color;

        public ColorBlock(int color) {
            super(Properties.create(Material.ROCK));
            this.color = color;
        }

        @Override
        public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
            if (!worldIn.isRemote) {
                player.sendMessage(new StringTextComponent("#" + Integer.toHexString(color)));
            }
            return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static int colorById(ItemStack stack, int i) {
        int id = Item.getIdFromItem(stack.getItem());
        return 0xFF000000 + id % 0x00FFFFFF;
    }

    @OnlyIn(Dist.CLIENT)
    public static int colorType(ItemStack stack, int i) {
        return ((ColorBlock) Block.getBlockFromItem(stack.getItem())).color;
    }

    @OnlyIn(Dist.CLIENT)
    public static int colorById(BlockState state, IEnviromentBlockReader world, BlockPos pos, int i) {
        return ((ColorBlock) state.getBlock()).color;
    }

    @OnlyIn(Dist.CLIENT)
    public static int colorType(BlockState state, IEnviromentBlockReader world, BlockPos pos, int i) {
        int id = (int) (Block.getStateId(state) + pos.toLong());
        return 0xFF000000 + id % 0x00FFFFFF;
    }

    public static class ColorRandom implements net.minecraft.client.renderer.color.IBlockColor, net.minecraft.client.renderer.color.IItemColor {

        private Random mRandom = new Random(System.currentTimeMillis());

        @Override
        public int getColor(BlockState p_getColor_1_, @Nullable IEnviromentBlockReader p_getColor_2_, @Nullable BlockPos p_getColor_3_, int p_getColor_4_) {
            return 0xFF000000 + mRandom.nextInt(0x00FFFFFF);
        }

        @Override
        public int getColor(ItemStack p_getColor_1_, int p_getColor_2_) {
            return 0xFF000000 + mRandom.nextInt(0x00FFFFFF);
        }
    }
}

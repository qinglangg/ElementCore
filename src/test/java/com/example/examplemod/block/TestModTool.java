package com.example.examplemod.block;

import com.elementtimes.elementcore.api.annotation.ModBlock;
import com.elementtimes.elementcore.api.annotation.enums.ValueType;
import com.elementtimes.elementcore.api.annotation.part.Getter;
import com.elementtimes.elementcore.api.annotation.part.ItemProps;
import com.elementtimes.elementcore.api.annotation.part.Method;
import com.elementtimes.elementcore.api.annotation.tools.ModBurnTime;
import com.elementtimes.elementcore.api.annotation.tools.ModTooltips;
import com.example.examplemod.group.Groups;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;

/**
 * 测试 ModTool 对 Block 的注解
 * @see ModTooltips
 * @see ModBurnTime
 */
public class TestModTool {

    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    @ModTooltips("Tooltips: Block Value")
    public static Block blockTooltips = new TooltipsBlock();

    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    @ModTooltips(type = ValueType.OBJECT, object = @Getter(value = TestModTool.class, name = "tooltipsArray"))
    public static Block blockTooltipsObjectArray = new TooltipsBlock();

    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    @ModTooltips(type = ValueType.OBJECT, object = @Getter(value = TestModTool.class, name = "tooltips"))
    public static Block blockTooltipsObject = new TooltipsBlock();

    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    @ModTooltips(type = ValueType.METHOD, method = @Method(value = TestModTool.class, name = "applyTooltips"))
    public static Block blockTooltipsMethod = new TooltipsBlock();

    @ModTooltips("tooltips.block.class")
    public static class TooltipsBlock extends Block {

        public TooltipsBlock() {
            super(Properties.create(Material.ROCK));
        }
    }

    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    @ModBurnTime(100)
    public static Block blockBurnTime = new Block(Block.Properties.create(Material.ROCK));

    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    @ModBurnTime(type = ValueType.OBJECT, object = @Getter(value = TestModTool.class, name = "burnTime"))
    public static Block blockBurnTimeObject = new Block(Block.Properties.create(Material.ROCK));

    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    public static Block blockBurnTimeMethod0 = new BurnTimeBlock();

    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    public static Block blockBurnTimeMethod1 = new BurnTimeBlock();

    @ModBurnTime(type = ValueType.METHOD, method = @Method(value = TestModTool.class, name = "burnTime"))
    public static class BurnTimeBlock extends Block {
        public BurnTimeBlock() {
            super(Properties.create(Material.ROCK));
        }
    }

    public static String[] tooltipsArray = new String[] {"Tooltips: Block Line 0", "Tooltips: Block Line 1"};

    public static ITextComponent tooltips = new StringTextComponent("Tooltips: Block Field");

    public static void applyTooltips(ItemTooltipEvent event) {
        event.getToolTip().add(new StringTextComponent("Tooltips: Block Method"));
    }

    public static int burnTime = 3000;

    private static int bt = 300;

    public static int burnTime(FurnaceFuelBurnTimeEvent event) {
        if (Block.getBlockFromItem(event.getItemStack().getItem()) == blockBurnTimeMethod0) {
            bt += bt * .1;
        } else {
            bt -= bt * .1;
        }
        return bt;
    }
}

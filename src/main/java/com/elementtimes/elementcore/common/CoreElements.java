package com.elementtimes.elementcore.common;

import com.elementtimes.elementcore.api.annotation.ModBlock;
import com.elementtimes.elementcore.api.annotation.ModElement;
import com.elementtimes.elementcore.api.annotation.ModItem;
import com.elementtimes.elementcore.api.annotation.ModTileEntity;
import com.elementtimes.elementcore.api.template.block.BaseClosableMachine;
import com.elementtimes.elementcore.api.template.groups.ItemGroupDynamic;
import com.elementtimes.elementcore.common.block.EnergyBox;
import com.elementtimes.elementcore.common.block.FluidBox;
import com.elementtimes.elementcore.common.block.ItemBox;
import com.elementtimes.elementcore.common.block.tileentity.TileMachine;
import com.elementtimes.elementcore.common.item.DebugStick;
import net.minecraft.block.Block;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

@ModElement
public class CoreElements {

    public static ItemGroup main = new ItemGroupDynamic("elementcore.main",
            "elementcore:itemcore",
            DebugStick.STACK_DEBUG_SERVER,
            DebugStick.STACK_DEBUG_CLIENT,
            DebugStick.STACK_DEBUG_CLIENT);

    @ModItem.Tags("forge:elementcoreCore")
    @ModItem.Tooltips("\u00a7cTag 测试")
    public static Item itemCore = new Item(new Item.Properties().group(main));

    @ModItem.ItemColor(com.elementtimes.elementcore.client.DebugStickColor.class)
    @ModItem.Tooltips("\u00a7cItemColor 测试")
    public static Item itemDebugger = new DebugStick();

    @ModBlock.Tags("elementcore:glass")
    @ModBlock.Tooltips({"\u00a7cBlockColor 测试", "\u00a7cTag 测试"})
    @ModBlock.BlockColor(com.elementtimes.elementcore.client.GlassColor.class)
    @ModBlock.ItemGroup
    public static Block blockGlass = new GlassBlock(Block.Properties.create(Material.GLASS).lightValue(2));

    @ModBlock.ItemGroup
    @ModBlock.Tooltips("\u00a7cITileEnergy 测试")
    public static Block blockEnergy = new EnergyBox();

    @ModBlock.ItemGroup
    @ModBlock.Tooltips("\u00a7cITileFluidHandler 测试")
    @ModTileEntity(FluidBox.FluidBoxTileEntity.class)
    public static Block blockFluid = new FluidBox();

    @ModBlock.ItemGroup
    @ModBlock.Tooltips("\u00a7cITileItem 测试")
    public static Block blockItem = new ItemBox();

    @ModBlock.ItemGroup
    @ModBlock.Tooltips({"\u00a7cGUI 测试", "\u00a7eBaseTileEntity, BaseClosableMachine, MachineRecipe"})
    public static Block blockMachine = new BaseClosableMachine<TileMachine>(Block.Properties.create(Material.IRON), TileMachine::new);
}

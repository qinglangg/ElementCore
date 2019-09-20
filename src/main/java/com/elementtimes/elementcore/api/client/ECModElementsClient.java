package com.elementtimes.elementcore.api.client;

import com.elementtimes.elementcore.api.common.ECModElements;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 客户端的内容，主要包括渲染部分
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
@SuppressWarnings("WeakerAccess")
public class ECModElementsClient {

    public final ECModElements elements;

    public ECModElementsClient(ECModElements elements) {
        this.elements = elements;
    }

    // Item
    public Map<Item, ItemMeshDefinition> itemMeshDefinition = null;
    public Map<IItemColor, List<Item>> itemColors = null;
    public Map<String, IItemColor> itemColorMap = null;
    public Map<Item, ArrayList<ModelLocation>> itemSubModel = null;

    // Block
    public Map<Block, IStateMapper> blockStateMaps = null;
    public Map<Class, TileEntitySpecialRenderer> blockTesr = null;
    public Map<IBlockColor, List<Block>> blockColors = null;
    public Map<IItemColor, List<Block>> blockItemColors = null;
    public Map<String, IItemColor> blockItemColorMap = null;
    public Map<String, IBlockColor> blockColorMap = null;
    public Map<Block, ArrayList<ModelLocation>> blockStates = null;

}

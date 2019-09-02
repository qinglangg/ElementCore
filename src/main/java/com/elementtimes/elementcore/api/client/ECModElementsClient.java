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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户端的内容，主要包括渲染部分
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
public class ECModElementsClient {

    public final ECModElements elements;

    public ECModElementsClient(ECModElements elements) {
        this.elements = elements;
    }

    // Item
    public final Map<Item, ItemMeshDefinition> itemMeshDefinition = new HashMap<>();
    public final Map<IItemColor, List<Item>> itemColors = new HashMap<>();
    public final Map<String, IItemColor> itemColorMap = new HashMap<>();
    public final Map<Item, ArrayList<ModelLocation>> itemSubModel = new HashMap<>();

    // Block
    public final Map<Block, IStateMapper> blockStateMaps = new HashMap<>();
    public final Map<Class, TileEntitySpecialRenderer> blockTesr = new HashMap<>();
    public final Map<IBlockColor, List<Block>> blockColors = new HashMap<>();
    public final Map<IItemColor, List<Block>> blockItemColors = new HashMap<>();
    public final Map<String, IItemColor> blockItemColorMap = new HashMap<>();
    public final Map<String, IBlockColor> blockColorMap = new HashMap<>();
    public final Map<Block, ArrayList<ModelLocation>> blockStates = new HashMap<>();

}

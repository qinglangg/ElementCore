package com.elementtimes.elementcore.api.client;

import com.elementtimes.elementcore.api.client.event.FMLClientRegister;
import com.elementtimes.elementcore.api.client.loader.ItemClientLoader;
import com.elementtimes.elementcore.api.common.ECModElements;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.ICommand;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
        this.fmlEventRegister = new FMLClientRegister(elements.container);
    }

    // color
    public final Int2ObjectMap<IItemColor> itemValueColors = new Int2ObjectArrayMap<>();
    public final Map<String, IItemColor> itemMethodColors = new HashMap<>();
    public final Map<String, IItemColor> itemObjectColors = new HashMap<>();
    public final Int2ObjectMap<IBlockColor> blockValueColors = new Int2ObjectArrayMap<>();
    public final Map<String, IBlockColor> blockMethodColors = new HashMap<>();
    public final Map<String, IBlockColor> blockObjectColors = new HashMap<>();

    // Item
    public final Map<Item, ItemMeshDefinition> itemMeshDefinition = new HashMap<>();
    public final Map<Item, ResourceLocation[]> itemMeshDefinitionAll = new HashMap<>();
    public final Map<IItemColor, List<Item>> itemColors = new HashMap<>();
    public final Map<Item, ArrayList<ItemClientLoader.SubModel>> itemSubModel = new HashMap<>();

    // Block
    public final Map<Block, IStateMapper> blockStateMaps = new HashMap<>();
    public final Map<Class, TileEntitySpecialRenderer> blockTesr = new HashMap<>();
    public final Map<IBlockColor, List<Block>> blockColors = new HashMap<>();
    public final Map<IItemColor, List<Block>> blockItemColors = new HashMap<>();

    // Entity
    public final Map<Class, IRenderFactory> entityRenders = new HashMap<>();

    // Command
    public final List<ICommand> commands = new ArrayList<>();

    // tooltips
    public final List<BiConsumer<ItemStack, List<String>>> tooltips = new ArrayList<>();

    // key
    public final List<KeyBinding> keys = new ArrayList<>();
    public final Map<KeyBinding, BiConsumer<InputEvent.KeyInputEvent, KeyBinding>> keyEvents = new HashMap<>();

    // event
    public FMLClientRegister fmlEventRegister;
}

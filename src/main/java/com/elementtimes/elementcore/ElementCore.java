package com.elementtimes.elementcore;

import com.elementtimes.elementcore.api.annotation.ModBlock;
import com.elementtimes.elementcore.api.annotation.ModCreativeTabs;
import com.elementtimes.elementcore.api.annotation.ModItem;
import com.elementtimes.elementcore.api.common.ECModContainer;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.template.block.BaseClosableMachine;
import com.elementtimes.elementcore.api.template.gui.client.BaseGuiContainer;
import com.elementtimes.elementcore.api.template.gui.server.BaseContainer;
import com.elementtimes.elementcore.api.template.tabs.CreativeTabDynamic;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.IGuiProvider;
import com.elementtimes.elementcore.common.block.EnergyBox;
import com.elementtimes.elementcore.common.item.DebugStick;
import com.elementtimes.elementcore.common.block.tileentity.TileTest;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * 元素核心
 *
 * @author luqin2007
 */
@SuppressWarnings({"unused"})
@Mod(
        modid = ElementCore.MODID,
        name = "Element Core",
        version = ElementCore.VERSION,
        updateJSON = ElementCore.UPDATE
)
public class ElementCore {
    private static ElementCore INSTANCE = null;

    static final String MODID = "elementcore";
    static final String VERSION = "@version@";
    static final String UPDATE = "https://github.com/luiqn2007/ElementCore/blob/1.12.2/update.json";

    public static ECModElements.Builder builder() {
        return ECModElements.builder();
    }

    public ECModContainer container;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        container = ECModElements.builder().disableDebugMessage().build(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        for (ECModContainer mod : ECModContainer.MODS.values()) {
            mod.elements.fmlEventRegister.onInit(event);
        }
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new IGuiHandler() {
            @Nullable
            @Override
            public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
                TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
                if (tileEntity instanceof IGuiProvider) {
                    return new BaseContainer(tileEntity, player);
                }
                return null;
            }

            @Nullable
            @Override
            @SideOnly(Side.CLIENT)
            public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
                TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
                if (tileEntity instanceof IGuiProvider) {
                    return new BaseGuiContainer(new BaseContainer(tileEntity, player));
                }
                return getServerGuiElement(ID, player, world, x, y, z);
            }
        });
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        for (ECModContainer mod : ECModContainer.MODS.values()) {
            mod.elements.fmlEventRegister.onPostInit(event);
        }
    }

    @Mod.InstanceFactory
    public static ElementCore instance() {
        if (INSTANCE == null) {
            INSTANCE = new ElementCore();
            FluidRegistry.enableUniversalBucket();
        }
        return INSTANCE;
    }

    public static class Items {
        @ModItem(creativeTabKey = "main")
        @ModItem.ItemColor("com.elementtimes.elementcore.client.DebugStickColor")
        @ModItem.HasSubItem(
                metadatas = {0b0000, 0b0001, 0b0010},
                models = {"minecraft:stick", "minecraft:stick", "minecraft:stick"})
        public static Item debugger = new DebugStick();
    }

    public static class Tabs {
        @ModCreativeTabs
        public static CreativeTabs main = new CreativeTabDynamic("elementcore.main", 20L,
                new ItemStack(Items.debugger, 1, 0),
                new ItemStack(Items.debugger, 1, 1),
                new ItemStack(Items.debugger, 1, 2));
    }

    public static class Blocks {
        @ModBlock(creativeTabKey = "main")
        @ModBlock.TileEntity(name = "energy", clazz = "com.elementtimes.elementcore.common.block.EnergyBox$TileEntity")
        @ModBlock.Tooltip("ITileEnergy 测试")
        public static Block energy = new EnergyBox();

//        @ModBlock(creativeTabKey = "main")
//        @ModBlock.TileEntity(name = "test", clazz = "com.elementtimes.elementcore.common.block.tileentity.TileTest")
//        @ModBlock.StateMapperCustom
//        @ModBlock.StateMap
//        public static Block test = new BaseClosableMachine<>(TileTest.class, ElementCore.INSTANCE);
    }
}

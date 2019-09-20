package com.elementtimes.elementcore.api.client.event;

import com.elementtimes.elementcore.api.client.ECModElementsClient;
import com.elementtimes.elementcore.api.client.LoaderHelperClient;
import com.elementtimes.elementcore.api.client.ModelLocation;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.b3d.B3DLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Map;

/**
 * 注解注册
 *
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
public class ForgeBusRegisterClient {

    private ECModElements mElements;
    private ECModElementsClient mClients;

    public ForgeBusRegisterClient(ECModElements elements) {
        mElements = elements;
        mClients = elements.getClientElements();
    }

    @SubscribeEvent
    public void registerModel(ModelRegistryEvent event) {
        ECUtils.common.runWithModActive(mElements.container.mod, this::registerModelFunc, event);
    }

    private void registerModelFunc() {
        // 三方渲染
        if (mElements.blockObj) {
            OBJLoader.INSTANCE.addDomain(mElements.container.id());
        }
        if (mElements.blockB3d) {
            B3DLoader.INSTANCE.addDomain(mElements.container.id());
        }
        // 注册渲染
        if (mElements.items != null) {
            for (Item item : mElements.items.values()) {
                registerItemFunc(item);
            }
        }
        if (mElements.blocks != null) {
            for (Block block : mElements.blocks.values()) {
                registerBlockFunc(block);
            }
        }
        // 注册流体
        if (mElements.fluidBlocks != null) {
            mElements.fluidBlocks.forEach((fluid, block) -> ModelLoader.setCustomStateMapper(fluid.getBlock(), new StateMapperBase() {
                @Nonnull
                @Override
                protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
                    String bs = mElements.fluidBlockStates == null ? null : mElements.fluidBlockStates.get(fluid);
                    ResourceLocation location;
                    if (bs != null && !bs.isEmpty()) {
                        location = new ResourceLocation(mElements.container.id(), bs);
                    } else {
                        location = fluid.getBlock().getRegistryName();
                    }
                    assert location != null;
                    return new ModelResourceLocation(location, fluid.getName());
                }
            }));
        }
        // 注册 TESR
        if (mClients.blockTesr != null) {
            mClients.blockTesr.forEach(ClientRegistry::bindTileEntitySpecialRenderer);
        }
    }

    private void registerItemFunc(Item item) {
        ItemMeshDefinition definition = mClients.itemMeshDefinition == null ? null : mClients.itemMeshDefinition.get(item);
        if (definition != null) {
            ModelLoader.setCustomMeshDefinition(item, definition);
        } else if (mClients.itemSubModel.containsKey(item)) {
            for (Map.Entry<Item, ArrayList<ModelLocation>> entry : mClients.itemSubModel.entrySet()) {
                Item i = entry.getKey();
                for (ModelLocation triple : entry.getValue()) {
                    ModelLoader.setCustomModelResourceLocation(i, triple.metadata, triple.modelResourceLocation);
                }
            }
        } else {
            //noinspection ConstantConditions
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }

    private void registerBlockFunc(Block block) {
        if (mClients.blockStateMaps != null && mClients.blockStateMaps.containsKey(block)) {
            // IStateMapper
            IStateMapper mapper = mClients.blockStateMaps.get(block);
            ModelLoader.setCustomStateMapper(block, mapper);
            // ResourceLocation
            ArrayList<ModelLocation> triples = mClients.blockStates == null ? null : mClients.blockStates.get(block);
            if (triples != null) {
                Item item = Item.getItemFromBlock(block);
                Map<IBlockState, ModelResourceLocation> locationMap;
                //noinspection ConstantConditions
                ModelResourceLocation defLocation = new ModelResourceLocation(block.getRegistryName(), "inventory");
                locationMap = mapper.putStateModelLocations(block);
                if (triples.size() == 0) {
                    // metadata from DefaultState
                    int defMeta = block.getMetaFromState(block.getDefaultState());
                    ModelLoader.setCustomModelResourceLocation(item, defMeta, LoaderHelperClient.getLocationFromState(locationMap, defLocation, block.getDefaultState()));
                    if (defMeta != 0b0000) {
                        // metadata from 0b0000
                        //noinspection deprecation
                        IBlockState stateZero = block.getStateFromMeta(0b0000);
                        ModelLoader.setCustomModelResourceLocation(item, defMeta, LoaderHelperClient.getLocationFromState(locationMap, defLocation, stateZero));
                    }
                    return;
                }
                for (ModelLocation triple : triples) {
                    ModelLoader.setCustomModelResourceLocation(item, triple.metadata, triple.modelResourceLocation);
                }
            } else {
                mapper.putStateModelLocations(block).forEach((iBlockState, modelResourceLocation) -> ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), block.getMetaFromState(iBlockState), modelResourceLocation));
            }
        } else {
            if (mClients.blockStates != null && mClients.blockStates.containsKey(block)) {
                final ArrayList<ModelLocation> triples = mClients.blockStates.get(block);
                Item blockItem = Item.getItemFromBlock(block);
                for (ModelLocation triple : triples) {
                    ModelLoader.setCustomModelResourceLocation(blockItem, triple.metadata, triple.modelResourceLocation);
                }
            } else {
                //noinspection ConstantConditions
                ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
            }
        }
    }

    @SubscribeEvent
    public void regFluidSpirit(TextureStitchEvent.Pre event) {
        regFluidSpiritFunc(event);
//        ECUtils.common.runWithModActive(mInitializer.container.mod, () -> regFluidSpiritFunc(event), event);
    }

    private void regFluidSpiritFunc(TextureStitchEvent.Pre event) {
        TextureMap textureMap = event.getMap();
        if (mElements.fluidResources != null) {
            mElements.fluidResources.forEach(fluid -> {
                if (fluid.getStill() != null) {
                    textureMap.registerSprite(fluid.getStill());
                }

                if (fluid.getFlowing() != null) {
                    textureMap.registerSprite(fluid.getFlowing());
                }
            });
        }
    }

    @SubscribeEvent
    public void registerItemColor(ColorHandlerEvent.Item event) {
        registerItemColorFunc(event);
//        ECUtils.common.runWithModActive(mInitializer.container.mod, () -> registerItemColorFunc(event), event);
    }

    private void registerItemColorFunc(ColorHandlerEvent.Item event) {
        if (mClients.itemColors != null) {
            mClients.itemColors.forEach((color, items) -> {
                if (items.size() > 0) {
                    event.getItemColors().registerItemColorHandler(color, items.toArray(new Item[0]));
                }
            });
        }
        if (mClients.blockItemColors != null) {
            mClients.blockItemColors.forEach((color, blocks) -> {
                if (blocks.size() > 0) {
                    event.getItemColors().registerItemColorHandler(color, blocks.toArray(new Block[0]));
                }
            });
        }
    }

    @SubscribeEvent
    public void registerBlockColor(ColorHandlerEvent.Block event) {
        registerBlockColorFunc(event);
//        ECUtils.common.runWithModActive(mInitializer.container.mod, () -> registerBlockColorFunc(event), event);
    }

    private void registerBlockColorFunc(ColorHandlerEvent.Block event) {
        if (mClients.blockColors != null) {
            mClients.blockColors.forEach((color, blocks) -> {
                if (blocks.size() > 0) {
                    event.getBlockColors().registerBlockColorHandler(color, blocks.toArray(new Block[0]));
                }
            });
        }
    }
}

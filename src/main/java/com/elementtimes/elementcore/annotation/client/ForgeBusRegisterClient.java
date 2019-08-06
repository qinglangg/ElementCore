package com.elementtimes.elementcore.annotation.client;

import com.elementtimes.elementcore.ElementContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.IItemColor;
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

    private ElementContainer mInitializer;

    public ForgeBusRegisterClient(ElementContainer initializer) {
        mInitializer = initializer;
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelRegistryEvent event) {
        // 三方渲染
        if (mInitializer.blockObj) {
            OBJLoader.INSTANCE.addDomain(mInitializer.modInfo.id());
        }
        if (mInitializer.blockB3d) {
            B3DLoader.INSTANCE.addDomain(mInitializer.modInfo.id());
        }
        // 注册渲染
        mInitializer.items.values().forEach(item -> {
            if (mInitializer.itemSubModel.containsKey(item)) {
                for (Map.Entry<Item, ArrayList<ModelLocation>> entry : mInitializer.itemSubModel.entrySet()) {
                    Item i = entry.getKey();
                    for (ModelLocation triple : entry.getValue()) {
                        ModelLoader.setCustomModelResourceLocation(i, triple.metadata, triple.modelResourceLocation);
                    }
                }
            } else {
                //noinspection ConstantConditions
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        });
        mInitializer.blocks.values().forEach(block -> {
            if (mInitializer.blockStateMaps.containsKey(block)) {
                // IStateMapper
                IStateMapper mapper = mInitializer.blockStateMaps.get(block);
                ModelLoader.setCustomStateMapper(block, mapper);
                // ResourceLocation
                final ArrayList<ModelLocation> triples = mInitializer.blockStates.get(block);

                if (mInitializer.blockStates.containsKey(block)) {
                    Item item = Item.getItemFromBlock(block);
                    Map<IBlockState, ModelResourceLocation> locationMap;
                    //noinspection ConstantConditions
                    ModelResourceLocation defLocation = new ModelResourceLocation(block.getRegistryName(), "inventory");
                    locationMap = mapper.putStateModelLocations(block);
                    if (triples == null || triples.size() == 0) {
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
                if (mInitializer.blockStates.containsKey(block)) {
                    final ArrayList<ModelLocation> triples = mInitializer.blockStates.get(block);
                    Item blockItem = Item.getItemFromBlock(block);
                    for (ModelLocation triple : triples) {
                        ModelLoader.setCustomModelResourceLocation(blockItem, triple.metadata, triple.modelResourceLocation);
                    }
                } else {
                    //noinspection ConstantConditions
                    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
                }
            }
        });
        // 注册流体
        mInitializer.fluidBlocks.forEach((fluid, block) -> ModelLoader.setCustomStateMapper(fluid.getBlock(), new StateMapperBase() {
            @Nonnull
            @Override
            protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
                String bs = mInitializer.fluidBlockStates.get(fluid);
                ResourceLocation location;
                if (bs != null && !bs.isEmpty()) {
                    location = new ResourceLocation(mInitializer.modInfo.id(), bs);
                } else {
                    location = fluid.getBlock().getRegistryName();
                }
                assert location != null;
                return new ModelResourceLocation(location, fluid.getName());
            }
        }));
        // 注册 TESR
        mInitializer.blockTesr.forEach(ClientRegistry::bindTileEntitySpecialRenderer);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void regFluidSpirit(TextureStitchEvent.Pre event) {
        TextureMap textureMap = event.getMap();
        mInitializer.fluidResources.forEach(fluid -> {
            if (fluid.getStill() != null) {
                textureMap.registerSprite(fluid.getStill());
            }

            if (fluid.getFlowing() != null) {
                textureMap.registerSprite(fluid.getFlowing());
            }
        });
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerItemColor(ColorHandlerEvent.Item event) {
        mInitializer.itemColors.forEach((item, iItemColor) ->
                event.getItemColors().registerItemColorHandler((IItemColor) iItemColor, item));
    }
}

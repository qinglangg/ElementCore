package com.elementtimes.elementcore.api.client.event;

import com.elementtimes.elementcore.api.client.ECModElementsClient;
import com.elementtimes.elementcore.api.client.loader.ItemClientLoader;
import com.elementtimes.elementcore.api.common.ECModContainer;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.entity.RenderEntity;
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
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

/**
 * 注解注册
 *
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
public class ForgeBusRegisterClient {

    private ECModContainer mContainer;

    public ForgeBusRegisterClient(ECModContainer container) {
        mContainer = container;
    }
    
    public ECModElements elements() {
        return mContainer.elements();
    }
    
    public ECModElementsClient client() {
        return elements().getClientElements();
    }

    @SubscribeEvent
    public void registerModel(ModelRegistryEvent event) {
        ECUtils.common.runWithModActive(mContainer.mod, this::registerModelFunc, event);
    }

    private void registerModelFunc() {
        // 三方渲染
        if (elements().blockObj) {
            OBJLoader.INSTANCE.addDomain(mContainer.id());
        }
        if (elements().blockB3d) {
            B3DLoader.INSTANCE.addDomain(mContainer.id());
        }
        // 注册渲染
        for (Item item : elements().items) {
            registerItemFunc(item);
        }
        for (Block block : elements().blocks) {
            registerBlockFunc(block);
        }
        // 注册流体
        elements().fluidBlocks.forEach((fluid, block) -> ModelLoader.setCustomStateMapper(fluid.getBlock(), new StateMapperBase() {
            @Nonnull
            @Override
            protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
                String bs = elements().fluidBlockResources == null ? null : elements().fluidBlockResources.get(fluid);
                ResourceLocation location;
                if (bs != null && !bs.isEmpty()) {
                    location = new ResourceLocation(mContainer.id(), bs);
                } else {
                    location = fluid.getBlock().getRegistryName();
                }
                assert location != null;
                return new ModelResourceLocation(location, fluid.getName());
            }
        }));
        // 注册 TESR
        client().blockTesr.forEach(ClientRegistry::bindTileEntitySpecialRenderer);
        // 注册实体渲染
        client().entityRenders.forEach(RenderingRegistry::registerEntityRenderingHandler);
    }

    private void registerItemFunc(Item item) {
        ItemMeshDefinition definition = client().itemMeshDefinition.get(item);
        if (definition != null) {
            ModelLoader.setCustomMeshDefinition(item, definition);
        } else if (client().itemSubModel.containsKey(item)) {
            for (ItemClientLoader.SubModel model : client().itemSubModel.get(item)) {
                ModelLoader.setCustomModelResourceLocation(item, model.metadata, model.modelResourceLocation);
            }
        } else {
            //noinspection ConstantConditions
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }

    private void registerBlockFunc(Block block) {
        IStateMapper mapper = client().blockStateMaps.get(block);
        if (mapper != null) {
            // IStateMapper
            ModelLoader.setCustomStateMapper(block, mapper);
            mapper.putStateModelLocations(block).forEach((state, model) ->
                    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), block.getMetaFromState(state), model));
        } else {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(Objects.requireNonNull(block.getRegistryName()), "inventory"));
        }
    }

    @SubscribeEvent
    public void regFluidSpirit(TextureStitchEvent.Pre event) {
        regFluidSpiritFunc(event);
//        ECUtils.common.runWithModActive(mInitializer.container.mod, () -> regFluidSpiritFunc(event), event);
    }

    private void regFluidSpiritFunc(TextureStitchEvent.Pre event) {
        TextureMap textureMap = event.getMap();
        elements().fluidResources.forEach(fluid -> {
            if (fluid.getStill() != null) {
                textureMap.registerSprite(fluid.getStill());
            }

            if (fluid.getFlowing() != null) {
                textureMap.registerSprite(fluid.getFlowing());
            }
        });
    }

    @SubscribeEvent
    public void registerItemColor(ColorHandlerEvent.Item event) {
        registerItemColorFunc(event);
//        ECUtils.common.runWithModActive(mInitializer.container.mod, () -> registerItemColorFunc(event), event);
    }

    private void registerItemColorFunc(ColorHandlerEvent.Item event) {
        client().itemColors.forEach((color, items) -> {
            if (items.size() > 0) {
                event.getItemColors().registerItemColorHandler(color, items.toArray(new Item[0]));
            }
        });
        client().blockItemColors.forEach((color, blocks) -> {
            if (blocks.size() > 0) {
                event.getItemColors().registerItemColorHandler(color, blocks.toArray(new Block[0]));
            }
        });
    }

    @SubscribeEvent
    public void registerBlockColor(ColorHandlerEvent.Block event) {
        registerBlockColorFunc(event);
//        ECUtils.common.runWithModActive(mInitializer.container.mod, () -> registerBlockColorFunc(event), event);
    }

    private void registerBlockColorFunc(ColorHandlerEvent.Block event) {
        client().blockColors.forEach((color, blocks) -> {
            if (blocks.size() > 0) {
                event.getBlockColors().registerBlockColorHandler(color, blocks.toArray(new Block[0]));
            }
        });
    }
}

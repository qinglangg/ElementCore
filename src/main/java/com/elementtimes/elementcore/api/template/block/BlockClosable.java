package com.elementtimes.elementcore.api.template.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * 拥有 on/off 两种状态的机器
 * @author luqin2007
 */
@SuppressWarnings("unused")
public class BlockClosable extends BlockTileBase {

    public BlockClosable(Properties properties, Supplier<? extends TileEntity> teCreator) {
        super(properties, teCreator);
        init();
    }

    public BlockClosable(Properties properties, TileEntityType<? extends TileEntity> teType) {
        super(properties, teType);
        init();
    }

    public BlockClosable(Properties properties, ResourceLocation name) {
        super(properties, name);
        init();
    }

    public BlockClosable(Properties properties, String namespace, String path) {
        super(properties, namespace, path);
        init();
    }

    private void init() {
        setDefaultState(getStateContainer().getBaseState()
                .with(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH).with(BlockStateProperties.ENABLED, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BlockStateProperties.ENABLED, BlockStateProperties.HORIZONTAL_FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context)
                .with(BlockStateProperties.HORIZONTAL_FACING, context.getPlacementHorizontalFacing());
    }
}

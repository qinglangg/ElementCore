package com.elementtimes.elementcore.api.template.block;

import com.elementtimes.elementcore.api.ECUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

import static com.elementtimes.elementcore.api.template.block.Properties.FACING;
import static com.elementtimes.elementcore.api.template.block.Properties.IS_RUNNING;

/**
 * 拥有 on/off 两种状态的机器
 * @author luqin2007
 */
@SuppressWarnings("unused")
public class BaseClosableMachine<T extends TileEntity> extends BlockTileBase<T> {

    public BaseClosableMachine(Block.Properties properties, Supplier<TileEntity> teCreator) {
        super(properties, teCreator);
        setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH).with(IS_RUNNING, false));
    }

    public BaseClosableMachine(Block.Properties properties, TileEntityType<T> type) {
        super(properties, type);
        setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH).with(IS_RUNNING, false));
    }

    public BaseClosableMachine(Block.Properties properties, Class<T> teClass) {
        super(properties, teClass);
        setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH).with(IS_RUNNING, false));
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        BlockState s2 = placer == null
                ? worldIn.getBlockState(pos)
                : worldIn.getBlockState(pos).with(FACING, placer.getHorizontalFacing());
        TileEntity te = worldIn.getTileEntity(pos);
        ECUtils.block.setBlockState(worldIn, pos, s2, te);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING, IS_RUNNING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return Objects.requireNonNull(super.getStateForPlacement(context))
                .with(FACING, context.getPlacementHorizontalFacing())
                .with(IS_RUNNING, false);
    }
}

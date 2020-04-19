package com.elementtimes.elementcore.api.template.block;

import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

import static com.elementtimes.elementcore.api.template.block.Properties.FACING;
import static com.elementtimes.elementcore.api.template.block.Properties.IS_RUNNING;

/**
 * 拥有 on/off 两种状态的机器
 * @author luqin2007
 */
@SuppressWarnings("unused")
public class BaseClosableMachine<T extends TileEntity> extends BlockTileBase<T> {

    public BaseClosableMachine(Class<T> entityClass, Object mod) {
        super(entityClass, mod);
        setDefaultState(getDefaultState().withProperty(FACING, EnumFacing.NORTH).withProperty(IS_RUNNING, false));
    }

    public BaseClosableMachine(Class<T> entityClass) {
        this(entityClass, null);
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, IS_RUNNING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int facing = state.getValue(FACING).getHorizontalIndex() & 0b0011;
        int burning = state.getValue(IS_RUNNING) ? 0b0100 : 0b0000;
        return facing | burning;
    }

    @Override
    @SuppressWarnings({"NullableProblems", "deprecation"})
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.getHorizontal(meta & 0b0011);
        boolean running = (meta & 0b0100) == 0b0100;
        return super.getStateFromMeta(meta).withProperty(FACING, facing).withProperty(IS_RUNNING, running);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        IBlockState s = worldIn.getBlockState(pos);
        IBlockState s2 = s.withProperty(FACING, placer.getHorizontalFacing());
        worldIn.setBlockState(pos, s2);
    }
}

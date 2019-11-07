package com.elementtimes.elementcore.api.template.block;

import com.elementtimes.elementcore.api.template.block.interfaces.IDismantleBlock;
import com.elementtimes.elementcore.api.template.gui.server.BaseContainer;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.IGuiProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

/**
 * 需要带有 TileEntity 的方块时继承此类
 * 15 25
 * @author KSGFK create in 2019/2/17
 */
public class BlockTileBase<T extends TileEntity> extends Block implements IDismantleBlock {

    private Supplier<TileEntity> mTileEntityCreator;

    private BlockTileBase(Block.Properties properties) {
        super(properties);
        mTileEntityCreator = null;
    }

    public BlockTileBase(Block.Properties properties, Supplier<TileEntity> teCreator) {
        this(properties);
        mTileEntityCreator = teCreator;
    }

    public BlockTileBase(Block.Properties properties, TileEntityType<T> type) {
        this(properties);
        mTileEntityCreator = type::create;
    }

    public BlockTileBase(Block.Properties properties, Class<T> teClass) {
        this(properties);
        for (Constructor<?> c : teClass.getDeclaredConstructors()) {
            if (c.getParameterCount() == 0) {
                mTileEntityCreator = createCreator(c);
                break;
            } else if (c.getParameterCount() == 1 && Block.class.isAssignableFrom(c.getParameterTypes()[0])) {
                mTileEntityCreator = createCreator(c, this);
                break;
            }
        }
    }

    private Supplier<TileEntity> createCreator(Constructor c, Object... params) {
        if (!Modifier.isPublic(c.getModifiers())) {
            c.setAccessible(true);
        }
        final Constructor fc = c;
        mTileEntityCreator = () -> {
            try {
                return (TileEntity) fc.newInstance(params);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        };
        return mTileEntityCreator;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        if (mTileEntityCreator != null) {
            return mTileEntityCreator.get();
        }
        return null;
    }

    @Nonnull
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos,
                                    PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof IGuiProvider) {
            BaseContainer.TE = te;
            BaseContainer.GUI = (IGuiProvider) te;
            if (!worldIn.isRemote) {
                ServerPlayerEntity sp = (ServerPlayerEntity) player;
                NetworkHooks.openGui(sp, (INamedContainerProvider) te);
            }
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        IDismantleBlock.super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }
}

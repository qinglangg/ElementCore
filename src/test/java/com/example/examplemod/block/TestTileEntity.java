package com.example.examplemod.block;

import com.elementtimes.elementcore.api.annotation.ModBlock;
import com.elementtimes.elementcore.api.annotation.ModTileEntity;
import com.elementtimes.elementcore.api.annotation.part.Getter;
import com.elementtimes.elementcore.api.annotation.part.Getter2;
import com.elementtimes.elementcore.api.annotation.part.ItemProps;
import com.elementtimes.elementcore.api.annotation.part.Method;
import com.example.examplemod.ExampleMod;
import com.example.examplemod.capability.TestCapability;
import com.example.examplemod.group.Groups;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ModTileEntity(blocks = @Getter(value = TestTileEntity.class, name = "()getBlock"), newTe = @Method(TestTileEntity.class))
@ModTileEntity.Ter(@Getter2("com.example.examplemod.block.TestTileEntityRenderer"))
public class TestTileEntity extends TileEntity {

    public TestTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public static Block getBlock() {
        return ExampleMod.CONTAINER.elements.generatedBlocks.get(BlockWithTe.class);
    }

    private TestCapability mCapability = new TestCapability();

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == TestCapability.TEST) {
            return LazyOptional.of(() -> (T) mCapability);
        }
        return super.getCapability(cap);
    }

    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    public static class BlockWithTe extends Block {

        public BlockWithTe() {
            super(Properties.create(Material.ROCK));
        }

        @Override
        public boolean hasTileEntity(BlockState state) {
            return true;
        }

        @Nullable
        @Override
        public TileEntity createTileEntity(BlockState state, IBlockReader world) {
            return new TestTileEntity(ExampleMod.CONTAINER.elements.generatedTileEntityTypes.get(TestTileEntity.class));
        }

        @Override
        public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
            if (!worldIn.isRemote) {
                LazyOptional<TestCapability> optional = worldIn.getTileEntity(pos).getCapability(TestCapability.TEST);
                if (optional.isPresent()) {
                    player.sendMessage(new StringTextComponent("Test Capability: " + optional.orElse(null).add()));
                } else {
                    player.sendMessage(new StringTextComponent("No Test Capability"));
                }
            }
            return true;
        }
    }
}

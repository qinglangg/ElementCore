package com.example.examplemod.item;

import com.elementtimes.elementcore.api.annotation.ModItem;
import com.elementtimes.elementcore.api.annotation.enums.ValueType;
import com.elementtimes.elementcore.api.annotation.part.Color;
import com.elementtimes.elementcore.api.annotation.part.Method;
import com.elementtimes.elementcore.api.annotation.tools.ModTooltips;
import com.elementtimes.elementcore.api.utils.CommonUtils;
import com.example.examplemod.group.Groups;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ModItem(value = "count", color = @Color(0xFFAAAAAA))
@ModTooltips(type = ValueType.METHOD, method = @Method(value = BlockCount.class, name = "tooltips"))
public class BlockCount extends Item {

    public BlockCount() {
        super(new Properties().group(Groups.main));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockPos blockPos = context.getPos();
        if (!world.isRemote && player != null) {
            Block block = getBlock(context.getItem());
            if (block == null) {
                BlockState state = world.getBlockState(blockPos);
                setBlock(state.getBlock(), context.getItem());
                player.sendMessage(new StringTextComponent("set " + state.getBlock()));
            } else {
                IChunk chunk = world.getChunk(player.getPosition());
                List<BlockPos> blockPosList = new ArrayList<>();
                for (int x = blockPos.getX() - 100; x <= blockPos.getX() + 100; x++) {
                    for (int z = blockPos.getZ() - 100; z < blockPos.getZ() + 100; z++) {
                        for (int y = 0; y <= chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE, x, z); y++) {
                            BlockPos pos = new BlockPos(x, y, z);
                            BlockState state = world.getBlockState(pos);
                            if (block == state.getBlock()) {
                                blockPosList.add(pos);
                            }
                        }
                    }
                }
                for (BlockPos bp : blockPosList) {
                    player.sendMessage(new StringTextComponent("Pos=" + bp));
                }
            }
        }
        return ActionResultType.PASS;
    }

    public void setBlock(Block block, ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag != null) {
            if (block == null || block == net.minecraft.block.Blocks.AIR) {
                tag.remove("block");
            } else {
                tag.putString("block", block.getRegistryName().toString());
            }
        }
    }

    public Block getBlock(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag != null && tag.contains("block")) {
            ResourceLocation location = new ResourceLocation(tag.getString("block"));
            Block block = ForgeRegistries.BLOCKS.getValue(location);
            return block == net.minecraft.block.Blocks.AIR ? null : block;
        }
        return null;
    }

    public List<ITextComponent> getText(ItemStack stack) {
        Block block = getBlock(stack);
        if (block == null) {
            return Collections.emptyList();
        } else {
            if (CommonUtils.isClient()) {
                return Collections.singletonList(block.getNameTextComponent());
            } else {
                return Collections.singletonList(new TranslationTextComponent(block.getTranslationKey()));
            }
        }
    }

    public static void tooltips(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        event.getToolTip().addAll(((BlockCount) itemStack.getItem()).getText(itemStack));
    }
}

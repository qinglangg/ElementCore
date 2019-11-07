package com.elementtimes.elementcore.common.item;

import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.common.CoreElements;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * 大小锤子
 * 唯一指定调试工具
 * @author luqin2007
 */
public class DebugStick extends Item {

    public static final String TYPE = "type";
    public static final String TYPE_DEBUG = "debug";
    public static final String TYPE_TOOL = "tool";
    public static final String SIDE = "side";
    public static final String SIDE_SERVER = "server";
    public static final String SIDE_CLIENT = "client";

    public static final Supplier<ItemStack> STACK_DEBUG_SERVER = () -> {
        ItemStack stack = new ItemStack(CoreElements.itemDebugger);
        stack.getOrCreateTag().putString(TYPE, TYPE_DEBUG);
        stack.getOrCreateTag().putString(SIDE, SIDE_SERVER);
        return stack;
    };

    public static final Supplier<ItemStack> STACK_DEBUG_CLIENT = () -> {
        ItemStack stack = new ItemStack(CoreElements.itemDebugger);
        stack.getOrCreateTag().putString(TYPE, TYPE_DEBUG);
        stack.getOrCreateTag().putString(SIDE, SIDE_CLIENT);
        return stack;
    };

    public static final Supplier<ItemStack> STACK_TOOL_SERVER = () -> {
        ItemStack stack = new ItemStack(CoreElements.itemDebugger);
        stack.getOrCreateTag().putString(TYPE, TYPE_TOOL);
        stack.getOrCreateTag().putString(SIDE, SIDE_SERVER);
        return stack;
    };

    public DebugStick() {
        super(new Properties().group(CoreElements.main).rarity(Rarity.EPIC));
    }

    @Override
    @Nonnull
    public ItemStack getDefaultInstance() {
        return STACK_DEBUG_SERVER.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        if (isInGroup(group)) {
            items.add(STACK_DEBUG_SERVER.get());
            items.add(STACK_DEBUG_CLIENT.get());
            items.add(STACK_TOOL_SERVER.get());
        }
    }

    @Override
    @Nonnull
    public ITextComponent getDisplayName(@Nonnull ItemStack stack) {
        String[] typeAndServer = getTypeAndServer(stack);
        TranslationTextComponent text = new TranslationTextComponent("item.elementcore.debugstick." + typeAndServer[0] + "." + typeAndServer[1]);
        Style style = text.getStyle();
        switch (typeAndServer[0]) {
            case TYPE_DEBUG:
                switch (typeAndServer[1]) {
                    case SIDE_SERVER:
                        style.setColor(TextFormatting.RED);
                        break;
                    case SIDE_CLIENT:
                        style.setColor(TextFormatting.BLUE);
                        break;
                    default:
                        style.setColor(TextFormatting.GRAY);
                }
                break;
            case TYPE_TOOL:
                switch (typeAndServer[1]) {
                    case SIDE_SERVER:
                        style.setColor(TextFormatting.YELLOW);
                        break;
                    case SIDE_CLIENT:
                    default:
                        style.setColor(TextFormatting.GRAY);
                }
                break;
            default:
                style.setColor(TextFormatting.GRAY);
        }

        return text;
    }

    @Override
    @Nonnull
    public ActionResultType onItemUse(ItemUseContext context) {
        String[] typeAndServer = getTypeAndServer(context.getItem());
        World world = context.getWorld();
        if (world.isRemote == SIDE_CLIENT.equals(typeAndServer[1])) {
            switch (typeAndServer[0]) {
                case TYPE_DEBUG:
                    debug(world, context.getPos(), context.getPlayer());
                    break;
                case TYPE_TOOL:
                    lighting(world, context.getPos());
                    break;
                default:
            }
        }
        return super.onItemUse(context);
    }

    private void debug(World worldIn, BlockPos pos, PlayerEntity player) {
        Block block = worldIn.getBlockState(pos).getBlock();
        TileEntity te = worldIn.getTileEntity(pos);
        if (block == Blocks.AIR) {
            // 空气
            player.sendMessage(new TranslationTextComponent("chat.elementcore.debug.noblock", pos.getX(), pos.getY(), pos.getZ()));
        } else {
            debugTe(te, block, player);
        }
    }

    private void debugTe(TileEntity te, Block block, PlayerEntity player) {
        if (te == null) {
            // 无 te
            player.sendMessage(new TranslationTextComponent("chat.elementcore.debug.noblockte", new ItemStack(block).getDisplayName()));
        } else {
            CompoundNBT nbt = te.write(new CompoundNBT());
            if (nbt.isEmpty()) {
                // 无 nbt
                player.sendMessage(new TranslationTextComponent("chat.elementcore.debug.nonbt", new ItemStack(block).getDisplayName()));
            } else {
                player.sendMessage(block.getNameTextComponent());
                sendDebugChat(player, "", nbt, 0);
                player.sendMessage(new StringTextComponent("============================================================"));
            }
        }
    }

    private void sendDebugChat(PlayerEntity player, String lastKey, INBT nbt, int level) {
        StringBuilder space = new StringBuilder();
        for (int i = 1; i < level; i++) {
            space.append("    ");
        }
        if (nbt instanceof CompoundNBT) {
            player.sendMessage(new StringTextComponent(space.toString() + lastKey));
            ((CompoundNBT) nbt).keySet().forEach(key -> sendDebugChat(player, key, ((CompoundNBT) nbt).get(key), level + 1));
        } else {
            if (nbt instanceof CollectionNBT) {
                for (int i = 0; i < ((CollectionNBT) nbt).size(); i++) {
                    sendDebugChat(player, lastKey + "[" + i + "]", (INBT) ((CollectionNBT) nbt).get(i), level);
                }
            } else if (nbt instanceof ByteNBT) {
                player.sendMessage(new StringTextComponent(space.toString() + lastKey + " = " + ((ByteNBT) nbt).getByte()));
            } else if (nbt instanceof DoubleNBT) {
                player.sendMessage(new StringTextComponent(space.toString() + lastKey + " = " + ((DoubleNBT) nbt).getDouble()));
            } else if (nbt instanceof FloatNBT) {
                player.sendMessage(new StringTextComponent(space.toString() + lastKey + " = " + ((FloatNBT) nbt).getDouble()));
            } else if (nbt instanceof IntNBT) {
                player.sendMessage(new StringTextComponent(space.toString() + lastKey + " = " + ((IntNBT) nbt).getInt()));
            } else if (nbt instanceof LongNBT) {
                player.sendMessage(new StringTextComponent(space.toString() + lastKey + " = " + ((LongNBT) nbt).getLong()));
            } else if (nbt instanceof ShortNBT) {
                player.sendMessage(new StringTextComponent(space.toString() + lastKey + " = " + ((ShortNBT) nbt).getShort()));
            } else if (nbt instanceof StringNBT) {
                player.sendMessage(new StringTextComponent(space.toString() + lastKey + " = " + nbt.getString()));
            } else {
                player.sendMessage(new StringTextComponent(space.toString() + lastKey + " = " + nbt.toString()));
            }
        }
    }

    private void lighting(World worldIn, BlockPos pos) {
        if (worldIn instanceof ServerWorld) {
            LightningBoltEntity bolt = new LightningBoltEntity(worldIn, pos.getX(), pos.getY(), pos.getZ(), false);
            ((ServerWorld) worldIn).addLightningBolt(bolt);
        }
    }

    public static String[] getTypeAndServer(ItemStack stack) {
        String type;
        String side;
        if (stack.hasTag()) {
            CompoundNBT tag = stack.getTag();
            assert tag != null;
            if (tag.contains(TYPE)) {
                type = tag.getString(TYPE);
            } else {
                type = TYPE_DEBUG;
            }
            if (tag.contains(SIDE)) {
                side = tag.getString(SIDE);
            } else {
                side = SIDE_SERVER;
            }
        } else {
            type = TYPE_DEBUG;
            side = SIDE_SERVER;
        }
        return new String[] {type, side};
    }
}

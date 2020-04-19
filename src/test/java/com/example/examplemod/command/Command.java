package com.example.examplemod.command;

import com.elementtimes.elementcore.api.annotation.ModCommand;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.server.command.EnumArgument;

public class Command {

    private static boolean testCmdRuns = false;

    @ModCommand
    public static LiteralArgumentBuilder<CommandSource> testCmd = Commands.literal("ectest").executes(Command::testCmd);

    @ModCommand
    public static LiteralArgumentBuilder<CommandSource> testParameterCmd = Commands.literal("ectestparam")
            .requires(source -> testCmdRuns)
            .then(Commands.argument("target", EntityArgument.player()))
            .then(Commands.argument("hand", EnumArgument.enumArgument(Hand.class)))
            .executes(Command::testCmdParam);

    public static int testCmd(CommandContext<CommandSource> context) {
        context.getSource().sendFeedback(new StringTextComponent("testCmd"), true);
        testCmdRuns = true;
        return 0;
    }

    public static int testCmdParam(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgument.getPlayer(context, "target");
        Hand hand = context.getArgument("hand", Hand.class);
        ItemStack handItem = player.getHeldItem(hand);
        context.getSource().sendFeedback(new StringTextComponent(handItem.toString()), true);
        return 1;
    }
}

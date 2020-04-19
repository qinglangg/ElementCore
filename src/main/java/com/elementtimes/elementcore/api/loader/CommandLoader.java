package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.annotation.ModCommand;
import com.elementtimes.elementcore.api.helper.FindOptions;
import com.elementtimes.elementcore.api.helper.ObjHelper;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;

import java.lang.annotation.ElementType;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author luqin2007
 */
public class CommandLoader {

    public static void load(ECModElements elements) {
        loadCommand(elements);
    }

    private static void loadCommand(ECModElements elements) {
        ObjHelper.stream(elements, ModCommand.class).forEach(data -> {
            AtomicReference<Class<?>> c = new AtomicReference<>();
            FindOptions<Object> options = new FindOptions<>(Object.class, ElementType.TYPE, ElementType.FIELD);
            ObjHelper.find(elements, data, options).ifPresent(obj -> {
                if (obj instanceof CommandNode) {
                    CommandNode<CommandSource> command = (CommandNode<CommandSource>) obj;
                    elements.commands.add(command);
                    if (data.getTargetType() == ElementType.TYPE) {
                        ObjHelper.saveResult(options, command, elements.generatedCommandNodes);
                    }
                } else if (obj instanceof LiteralArgumentBuilder) {
                    LiteralArgumentBuilder<CommandSource> command = (LiteralArgumentBuilder<CommandSource>) obj;
                    elements.commandBuilders.add(command);
                    if (data.getTargetType() == ElementType.TYPE) {
                        ObjHelper.saveResult(options, command, elements.generatedCommandBuilders);
                    }
                }
            });
        });
    }
}

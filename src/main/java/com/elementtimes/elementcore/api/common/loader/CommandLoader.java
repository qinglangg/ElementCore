package com.elementtimes.elementcore.api.common.loader;

import com.elementtimes.elementcore.api.annotation.ModCommand;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import net.minecraft.command.ICommand;

import java.util.Collections;

/**
 * @author luqin2007
 */
public class CommandLoader {

    public static void load(ECModElements elements) {
        ObjHelper.stream(elements, ModCommand.class).forEach(data -> {
            if (!(boolean) data.getAnnotationInfo().getOrDefault("client", false)) {
                ObjHelper.find(elements, ICommand.class, data).ifPresent(command -> {
                    elements.warn("[ModCommand]{} ", command.getName());
                    command.getAliases().forEach(alias -> elements.warn("[ModCommand] -> {}", alias));
                    elements.commands.add(command);
                });
            }
        });
    }
}

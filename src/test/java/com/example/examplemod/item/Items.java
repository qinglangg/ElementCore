package com.example.examplemod.item;

import com.elementtimes.elementcore.api.annotation.ModItem;
import com.elementtimes.elementcore.api.annotation.enums.ValueType;
import com.elementtimes.elementcore.api.annotation.part.Color;
import com.elementtimes.elementcore.api.annotation.part.Getter2;
import com.elementtimes.elementcore.api.annotation.part.Method2;
import com.example.examplemod.group.Groups;
import net.minecraft.item.Item;

public class Items {

    @ModItem(color = @Color(type = ValueType.METHOD, method = @Method2(value = "com.example.examplemod.item.ClientPart", name = "debuggerColor")))
    public static Item DebuggerServer = new Debugger(new Item.Properties().group(Groups.main));

    @ModItem(color = @Color(type = ValueType.OBJECT, object = @Getter2("com.example.examplemod.item.ClientPart$DebuggerColor")))
    public static Item DebuggerClient = new Debugger(new Item.Properties().group(Groups.main));
}

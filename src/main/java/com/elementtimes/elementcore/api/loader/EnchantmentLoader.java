package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.annotation.ModEnchantment;
import com.elementtimes.elementcore.api.annotation.enums.EnchantmentBook;
import com.elementtimes.elementcore.api.annotation.part.Parts;
import com.elementtimes.elementcore.api.helper.FindOptions;
import com.elementtimes.elementcore.api.helper.ObjHelper;
import com.elementtimes.elementcore.api.misc.wrapper.EnchantmentBookWrapper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemGroup;

import java.lang.annotation.ElementType;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class EnchantmentLoader {

    public static void load(ECModElements elements) {
        loadEnchantment(elements);
        loadBook(elements);
    }

    private static void loadEnchantment(ECModElements elements) {
        ObjHelper.stream(elements, ModEnchantment.class).forEach(data -> {
            AtomicReference<Class<?>> c = new AtomicReference<>();
            FindOptions<Enchantment> option = new FindOptions<>(Enchantment.class, ElementType.FIELD, ElementType.TYPE);
            ObjHelper.find(elements, data, option).ifPresent(enchantment -> {
                String regName = ObjHelper.getDefault(data);
                ObjHelper.setRegisterName(enchantment, regName, data, elements);
                elements.enchantments.add(enchantment);
                ObjHelper.saveResult(option, elements.generatedEnchantments);
            });
        });
    }

    private static void loadBook(ECModElements elements) {
        ObjHelper.stream(elements, ModEnchantment.Book.class).forEach(data -> {
            Enchantment enchantment;
            if (data.getTargetType() == ElementType.TYPE) {
                Optional<Class<?>> classOpt = ObjHelper.findClass(elements, data.getClassType());
                if (classOpt.isPresent()) {
                    enchantment = elements.generatedEnchantments.get(classOpt.get());
                } else {
                    enchantment = null;
                }
            } else {
                enchantment = ObjHelper.find(elements, data, new FindOptions<>(Enchantment.class, ElementType.FIELD)).orElse(null);
            }
            if (enchantment != null) {
                EnchantmentBook book = ObjHelper.getEnum(EnchantmentBook.class, ObjHelper.getDefault(data), EnchantmentBook.ALL);
                Parts.getter(elements, data.getAnnotationData().get("groups")).<ItemGroup>get()
                        .ifPresent(tab -> elements.enchantmentBooks.add(new EnchantmentBookWrapper(tab, book, enchantment)));
            }
        });
    }
}

package com.elementtimes.elementcore.api.common.loader;

import com.elementtimes.elementcore.api.annotation.ModEnchantment;
import com.elementtimes.elementcore.api.annotation.enums.EnchantmentBook;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentLoader {

    public static void load(ECModElements elements) {
        loadEnchantment(elements);
        loadBook(elements);
    }

    private static void loadEnchantment(ECModElements elements) {
        ObjHelper.stream(elements, ModEnchantment.class).forEach(data -> {
            ObjHelper.findClass(elements, data.getClassName()).ifPresent(clazz -> {
                Map<String, Object> info = data.getAnnotationInfo();
                Enchantment enchantment = newEnchantment(elements, data.getClassName(), data.getObjectName(), ObjHelper.getDefault(data), (String) info.get("name"));
                elements.warn("[ModEnchantment]{} rarity={}, maxLevel={}", enchantment.getName(), enchantment.getRarity(), enchantment.getMaxLevel());
            });
        });
    }

    private static void loadBook(ECModElements elements) {
        ObjHelper.stream(elements, ModEnchantment.Book.class).forEach(data -> {
            ObjHelper.find(elements, Enchantment.class, data).ifPresent(enchantment -> {
                Map<String, Object> info = data.getAnnotationInfo();
                String tab = (String) info.getOrDefault("creativeTabKey", "tools");
                ModAnnotation.EnumHolder bookHolder = (ModAnnotation.EnumHolder) info.get("book");
                EnchantmentBook book;
                if (bookHolder == null) {
                    book = EnchantmentBook.ALL;
                } else {
                    book = EnchantmentBook.valueOf(bookHolder.getValue());
                }
                elements.warn("[ModEnchantment.ModBook]load {} book, level {}", enchantment.getName(), book);
                elements.enchantmentBooks.add(new EnchantmentBookWrapper(elements, tab, book, enchantment));
            });
        });
    }

    public static Enchantment newEnchantment(ECModElements elements, String className, String objectName,
                                             String registerName, String enchantmentName) {
        Enchantment enchantment = ObjHelper.findClass(elements, className).
                flatMap(aClass -> ECUtils.reflect.get(aClass, objectName, null, Enchantment.class, elements)).
                orElseGet(() -> new Enchantment(Enchantment.Rarity.COMMON, EnumEnchantmentType.ALL, new EntityEquipmentSlot[0]) {});
        String register = StringUtils.isNullOrEmpty(registerName) ? objectName : registerName;
        if (enchantment.getRegistryName() == null) {
            if (register.contains(":")) {
                enchantment.setRegistryName(new ResourceLocation(register.toLowerCase()));
            } else {
                enchantment.setRegistryName(new ResourceLocation(elements.container.id(), register.toLowerCase()));
            }
        }
        if ("enchantment.null".equals(enchantment.getName())) {
            if (StringUtils.isNullOrEmpty(enchantmentName)) {
                enchantment.setName(elements.container.id() + "." + objectName.toLowerCase());
            } else {
                enchantment.setName(enchantment.getRegistryName().getResourceDomain() + "." + enchantmentName.toLowerCase());
            }
        }
        elements.enchantments.add(enchantment);
        return enchantment;
    }

    public static class EnchantmentBookWrapper {
        public final String tabKey;
        public final EnchantmentBook type;
        public final ECModElements elements;
        public final Enchantment enchantment;
        private CreativeTabs tab = null;
        private List<ItemStack> enchantedBooks = new ArrayList<>();
        public EnchantmentBookWrapper(ECModElements elements, String tabKey, EnchantmentBook type, Enchantment enchantment) {
            this.tabKey = tabKey;
            this.type = type;
            this.elements = elements;
            this.enchantment = enchantment;
        }

        public void apply(CreativeTabs tabs, NonNullList<ItemStack> stacks) {
            if (tab == null) {
                ObjHelper.findTab(elements, tabKey).ifPresent(t -> tab = t);
            }
            if (tabs == tab) {
                if (enchantedBooks.isEmpty()) {
                    ItemStack stack;
                    Map<Enchantment, Integer> map;
                    switch (type) {
                        case ALL:
                            for (int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); i++) {
                                stack = new ItemStack(Items.ENCHANTED_BOOK);
                                map = new HashMap<>(1);
                                map.put(enchantment, i);
                                EnchantmentHelper.setEnchantments(map, stack);
                                enchantedBooks.add(stack);
                            }
                            break;
                        case MAX:
                            stack = new ItemStack(Items.ENCHANTED_BOOK);
                            map = new HashMap<>(1);
                            map.put(enchantment, enchantment.getMaxLevel());
                            EnchantmentHelper.setEnchantments(map, stack);
                            enchantedBooks.add(stack);
                            break;
                        default:
                            stack = new ItemStack(Items.ENCHANTED_BOOK);
                            map = new HashMap<>(1);
                            map.put(enchantment, enchantment.getMinLevel());
                            EnchantmentHelper.setEnchantments(map, stack);
                            enchantedBooks.add(stack);
                    }
                }
                stacks.addAll(enchantedBooks);
            }
        }
    }

}

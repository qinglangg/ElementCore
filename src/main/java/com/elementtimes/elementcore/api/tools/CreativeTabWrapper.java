package com.elementtimes.elementcore.api.tools;

import com.elementtimes.elementcore.api.common.ECUtils;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 用于对原版创造模式物品栏的替换
 * 需要使用 apply 方法设置
 * @author luqin2007
 */
@SuppressWarnings("unused")
public class CreativeTabWrapper extends CreativeTabs {

    private final CreativeTabs tab;
    private final List<Predicate<ItemStack>> itemPredicates = new LinkedList<>();

    public static CreativeTabWrapper apply(CreativeTabs tab, String label) {
        CreativeTabWrapper newTab = new CreativeTabWrapper(tab, label);
        // 替换成员
        CREATIVE_TAB_ARRAY[tab.getTabIndex()] = newTab;
        for (Field field : CreativeTabs.class.getDeclaredFields()) {
            try {
                Object o = field.get(null);
                if (o == tab) {
                    field.setAccessible(true);
                    ECUtils.reflect.setFinalField(field, newTab);
                    break;
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return newTab;
    }

    private CreativeTabWrapper(CreativeTabs tab, String label) {
        super(tab.getTabIndex(), label);
        this.tab = tab;
    }

    public CreativeTabWrapper addPredicate(Predicate<ItemStack> predicate) {
        itemPredicates.add(predicate);
        return this;
    }

    // 重写方法 ==================================================================

    @SideOnly(Side.CLIENT)
    @Override
    public void displayAllRelevantItems(@Nonnull NonNullList<ItemStack> items) {
        tab.displayAllRelevantItems(items);
        items.removeIf(itemStack -> {
            for (Predicate<ItemStack> predicate : itemPredicates) {
                if (!predicate.test(itemStack)) {
                    return true;
                }
            }
            return false;
        });
    }

    // MC 原版方法 ===============================================================

    @Override
    @SideOnly(Side.CLIENT)
    public int getTabIndex() {
        return tab.getTabIndex();
    }

    @Override
    @Nonnull
    public CreativeTabs setBackgroundImageName(@Nonnull String texture) {
        tab.setBackgroundImageName(texture);
        return this;
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Nonnull
    public String getTabLabel() {
        return tab.getTabLabel();
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Nonnull
    public String getTranslatedTabLabel() {
        return tab.getTranslatedTabLabel();
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Nonnull
    public ItemStack getIconItemStack() {
        return tab.getIconItemStack();
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Nonnull
    public ItemStack getTabIconItem() {
        return tab.getTabIconItem();
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Nonnull
    public String getBackgroundImageName() {
        return tab.getBackgroundImageName();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean drawInForegroundOfTab() {
        return tab.drawInForegroundOfTab();
    }

    @Override
    @Nonnull
    public CreativeTabs setNoTitle() {
        tab.setNoTitle();
        return this;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldHidePlayerInventory() {
        return tab.shouldHidePlayerInventory();
    }

    @Override
    @Nonnull
    public CreativeTabs setNoScrollbar() {
        tab.setNoScrollbar();
        return this;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getTabColumn() {
        return tab.getTabColumn();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isTabInFirstRow() {
        return tab.isTabInFirstRow();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isAlignedRight() {
        return tab.isAlignedRight();
    }

    @Override
    @Nonnull
    public EnumEnchantmentType[] getRelevantEnchantmentTypes() {
        return tab.getRelevantEnchantmentTypes();
    }

    @Override
    @Nonnull
    public CreativeTabs setRelevantEnchantmentTypes(@Nonnull EnumEnchantmentType... types) {
        tab.setRelevantEnchantmentTypes(types);
        return this;
    }

    @Override
    public boolean hasRelevantEnchantmentType(@Nullable EnumEnchantmentType enchantmentType) {
        return tab.hasRelevantEnchantmentType(enchantmentType);
    }

    @Override
    public int getTabPage() {
        return tab.getTabPage();
    }

    @Override
    public boolean hasSearchBar() {
        return tab.hasSearchBar();
    }

    @Override
    public int getSearchbarWidth() {
        return tab.getSearchbarWidth();
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Nonnull
    public net.minecraft.util.ResourceLocation getBackgroundImage() {
        return tab.getBackgroundImage();
    }
}

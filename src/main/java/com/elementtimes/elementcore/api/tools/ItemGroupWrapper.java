package com.elementtimes.elementcore.api.tools;

import com.elementtimes.elementcore.api.ECUtils;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
public class ItemGroupWrapper extends ItemGroup {

    private final ItemGroup group;
    private final List<Predicate<ItemStack>> itemPredicates = new LinkedList<>();

    public static ItemGroupWrapper apply(ItemGroup group, String label) {
        ItemGroupWrapper newTab = new ItemGroupWrapper(group, label);
        // 替换成员
        GROUPS[group.getIndex()] = newTab;
        for (Field field : ItemGroup.class.getDeclaredFields()) {
            try {
                Object o = field.get(null);
                if (o == group) {
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

    private ItemGroupWrapper(ItemGroup group, String label) {
        super(group.getIndex(), label);
        this.group = group;
    }

    public ItemGroupWrapper addPredicate(Predicate<ItemStack> predicate) {
        itemPredicates.add(predicate);
        return this;
    }

    // 重写方法 ==================================================================

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fill(@Nonnull NonNullList<ItemStack> items) {
        group.fill(items);
        items.removeIf(itemStack -> {
            for (Predicate<ItemStack> predicate : itemPredicates) {
                if (!predicate.test(itemStack)) {
                    return true;
                }
            }
            return false;
        });
    }


    // 原版方法 ====================================================================

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getIndex() {
        return group.getIndex();
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public String getTabLabel() {
        return group.getTabLabel();
    }

    @Nonnull
    @Override
    public String getPath() {
        return group.getPath();
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public String getTranslationKey() {
        return group.getTranslationKey();
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public ItemStack getIcon() {
        return group.getIcon();
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public ItemStack createIcon() {
        return group.createIcon();
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public String getBackgroundImageName() {
        return group.getBackgroundImageName();
    }

    @Nonnull
    @Override
    public ItemGroup setBackgroundImageName(@Nonnull String texture) {
        group.setBackgroundImageName(texture);
        return this;
    }

    @Nonnull
    @Override
    public ItemGroup setTabPath(@Nonnull String pathIn) {
        group.setTabPath(pathIn);
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean drawInForegroundOfTab() {
        return group.drawInForegroundOfTab();
    }

    @Nonnull
    @Override
    public ItemGroup setNoTitle() {
        group.setNoTitle();
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasScrollbar() {
        return group.hasScrollbar();
    }

    @Nonnull
    @Override
    public ItemGroup setNoScrollbar() {
        group.setNoScrollbar();
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getColumn() {
        return group.getColumn();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isOnTopRow() {
        return group.isOnTopRow();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isAlignedRight() {
        return group.isAlignedRight();
    }

    @Nonnull
    @Override
    public EnchantmentType[] getRelevantEnchantmentTypes() {
        return group.getRelevantEnchantmentTypes();
    }

    @Nonnull
    @Override
    public ItemGroup setRelevantEnchantmentTypes(@Nonnull EnchantmentType... types) {
        group.setRelevantEnchantmentTypes(types);
        return this;
    }

    @Override
    public boolean hasRelevantEnchantmentType(@Nullable EnchantmentType enchantmentType) {
        return group.hasRelevantEnchantmentType(enchantmentType);
    }

    @Override
    public int getTabPage() {
        return group.getTabPage();
    }

    @Override
    public boolean hasSearchBar() {
        return group.hasScrollbar();
    }

    @Override
    public int getSearchbarWidth() {
        return group.getSearchbarWidth();
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public net.minecraft.util.ResourceLocation getBackgroundImage() {
        return group.getBackgroundImage();
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public net.minecraft.util.ResourceLocation getTabsImage() {
        return group.getTabsImage();
    }

    @Override
    public int getLabelColor() {
        return group.getLabelColor();
    }

    @Override
    public int getSlotColor() {
        return group.getSlotColor();
    }

}

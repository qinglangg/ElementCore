package com.elementtimes.elementcore.api.book;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * 任务书
 * @author luqin2007
 */
public interface IBook {

    /**
     * 书籍 id，同时也是书籍对应物品的默认 id
     * @return id
     */
    @Nonnull
    ResourceLocation getId();

    /**
     * 书籍物品名，若返回 null，则使用物品默认名
     * @return 物品名
     */
    @Nullable
    ITextComponent getBookName(ItemStack bookStack);

    /**
     * 书籍物品 ItemStack 附加 Tooltips
     * @return Tooltips
     */
    @Nullable
    @SideOnly(Side.CLIENT)
    List<ITextComponent> getTooltips(ItemStack bookStack);

    /**
     * 获取书籍物品，可直接 new 一个 ItemBook 类，因为接口不能使用 this 且可能需要对物品进行诸如创造物品栏等设定，因此有了这个方法
     * @return 书籍
     */
    @Nonnull
    ItemBook createItem();

    /**
     * 为书籍增加合成表
     * @return 所有可用合成表
     */
    @Nullable
    IRecipe createRecipe(ItemBook book);

    /**
     * 获取正文
     * @return 书籍正文
     */
    ArrayList<Page> getPages();

    default void resume() {
        ListIterator<Page> iterator = getPages().listIterator();
        while (iterator.hasNext()) {
            iterator.next().resume(iterator);
        }
    }
}

package com.elementtimes.elementcore.api.book;

import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.api.common.net.GuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class ItemBook extends Item implements IGuiHandler {

    protected IBook mBook;
    protected int mId;

    public ItemBook(IBook book) {
        mBook = book;
        mId = GuiHandler.getNextId(this);
    }

    public IBook getBook() {
        return mBook;
    }

    @Override
    @Nonnull
    public String getItemStackDisplayName(ItemStack stack) {
        ITextComponent bookName = mBook.getBookName(stack);
        if (bookName != null) {
            return bookName.getFormattedText();
        }
        return super.getItemStackDisplayName(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        List<ITextComponent> tooltips = mBook.getTooltips(stack);
        if (tooltips != null && !tooltips.isEmpty()) {
            tooltip.addAll(tooltips.stream().map(ITextComponent::getFormattedText).collect(Collectors.toList()));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand handIn) {
        player.openGui(ElementCore.instance(), mId, worldIn, (int) player.posX, (int) player.posY, (int) player.posZ);
        return super.onItemRightClick(worldIn, player, handIn);
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new BookContainer(mBook);
    }

    @Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BookContainer container = (BookContainer) getServerGuiElement(ID, player, world, x, y, z);
        if (player.world.isRemote) {
            return new com.elementtimes.elementcore.api.book.screen.BookGuiContainer(container);
        }
        return container;
    }
}

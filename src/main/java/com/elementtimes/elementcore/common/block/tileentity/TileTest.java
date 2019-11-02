package com.elementtimes.elementcore.common.block.tileentity;

import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.api.annotation.ModInvokeStatic;
import com.elementtimes.elementcore.api.template.tileentity.BaseTileEntity;
import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import com.elementtimes.elementcore.api.template.tileentity.recipe.IngredientPart;
import com.elementtimes.elementcore.api.template.tileentity.recipe.MachineRecipeHandler;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

/**
 * 电炉
 * @author luqin2007
 */
@ModInvokeStatic("init")
public class TileTest extends BaseTileEntity {

    public static MachineRecipeHandler RECIPE;

    public static void init() {
        RECIPE = new MachineRecipeHandler(1, 1, 0, 0)
                .newRecipe()
                .addCost(10)
                .addItemInput(IngredientPart.forItem(Blocks.IRON_ORE, 0))
                .addItemOutput(IngredientPart.forItem(Items.IRON_INGOT, 1).withProbability(0.5f))
                .endAdd();
    }

    public TileTest() {
        super(10000, 1, 1);
    }

    @Nonnull
    @Override
    public MachineRecipeHandler getRecipes() {
        return RECIPE;
    }

    @Override
    public int getEnergyTick() {
        return 1;
    }

    @Override
    public void applyConfig() {
        setEnergyTransfer(10);
    }

    @Override
    @Nonnull
    public Slot[] getSlots() {
        return new Slot[]{new SlotItemHandler(this.getItemHandler(SideHandlerType.INPUT), 0, 56, 30), new SlotItemHandler(this.getItemHandler(SideHandlerType.OUTPUT), 0, 110, 30)};
    }

    @Override
    public ResourceLocation getBackground() {
        return new ResourceLocation(ElementCore.instance().container.id(), "textures/gui/5.png");
    }

    @Override
    public GuiSize getSize() {
        return GUI_SIZE_176_156_74.copy().withTitleY(60)
                .withProcess(80, 30, 0, 156, 24, 17)
                .withEnergy(43, 55, 24, 156, 90, 4);
    }

    @Override
    public String getTitle() {
//        return ElementCore.Blocks.test.getLocalizedName();
        return "";
    }

    @Override
    public int getGuiId() {
        return 1;
    }

    @Override
    public void update() {
        update(this);
    }
}


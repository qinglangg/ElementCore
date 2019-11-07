package com.elementtimes.elementcore.common.block.tileentity;

import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.api.annotation.ModInvoke;
import com.elementtimes.elementcore.api.annotation.ModTileEntity;
import com.elementtimes.elementcore.api.template.tileentity.BaseTileEntity;
import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import com.elementtimes.elementcore.api.template.tileentity.recipe.IngredientPart;
import com.elementtimes.elementcore.api.template.tileentity.recipe.MachineRecipeHandler;
import com.elementtimes.elementcore.common.CoreElements;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

/**
 * 电炉
 * @author luqin2007
 */
public class TileMachine extends BaseTileEntity {

    @ModTileEntity.TileEntityType
    public static TileEntityType<TileMachine> TILE_MACHINE = TileEntityType.Builder
            .create(TileMachine::new, CoreElements.blockMachine)
            .build(null);

    public static MachineRecipeHandler RECIPE;

    @ModInvoke
    public static void init() {
        RECIPE = new MachineRecipeHandler(1, 1, 0, 0)
                .newRecipe()
                .addCost(10)
                .addItemInput(IngredientPart.forItem(new ResourceLocation("elementcore:core"), 1))
                .addItemOutput(IngredientPart.forItem(new ResourceLocation("elementcore:glass"), 1))
                .endAdd();
    }

    public TileMachine() {
        super(TILE_MACHINE, 10000, 1, 1);
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
        return new ResourceLocation(ElementCore.INSTANCE.container.id(), "textures/gui/5.png");
    }

    @Override
    public GuiSize getSize() {
        return GUI_SIZE_176_156_74.copy().withTitleY(60)
                .withProcess(80, 30, 0, 156, 24, 17)
                .withEnergy(43, 55, 24, 156, 90, 4);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent(CoreElements.blockMachine.getTranslationKey());
    }

    @Override
    public int getGuiId() {
        return 1;
    }

    @Override
    public void tick() {
        update(this);
    }
}


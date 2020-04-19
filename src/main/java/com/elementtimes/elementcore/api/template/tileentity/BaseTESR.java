package com.elementtimes.elementcore.api.template.tileentity;

import com.elementtimes.elementcore.api.template.tileentity.interfaces.ITileTESR;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BaseTESR extends TileEntitySpecialRenderer<TileEntity> {

    @Override
    public void render(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        if (te instanceof ITileTESR) {

            for (ITileTESR.RenderObject stack : ((ITileTESR) te).getRenderItems().values()) {
                if (stack == null || stack == ITileTESR.EMPTY || !stack.isRender()) {
                    continue;
                }
                GlStateManager.pushMatrix();
                for (ITileTESR.RenderObjectTransformation transformation : stack.transformations) {
                    switch (transformation.type) {
                        case 0:
                            GlStateManager.translate(transformation.params[0], transformation.params[1], transformation.params[2]);
                            break;
                        case 1:
                            GlStateManager.scale(transformation.params[0], transformation.params[1], transformation.params[2]);
                            break;
                        default:
                            GlStateManager.rotate((float) transformation.params[3], (float) transformation.params[0], (float) transformation.params[1], (float) transformation.params[2]);
                    }
                }
                renderItem.renderItem(stack.obj, ItemCameraTransforms.TransformType.GROUND);
                GlStateManager.popMatrix();
            }
        }
    }

//    @Override
//    public void renderTileEntityFast(@Nonnull TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
//        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
//        if (te instanceof ITileTESR) {
//            for (ITileTESR.RenderObject stack : ((ITileTESR) te).getRenderItems()) {
//                if (stack == null || stack == ITileTESR.EMPTY || !stack.isRender()) {
//                    continue;
//                }
//                GlStateManager.pushMatrix();
//                double ix = stack.vector.x;
//                double iy = stack.vector.y;
//                double iz = stack.vector.z;
//
//                GlStateManager.translate(x + ix, y + iy, z + iz);
//                GlStateManager.scale(3, 3, 3);
//
//                renderItem.renderItem(stack.obj, ItemCameraTransforms.TransformType.GROUND);
//
//                GlStateManager.popMatrix();
//            }
//        }
//    }
}

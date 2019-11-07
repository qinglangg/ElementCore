package com.elementtimes.elementcore.api.template.tileentity;

import com.elementtimes.elementcore.api.template.tileentity.interfaces.ITileTer;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BaseTsr extends TileEntityRenderer<TileEntity> {

    @Override
    public void render(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
        super.render(te, x, y, z, partialTicks, destroyStage);
        ItemRenderer renderItem = Minecraft.getInstance().getItemRenderer();
        if (te instanceof ITileTer) {

            for (ITileTer.RenderObject stack : ((ITileTer) te).getRenderItems().values()) {
                if (stack == null || stack == ITileTer.EMPTY || !stack.isRender()) {
                    continue;
                }
                GlStateManager.pushMatrix();
                // translate
                double tx = x, ty = y, tz = z;
                boolean needTranslate = false;
                for (Vec3d translate : stack.translates) {
                    tx += translate.x;
                    ty += translate.y;
                    tz += translate.z;
                    needTranslate = true;
                }
                if (needTranslate) {
                    GlStateManager.translated(tx, ty, tz);
                }
                // scale
                for (Vec3d scale : stack.scales) {
                    GlStateManager.scaled(scale.x, scale.y, scale.z);
                }
                // rotate
                for (int i = 0; i < stack.rotates.length; i++) {
                    Vec3d rotate = stack.rotates[i];
                    GlStateManager.rotated(stack.rotateAngles[i], (float) rotate.x, (float) rotate.y, (float) rotate.z);
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

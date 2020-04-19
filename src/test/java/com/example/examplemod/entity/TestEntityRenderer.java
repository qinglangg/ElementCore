package com.example.examplemod.entity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.CowModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

// 就用牛的吧
@OnlyIn(Dist.CLIENT)
public class TestEntityRenderer extends MobRenderer<TestEntity, EntityModel<TestEntity>> {

    protected TestEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager, new CowModel<>(), 0.7F);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(TestEntity entity) {
        return new ResourceLocation("textures/entity/cow/cow.png");
    }
}

package com.example.examplemod.entity;

import com.elementtimes.elementcore.api.annotation.ModEntity;
import com.elementtimes.elementcore.api.annotation.part.*;
import com.example.examplemod.ExampleMod;
import com.example.examplemod.group.Groups;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

@ModEntity(create = @Method(TestEntity.class), renderer = @Method2("com.example.examplemod.entity.TestEntityRenderer"))
@ModEntity.Egg(primary = 0xFFAAAAAA, secondary = 0xFF000000,
        prop = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
@ModEntity.Spawn(@EntitySpawn(biome = @Biome("plain")))
public class TestEntity extends AnimalEntity {

    public TestEntity(World worldIn) {
        this((EntityType<? extends AnimalEntity>) ExampleMod.CONTAINER.elements.generatedEntityTypes.get(TestEntity.class), worldIn);
    }

    public TestEntity(EntityType<? extends AnimalEntity> type, World worldIn) {
        super(type, worldIn);
        System.out.println("spawn " + this);
    }

    @Nullable
    @Override
    public AgeableEntity createChild(AgeableEntity ageable) {
        return new TestEntity(world);
    }
}

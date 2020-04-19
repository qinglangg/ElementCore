package com.example.examplemod.block;

import com.elementtimes.elementcore.api.Vanilla;
import com.elementtimes.elementcore.api.annotation.ModBlock;
import com.elementtimes.elementcore.api.annotation.enums.ValueType;
import com.elementtimes.elementcore.api.annotation.part.*;
import com.example.examplemod.group.Groups;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.placement.HeightWithChanceConfig;
import net.minecraft.world.gen.placement.Placement;

/**
 * 测试 ModBlock.Features 注解
 * @see ModBlock.Features
 */
public class TestModBlockFeature {

    /**
     * VALUE 测试
     */
    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    @ModBlock.Features(@Feature(biome = @Biome(value = "plains"), decoration = GenerationStage.Decoration.VEGETAL_DECORATION,
            feature = @Getter(FeatureLikeBamboo.class), featureConfig = @Method(value = FeatureLikeBamboo.class, name = "config"),
            placement = @Getter(value = Vanilla.Placements.class, name = "COUNT_CHANCE_HEIGHTMAP"),
            placementConfig = @Method(value = FeatureLikeBamboo.class, name = "placement")))
    public static Block blockFeature = new Block(Block.Properties.create(Material.ROCK));

    /**
     * OBJECT 测试
     */
    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    @ModBlock.Features(@Feature(type = ValueType.OBJECT,
            biome = @Biome(value = "plains"), decoration = GenerationStage.Decoration.VEGETAL_DECORATION,
            object = @Getter(value = FeatureLikeBamboo.class, name = "()feature")))
    public static Block blockFeatureObject = new Block(Block.Properties.create(Material.ROCK));

    /**
     * METHOD 测试
     */
    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    @ModBlock.Features(@Feature(type = ValueType.METHOD,
            biome = @Biome(value = "plains"), decoration = GenerationStage.Decoration.VEGETAL_DECORATION,
            method = @Method(value = FeatureLikeBamboo.class, name = "toFeature")))
    public static Block blockFeatureMethod = new Block(Block.Properties.create(Material.ROCK));

    /**
     * 多 Feature 测试
     */
    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    @ModBlock.Features({
            @Feature(biome = @Biome(value = "plains"), decoration = GenerationStage.Decoration.VEGETAL_DECORATION,
                    feature = @Getter(FeatureLikeBamboo.class), featureConfig = @Method(value = FeatureLikeBamboo.class, name = "config"),
                    placement = @Getter(value = Vanilla.Placements.class, name = "COUNT_CHANCE_HEIGHTMAP"),
                    placementConfig = @Method(value = FeatureLikeBamboo.class, name = "placement")),
            @Feature(biome = @Biome(type = ValueType.OBJECT, object = @Getter(value = Vanilla.Biomes.class, name = "NETHER")), decoration = GenerationStage.Decoration.VEGETAL_DECORATION,
                    feature = @Getter(FeatureLikeBamboo.class), featureConfig = @Method(value = FeatureLikeBamboo.class, name = "config"),
                    placement = @Getter(value = Vanilla.Placements.class, name = "COUNT_CHANCE_HEIGHTMAP"),
                    placementConfig = @Method(value = FeatureLikeBamboo.class, name = "placement"))
    })
    public static Block blockFeatures = new Block(Block.Properties.create(Material.ROCK));

    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    public static Block blockFeature0 = new FeatureBlock();

    @ModBlock(item = @ItemProps(group = @Getter(value = Groups.class, name = "main")))
    public static Block blockFeature1 = new FeatureBlock();

    /**
     * 对 class 的 Feature 测试
     */
    @ModBlock.Features(@Feature(biome = @Biome(type = ValueType.OBJECT, object = @Getter(value = Vanilla.Biomes.class, name = "NETHER")), decoration = GenerationStage.Decoration.VEGETAL_DECORATION,
            feature = @Getter(value = Vanilla.Features.class, name = "NETHER_BRIDGE"),
            featureConfig = @Method(value = Vanilla.Features.class, name = "noConfig"),
            placement = @Getter(value = Vanilla.Placements.class, name = "COUNT_CHANCE_HEIGHTMAP"),
            placementConfig = @Method(value = TestModBlockFeature.class, name = "placement")))
    public static class FeatureBlock extends Block {
        public FeatureBlock() {
            super(Properties.create(Material.ROCK));
        }
    }

    public static HeightWithChanceConfig placement(Block block, Placement<HeightWithChanceConfig> placement) {
        return new HeightWithChanceConfig(10, .3f);
    }
}

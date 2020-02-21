package com.elementtimes.elementcore.api.annotation;

import com.elementtimes.elementcore.api.annotation.enums.FluidBlockType;
import com.elementtimes.elementcore.api.annotation.part.Getter;
import com.elementtimes.elementcore.api.annotation.part.Method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 流体注册
 * @author luqin2007
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModFluid {

    /**
     * 修改 unlocalizedName
     * 若留空，则不会修改，会用 Fluid 构造中传入的 fluidName 值
     * 会使用 toLowerCase 处理
     * @return unlocalizedName
     */
    String unlocalizedName() default "";

    /**
     * 使用该参数请务必确保有 mod 开启万能桶
     * @return 是否添加对应的桶
     */
    boolean bucket() default true;

    /**
     * 设置流体密度，同时自动设置gaseous属性
     * @return 流体密度。水为1000，熔岩3000，小于0为气体
     */
    int density() default 1000;

    /**
     * @return 流体桶所在创造列表
     */
    String creativeTabKey() default "";

    /**
     * 不使用 FluidBlock 时，默认不会再加载 Fluid 的材质。当此参数为 true 时，仍会加载对应材质
     * 此时 加载的材质通常用于绘制其他 gui 界面
     * @return 是否仍要加载流体材质
     */
    boolean loadTexture() default true;

    /**
     * 流体方块
     * 使用默认的 BlockFluidClassic 或 BlockFluidFinite
     * 使用 Water 或 Lava 的 Material
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface FluidBlock {

        /**
         * RegisterName，代表方块注册名
         * 当方块 registerName 与变量名相同（忽略大小写，使用 toLowerCase 处理）时，可省略
         * @return registerName
         */
        String registerName() default "";

        /**
         * UnlocalizedName，用于获取方块显示名
         * 当 unlocalizedName 与变量名相同（忽略大小写，使用 toLowerCase 处理）时，可省略
         * @return unlocalizedName
         */
        String unlocalizedName() default "";

        /**
         * @return 创造物品栏
         */
        String creativeTabKey() default "";

        /**
         * 创建默认流体方块
         * 当 className/id 参数非空时忽略
         * XXXLava 表示使用 Lava 材质，默认 Water
         * @return 方块类型
         */
        FluidBlockType type() default FluidBlockType.Classic;

        /**
         * 由于 Forge 读取流体方块材质根据流体 name 属性区分，因此可以多个流体公用一个 blockstate json 文件
         * 此属性可设置使用的 blockstate 文件名（不包括扩展名）。
         * 留空则仍使用方块的 RegistryName
         * @return blockstate 资源名
         */
        String resource() default "fluids";
    }

    /**
     * 流体方块
     * 使用自定义的方块
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface FluidBlockObj {

        /**
         * @return 获取的 Block 类型方块
         */
        Getter value();

        /**
         * 由于 Forge 读取流体方块材质根据流体 name 属性区分，因此可以多个流体公用一个 blockstate json 文件
         * 此属性可设置使用的 blockstate 文件名（不包括扩展名）。
         * 留空则仍使用方块的 RegistryName
         * @return blockstate 资源名
         */
        String resource() default "fluids";
    }

    /**
     * 流体方块
     * 使用自定义的方块，通过方法获取
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface FluidBlockFunc {

        /**
         * 获取方块的方法
         * 参数
         *  Fluid：对应流体
         * 返回值
         *  Block：流体方块
         * @return 方法
         */
        Method value();

        /**
         * 由于 Forge 读取流体方块材质根据流体 name 属性区分，因此可以多个流体公用一个 blockstate json 文件
         * 此属性可设置使用的 blockstate 文件名（不包括扩展名）。
         * 留空则仍使用方块的 RegistryName
         * @return blockstate 资源名
         */
        String resource() default "fluids";
    }
}

package com.elementtimes.elementcore.api.annotation.tools;

import com.elementtimes.elementcore.api.annotation.part.Method2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解到任意 Item/Block 对象中，作为方块/物品染色
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModColor {

    /**
     * 参数
     *  IBlockState：待染色方块的状态
     *  IBlockAccess：可空，所在世界
     *  BlockPos pos：可空，所处位置
     *  tintIndex：材质 model 文件的 tintindex 属性
     * 返回值
     *  int：颜色值
     * @return 方块染色的方法
     */
    Method2 block() default @Method2;

    /**
     * 参数
     *  ItemStack：待染色物品栈
     *  tintIndex：材质 model 文件的 tintindex 属性
     * 返回值
     *  int：颜色值
     * @return 物品染色的方法
     */
    Method2 item() default @Method2;
}

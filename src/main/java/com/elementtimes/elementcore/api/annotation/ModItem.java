package com.elementtimes.elementcore.api.annotation;

import com.elementtimes.elementcore.api.annotation.part.Getter2;
import com.elementtimes.elementcore.api.annotation.part.Method2;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.NonNullList;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义物品的基本信息
 * 包括 registerName，unlocalizedName，creativeTab
 * @author luqin2007
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModItem {

    /**
     * RegisterName，代表物品注册名
     * 当 registerName 与属性名相同（忽略大小写，使用 toLowerCase 处理）时，可省略
     * @return registerName
     */
    String registerName() default "";

    /**
     * UnlocalizedName，用于获取物品显示名
     * 当 unlocalizedName 与属性名相同（忽略大小写，使用 toLowerCase 处理）时，可省略
     * @return unlocalizedName
     */
    String unlocalizedName() default "";

    String creativeTabKey() default "";

    /**
     * 该物品将在合成表中合成后仍保留在合成表中
     * 由于该注解位于物品注册前，故暂时不支持自定义保留物品（有点麻烦，懒）
     * 等同于 setContainerItem(this) 方法
     * 若需要自定义更多细节，需要重写该物品 Item 类的 getContainerItem 方法
     *
     * @see Item#setContainerItem(Item)
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface RetainInCrafting { }

    /**
     * 设置物品拥有耐久度
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Damageable {
        /**
         * 最大耐久
         */
        int value() default 0;

        /**
         * 是否不可修复
         */
        boolean noRepair() default false;
    }

    /**
     * 该物品类有子类型
     * 等价于
     *  setHasSubtypes(true);
     *  setMaxDamage(0);
     *  setNoRepair();
     * 具体子类型信息需要自行重写 getSubItems 方法
     *
     * @see Item#setHasSubtypes(boolean)
     * @see Item#setMaxDamage(int)
     * @see Item#setNoRepair()
     * @see Item#getSubItems(CreativeTabs, NonNullList)
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface HasSubItem {

        /**
         * 子类型物品的 metadata 列表
         */
        int[] metadatas();

        /**
         * 对应 metadata 值的材质名
         * 默认 domain 为当前 mod id，否则请使用 : 分隔
         * 默认参数为 inventory, 否则请使用 # 分隔
         */
        String[] models();
    }

    /**
     * 自定义 ItemStack 的 Tooltips
     * 简单的增加 Tooltips，详细增加请使用 {@link com.elementtimes.elementcore.api.annotation.tools.ModTooltip}
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Tooltip {
        String[] value();
    }

    /**
     * 简单的着色器
     * 详细设置使用 {@link com.elementtimes.elementcore.api.annotation.tools.ModColor}
     * 或 {@link com.elementtimes.elementcore.api.annotation.tools.ModColorObj}
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface ItemColor {
        /**
         * 简单的物品染色
         * 只要物品材质直接或间接继承自 item/generated 就能支持染色
         * @return 物品颜色
         */
        int value();
    }

    /**
     * ItemMeshDefinition
     * 在注册 model 时会覆盖 HasSubItem 的效果
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface MeshDefinitionObj {
        /**
         * ItemMeshDefinition 实例
         * @return 实例
         */
        Getter2 value();
    }

    /**
     * ItemMeshDefinition
     * 在注册 model 时会覆盖 HasSubItem 的效果
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface MeshDefinitionFunc {
        /**
         * ItemMeshDefinition 方法
         * 参数
         *  ItemStack 物品栈
         * 返回值
         *  ModelResourceLocation 资源
         * @return 方法
         */
        Method2 value();
    }
}
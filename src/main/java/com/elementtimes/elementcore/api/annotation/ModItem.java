package com.elementtimes.elementcore.api.annotation;

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
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface ModItem {
    /**
     * RegisterName，代表物品注册名
     * 当该注解注解 Field 且物品 registerName 与属性名相同（忽略大小写，使用 toLowerCase 处理）时，可省略
     * 当该注解注解 Class 且物品 registerName 与类名相同（忽略大小写，使用 toLowerCase 处理）时，可省略
     * @return registerName
     */
    String registerName() default "";

    /**
     * UnlocalizedName，用于获取物品显示名
     * 当 unlocalizedName 与 registerName 相同时，可省略
     * @return unlocalizedName
     */
    String unlocalizedName() default "";

    String creativeTabKey() default "";

    /**
     * 自定义 ItemStack 的 Tooltips
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD})
    @interface Tooltip {
        /**
         * Tooltips
         * 使用 @m 表示访问 metadata，@n 表示访问 NBT，@c 表示访问个数，-> 表示匹配检查
         *  "@m3@c2@n{Tag}=3->stack3" 意味着只有在 metadata=3，count=2, getTagCompound中Tag值为3 时才会添加 "stack3" 字符串
         *  "stack md=@m, c=@c, nbtName=@n{Stack.Name}" 表示添加
         *      "stack md=[实际metadata值], c=[count 值], nbtName=[getTagCompound().getTag("Stack").getTag("Name")]"
         *  字符串
         * 可以有多个相同类型匹配，同一类型中默认匹配方式为 或，@n& 表示 NBT且，不同类型间使用 且
         *  "@m3@m5@nTag=3@nTag=5@n&{Name}=aa->..." 表示 (meta=3 或 meta=5) 且 (Tag="3" 或 Tag="5" 且 Name="aa") 时匹配通过
         * @return 物品 Tooltip
         */
        String[] value();
    }

    /**
     * 着色器
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD})
    @interface ItemColor {
        /**
         * 物品染色
         * 只要物品材质直接或间接继承自 item/generated 就能支持染色
         * @return 物品染色所需 IItemColor 类全类名
         */
        String value();
    }

    /**
     * 该物品类有子类型
     * 等价于
     *  setHasSubtypes(true);
     *  setMaxDamage(0);
     *  setNoRepair()
     * 具体子类型信息需要自行重写 getSubItems 方法
     *
     * @see Item#setHasSubtypes(boolean)
     * @see Item#setMaxDamage(int)
     * @see Item#setNoRepair()
     * @see Item#getSubItems(CreativeTabs, NonNullList)
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD})
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
     * 有 ItemMeshDefinition
     * 在注册 model 时会覆盖 HasSubItem 的效果
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD})
    @interface HasMeshDefinition {
        /**
         * ItemMeshDefinition 类全类名
         * @return 全类名
         */
        String value();
    }

    /**
     * 该物品将在合成表中合成后仍保留在合成表中
     * 由于该注解位于物品注册前，故暂时不支持自定义保留物品（有点麻烦，懒）
     * 等同于 setContainerItem(this) 方法
     * 若需要自定义更多细节，需要重写该物品 Item 类的 getContainerItem 方法
     *
     * @see Item#setContainerItem(Item)
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD})
    @interface RetainInCrafting { }

    /**
     * 设置物品拥有耐久度
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD})
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
}
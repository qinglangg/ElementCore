package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.annotation.ModTileEntity;
import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.LoaderHelper;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import org.objectweb.asm.Type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 收集 TileEntityType 相关注解的产物
 *
 * @author luqin2007
 */
public class TileEntityTypeLoader {

    private boolean isTypeLoaded = false;
    private boolean isCreatorLoaded = false;
    private boolean isTileEntityLoaded = false;
    private ECModElements mElements;

    final List<TileEntityType> types = new ArrayList<>();
    final Map<String, Supplier<TileEntity>> creator = new HashMap<>();

    public TileEntityTypeLoader(ECModElements elements) {
        mElements = elements;
    }

    public List<TileEntityType> types() {
        mElements.elements.load();
        if (!isCreatorLoaded) {
            loadCreators();
        }
        if (!isTileEntityLoaded) {
            loadEntities();
        }
        if (!isTypeLoaded) {
            loadTypes();
        }
        return types;
    }

    /**
     * @see com.elementtimes.elementcore.api.annotation.ModTileEntity.TileEntityCreator
     */
    private void loadCreators() {
        LoaderHelper.stream(mElements, ModTileEntity.TileEntityCreator.class).forEach(data -> {
            String className = data.getClassType().getClassName();
            if (!creator.containsKey(className)) {
                LoaderHelper.loadClass(mElements, className).ifPresent(clazz -> {
                    boolean isContainer = clazz != Block.class && clazz != TileEntity.class
                            && (Block.class.isAssignableFrom(clazz) || TileEntity.class.isAssignableFrom(clazz));
                    if (isContainer) {
                        try {
                            String methodInfo = data.getMemberName();
                            String methodName = methodInfo.substring(0, methodInfo.indexOf('('));
                            Method method = clazz.getDeclaredMethod(methodName);
                            if (isTileEntityCreator(method)) {
                                creator.put(className, buildTeSupplier(method));
                            }
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        isCreatorLoaded = true;
    }

    /**
     * @see com.elementtimes.elementcore.api.annotation.ModTileEntity
     */
    private void loadEntities() {
        if (!isCreatorLoaded) {
            loadCreators();
        }
        LoaderHelper.stream(mElements, ModTileEntity.class).forEach(data -> LoaderHelper.getBlock(mElements, data.getClassType().getClassName(), data.getMemberName()).ifPresent(block -> {
            Type teType = LoaderHelper.getDefault(data);
            LoaderHelper.loadClass(mElements, teType.getClassName()).ifPresent(teClass -> {
                Supplier<TileEntity> teSupplier = null;
                String teClassName = teClass.getName();
                if (creator.containsKey(teClassName)) {
                    // 1. 搜索 TileEntity 中所有静态方法，选取被 TileEntityCreator 注解的 ()TileEntity 方法
                    teSupplier = creator.get(teClassName);
                } else if (block.getClass() != Block.class && creator.containsKey(block.getClass().getName())) {
                    // 2. 搜索注册的 Block 对象类中所有静态方法，选取被 TileEntityCreator 注解的 ()TileEntity 方法
                    teSupplier = creator.get(block.getClass().getName());
                } else {
                    // 3. 搜索 TileEntity 中所有静态方法，选取 ()TileEntity 方法
                    if (teClass != TileEntity.class) {
                        for (Method method : teClass.getDeclaredMethods()) {
                            if (isTileEntityCreator(method)) {
                                teSupplier = buildTeSupplier(method);
                                break;
                            }
                        }
                    }
                    // 4. 搜索注册的 Block 对象类中所有静态方法，选取 ()TileEntity 方法
                    Class<? extends Block> blockClass = block.getClass();
                    if (teSupplier == null && blockClass != Block.class) {
                        for (Method method : blockClass.getDeclaredMethods()) {
                            if (isTileEntityCreator(method)) {
                                teSupplier = buildTeSupplier(method);
                                break;
                            }
                        }
                    }
                    // 5. 若 Block 实现 {@link net.minecraft.block.ITileEntityProvider}，选取 createNewTileEntity 方法
                    if (teSupplier == null && block instanceof ITileEntityProvider) {
                        teSupplier = () -> {
                            TileEntity te = null;
                            try {
                                te = ((ITileEntityProvider) block).createNewTileEntity(ECUtils.common.getServer());
                            } catch (NullPointerException ignored) { }
                            if (te == null) {
                                ECUtils.reflect.create(teClass, TileEntity.class, mElements.logger).orElse(null);
                            }
                            return te;
                        };
                    }
                    // 6. 选取 {@link net.minecraft.block.Block#createTileEntity(BlockState, IBlockReader)} 方法
                    if (teSupplier == null) {
                        teSupplier = () -> {
                            TileEntity te = null;
                            try {
                                te = block.createTileEntity(block.getDefaultState(), ECUtils.common.getServer());
                            } catch (NullPointerException ignored) { }
                            if (te == null) {
                                return ECUtils.reflect.create(teClass, TileEntity.class, mElements.logger).orElse(null);
                            }
                            return te;
                        };
                    }
                }
                TileEntityType type = TileEntityType.Builder.create(teSupplier, block).build(null);
                LoaderHelper.regName(mElements, type, block, data.getMemberName());
                types.add(type);
            });
        }));
        isTileEntityLoaded = true;
    }

    /**
     * @see com.elementtimes.elementcore.api.annotation.ModTileEntity.TileEntityType
     */
    private void loadTypes() {
        LoaderHelper.stream(mElements, ModTileEntity.TileEntityType.class).forEach(data -> {
            LoaderHelper.loadClass(mElements, data.getClassType().getClassName()).ifPresent(clazz -> {
                ECUtils.reflect.getField(clazz, TileEntityType.class, null, mElements.logger).ifPresent(type -> {
                    LoaderHelper.regName(mElements, type, LoaderHelper.getDefault(data, data.getMemberName()));
                    types.add(type);
                });
            });
        });
        isTypeLoaded = true;
    }

    private boolean isTileEntityCreator(Method method) {
        int modifiers = method.getModifiers();
        return Modifier.isStatic(modifiers)
                && method.getParameterCount() == 0
                && TileEntity.class.isAssignableFrom(method.getReturnType());
    }

    private Supplier<TileEntity> buildTeSupplier(Method method) {
        int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers)) {
            method.setAccessible(true);
        }
        return () -> {
            try {
                return (TileEntity) method.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        };
    }
}

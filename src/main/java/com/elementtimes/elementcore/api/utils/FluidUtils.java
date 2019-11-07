package com.elementtimes.elementcore.api.utils;

import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.template.capability.fluid.vanilla.CapabilityFluidHandler;
import com.elementtimes.elementcore.api.template.capability.fluid.vanilla.interfaces.IFluidHandler;
import com.elementtimes.elementcore.api.template.fluid.AbstractFluid;
import com.elementtimes.elementcore.api.template.fluid.FluidActionResult;
import com.elementtimes.elementcore.api.template.fluid.FluidStack;
import com.elementtimes.elementcore.api.template.fluid.IForgeFluidProvider;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流体工具类
 * @author luqin2007
 */
@SuppressWarnings({"unused"})
public class FluidUtils {

    private static final Map<ResourceLocation, net.minecraftforge.fluids.Fluid> FORGE_FLUIDS = new HashMap<>();
    static {
        FORGE_FLUIDS.put(Fluids.EMPTY.getRegistryName(), new net.minecraftforge.fluids.Fluid("null",
                new ResourceLocation("minecraft", "block/glass"),
                new ResourceLocation("minecraft", "block/water_flow"),
                0xFFFFFFFF));
    }
    public static final net.minecraftforge.fluids.Fluid NULL = FORGE_FLUIDS.get(Fluids.EMPTY.getRegistryName());
    public static final net.minecraftforge.fluids.FluidStack EMPTY = new net.minecraftforge.fluids.FluidStack(NULL, 0);

    private static FluidUtils u = null;
    public static FluidUtils getInstance() {
        if (u == null) {
            u = new FluidUtils();
        }
        return u;
    }
//

//
//    /**
//     * 将流体列表转化为 NBT 列表
//     * @param fluids 流体列表
//     * @return NBT 列表
//     */
//    public ListNBT saveToNbtVanilla(List<FluidStackVanilla> fluids) {
//        ListNBT list = new ListNBT();
//        fluids.forEach(fluid -> list.add(fluid.serializeNBT()));
//        return list;
//    }
//
//    /**
//     * 从 NBT 列表读取流体
//     * @param list NBT 列表
//     * @return 流体
//     */
//    public List<FluidStack> readFromNbt(ListNBT list) {
//        int count = list.size();
//        List<FluidStack> fluidStacks = new ArrayList<>(count);
//        for (int i = 0; i < count; i++) {
//            fluidStacks.add(FluidStack.loadFluidStackFromNBT(list.getCompound(i)));
//        }
//        return fluidStacks;
//    }
//
//    /**
//     * 从 NBT 列表读取流体
//     * @param list NBT 列表
//     * @return 流体
//     */
//    public List<FluidStackVanilla> readFromNbtVanilla(ListNBT list) {
//        int count = list.size();
//        List<FluidStackVanilla> fluidStacks = new ArrayList<>(count);
//        for (int i = 0; i < count; i++) {
//            fluidStacks.add(FluidStackVanilla.fromNbt(list.getCompound(i)));
//        }
//        return fluidStacks;
//    }
//
//    /**
//     * 流体容器非空
//     * @param properties IFluidTankProperties
//     * @return 流体容器非空
//     */
//    public boolean isEmpty(IFluidTankProperties[] properties) {
//        for (IFluidTankProperties property : properties) {
//            if (property.getContents() != null && property.getContents().amount > 0) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * 流体容器非空
//     * @param properties IFluidTankPropertiesVanilla
//     * @return 流体容器非空
//     */
//    public boolean isEmpty(IFluidTankPropertiesVanilla[] properties) {
//        for (IFluidTankPropertiesVanilla property : properties) {
//            if (property.getContents() != Fluids.EMPTY && property.getAmount() > 0) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * 流体容器非空
//     * @param handler 流体容器
//     * @return 流体容器非空
//     */
//    public boolean isEmpty(ITankHandler handler) {
//        for (int i = 0; i < handler.size(); i++) {
//            FluidStackVanilla fluid = handler.getFluidVanilla(i);
//            if (fluid != null && fluid.getAmount() > 0) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * 流体容器某一槽位非空
//     * @param properties IFluidTankProperties
//     * @param slot 槽位
//     * @return 非空
//     */
//    public boolean isEmpty(IFluidTankProperties[] properties, int slot) {
//        if (slot < properties.length) {
//            IFluidTankProperties property = properties[slot];
//            return property.getContents() == null || property.getContents().amount == 0;
//        }
//        return true;
//    }
//
//    /**
//     * 流体容器某一槽位非空
//     * @param properties IFluidTankProperties
//     * @param slot 槽位
//     * @return 非空
//     */
//    public boolean isEmpty(IFluidTankPropertiesVanilla[] properties, int slot) {
//        if (slot < properties.length) {
//            IFluidTankPropertiesVanilla property = properties[slot];
//            return property.getContents() == Fluids.EMPTY || property.getAmount() <= 0;
//        }
//        return true;
//    }
//
//    /**
//     * 流体容器某一槽位非空
//     * @param handler 流体容器
//     * @param slot 槽位
//     * @return 非空
//     */
//    public boolean isEmpty(ITankHandler handler, int slot) {
//        if (slot < handler.size()) {
//            return handler.getFluidVanilla(slot).isEmpty();
//        }
//        return true;
//    }
//
//    /**
//     * 流体容器满
//     * @param properties IFluidTankProperties
//     * @return 流体容器满，。
//     */
//    public boolean isFull(IFluidTankProperties[] properties) {
//        for (IFluidTankProperties property : properties) {
//            int capacity = property.getCapacity();
//            if (capacity > 0) {
//                if (property.getContents() == null || property.getContents().amount < capacity) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
//
//    /**
//     * 流体容器满
//     * @param properties IFluidTankProperties
//     * @return 流体容器满，。
//     */
//    public boolean isFull(IFluidTankPropertiesVanilla[] properties) {
//        for (IFluidTankPropertiesVanilla property : properties) {
//            if (property.getAmount() < property.getCapacity()) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * 流体容器满
//     * @param handler 流体容器
//     * @return 流体容器满，。
//     */
//    public boolean isFull(ITankHandler handler) {
//        for (int i = 0; i < handler.size(); i++) {
//            if (!handler.getFluidVanilla(i).isEmpty()) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * 某槽位是否满
//     * @param properties IFluidTankProperties
//     * @param slot 槽位
//     * @return 是否满
//     */
//    public boolean isFull(IFluidTankProperties[] properties, int slot) {
//        if (slot < properties.length) {
//            IFluidTankProperties property = properties[slot];
//            int capacity = property.getCapacity();
//            return capacity <= 0 || (property.getContents() != null && property.getContents().amount >= capacity);
//        }
//        return false;
//    }
//
//    /**
//     * 某槽位是否满
//     * @param properties IFluidTankProperties
//     * @param slot 槽位
//     * @return 是否满
//     */
//    public boolean isFull(IFluidTankPropertiesVanilla[] properties, int slot) {
//        if (slot < properties.length) {
//            IFluidTankPropertiesVanilla property = properties[slot];
//            int capacity = property.getCapacity();
//            return capacity <= 0 || property.getAmount() >= capacity;
//        }
//        return false;
//    }
//
//    /**
//     * 将某流体容器的流体转化为列表
//     * @param properties IFluidTankProperties
//     * @return 列表
//     */
//    public List<FluidStack> toList(IFluidTankProperties[] properties) {
//        return Arrays.stream(properties)
//                .map(IFluidTankProperties::getContents)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * 将某流体容器的流体转化为列表
//     * @param properties IFluidTankProperties
//     * @return 列表
//     */
//    public List<FluidStackVanilla> toList(IFluidTankPropertiesVanilla[] properties) {
//        return Arrays.stream(properties)
//                .map(IFluidTankPropertiesVanilla::toStack)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * 将某流体容器的流体转化为列表
//     * @param handler 流体容器
//     * @return 列表
//     */
//    public List<FluidStack> toList(ITankHandler handler) {
//        int size = handler.size();
//        List<FluidStack> fluidStacks = new ArrayList<>(size);
//        for (int i = 0; i < size; i++) {
//            fluidStacks.add(i, handler.getFluid(i));
//        }
//        return fluidStacks;
//    }
//
//    /**
//     * 将某流体容器的流体转化为列表
//     * @param handler 流体容器
//     * @return 列表
//     */
//    public List<FluidStackVanilla> toListVanilla(ITankHandler handler) {
//        int size = handler.size();
//        List<FluidStackVanilla> fluidStacks = new ArrayList<>(size);
//        for (int i = 0; i < size; i++) {
//            fluidStacks.add(i, handler.getFluidVanilla(i));
//        }
//        return fluidStacks;
//    }
//
//    /**
//     * 将某流体容器的流体转化为列表，列表非空
//     * @param properties IFluidTankProperties
//     * @return 列表
//     */
//    public List<FluidStack> toListNotNull(IFluidTankProperties[] properties) {
//        return Arrays.stream(properties)
//                .map(IFluidTankProperties::getContents)
//                .map(f -> (f != null && f.amount > 0) ? f : EMPTY)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * 将某流体容器的流体转化为列表，列表非空
//     * @param handler 流体容器
//     * @return 列表
//     */
//    public List<FluidStack> toListNotNull(ITankHandler handler) {
//        int size = handler.size();
//        List<FluidStack> fluidStacks = new ArrayList<>(size);
//        for (int i = 0; i < size; i++) {
//            FluidStack fluid = handler.getFluid(i);
//            if (fluid == null) {
//                fluid = EMPTY;
//            }
//            fluidStacks.add(i, fluid);
//        }
//        return fluidStacks;
//    }
//
//    /**
//     * 从物品中获取流体
//     * @param itemStack 物品栈
//     * @return 流体
//     */
//    public FluidStack getFluid(ItemStack itemStack) {
//        FluidStack fluidStack;
//        LazyOptional<IFluidHandlerItem> capabilityOpt = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
//        if (capabilityOpt.isPresent()) {
//            IFluidHandlerItem capability = capabilityOpt.orElseThrow(RuntimeException::new);
//            if (capability instanceof FluidBucketWrapper) {
//                return ((FluidBucketWrapper) capability).getFluid();
//            } else if (capability instanceof FluidHandlerItemStack) {
//                return ((FluidHandlerItemStack) capability).getFluid();
//            } else if (capability instanceof FluidHandlerItemStackSimple) {
//                return ((FluidHandlerItemStackSimple) capability).getFluid();
//            } else if (capability instanceof ITankHandler) {
//                return  ((ITankHandler) capability).getFluidFirst();
//            } else {
//                return capability.getTankProperties()[0].getContents();
//            }
//        }
//        Item item = itemStack.getItem();
//        Optional<net.minecraft.fluid.Fluid> fluidVanillaOpt = Registry.FLUID.stream()
//                .filter(fluid -> fluid.getFilledBucket() == item)
//                .findFirst();
//        if (fluidVanillaOpt.isPresent()) {
//            net.minecraft.fluid.Fluid fluidVanilla = fluidVanillaOpt.get();
//            if (fluidVanilla instanceof IForgeFluidProvider) {
//                FORGE_FLUIDS.putIfAbsent(fluidVanilla.getRegistryName(), ((IForgeFluidProvider) fluidVanilla).getForgeFluid());
//                return ((IForgeFluidProvider) fluidVanilla).getFluidStack();
//            } else {
//                return new FluidStack(convert(fluidVanilla), Fluid.BUCKET_VOLUME);
//            }
//        }
//        return EMPTY;
//    }
//
//    /**
//     * 从物品中获取流体
//     * @param itemStack 物品栈
//     * @return 流体
//     */
//    public FluidStackVanilla getFluidVanilla(ItemStack itemStack) {
//        FluidStack fluidStack;
//        LazyOptional<IFluidHandlerItem> capabilityOpt = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
//        if (capabilityOpt.isPresent()) {
//            IFluidHandlerItem capability = capabilityOpt.orElseThrow(RuntimeException::new);
//            if (capability instanceof IFluidHandlerVanilla) {
//                return ((IFluidHandlerVanilla) capability).getTankPropertiesVanilla()[0].toStack();
//            }
//        }
//        return Registry.FLUID.stream()
//                .filter(fluid -> fluid.getFilledBucket() == itemStack.getItem())
//                .findFirst()
//                .map(fluid -> new FluidStackVanilla(fluid, Fluid.BUCKET_VOLUME))
//                .orElse(FluidStackVanilla.EMPTY);
//    }
//
//    /**
//     * 从物品中获取流体，非空
//     * @param itemStack 物品栈
//     * @return 流体
//     */
//    public FluidStack getFluidNotNull(ItemStack itemStack) {
//        FluidStack fluidStack = getFluid(itemStack);
//        return fluidStack == null ? EMPTY : fluidStack;
//    }
//
//    public CompoundNBT writeToNBT(IFluidHandler handler, String key, CompoundNBT nbtTagCompound) {
//        if (handler instanceof INBTSerializable) {
//            nbtTagCompound.put(key, ((INBTSerializable) handler).serializeNBT());
//        } else {
//            ListNBT list = new ListNBT();
//            IFluidTankProperties[] properties = handler.getTankProperties();
//            for (IFluidTankProperties property : properties) {
//                FluidStack fluid = property.getContents();
//                if (fluid == null) {
//                    list.add(new CompoundNBT());
//                } else {
//                    list.add(fluid.writeToNBT(new CompoundNBT()));
//                }
//            }
//            nbtTagCompound.put(key, list);
//        }
//        return nbtTagCompound;
//    }
//
//    public void readFromNBT(IFluidHandler handler, String key, CompoundNBT nbtTagCompound) {
//        if (handler instanceof INBTSerializable) {
//            //noinspection unchecked
//            ((INBTSerializable) handler).deserializeNBT(nbtTagCompound.getCompound(key));
//        } else {
//            ListNBT list = nbtTagCompound.getList(key, Constants.NBT.TAG_COMPOUND);
//            IFluidTankProperties[] properties = handler.getTankProperties();
//            int count = Math.min(list.size(), properties.length);
//            for (int i = 0; i < count; i++) {
//                handler.fill(FluidStack.loadFluidStackFromNBT(list.getCompound(i)), true);
//            }
//        }
//    }
//
    /**
     * 从 NBT 列表读取流体
     * @param list NBT 列表
     * @return 流体
     */
    public List<FluidStack> read(ListNBT list) {
        int count = list.size();
        List<FluidStack> fluidStacks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            fluidStacks.add(FluidStack.fromNbt(list.getCompound(i)));
        }
        return fluidStacks;
    }

    /**
     * 将流体列表转化为 NBT 列表
     * @param fluids 流体列表
     * @return NBT 列表
     */
    public ListNBT save(List<FluidStack> fluids) {
        ListNBT list = new ListNBT();
        fluids.forEach(fluid -> list.add(fluid.serializeNBT()));
        return list;
    }

    public int getFilledAmount(Fluid fluid) {
        if (fluid instanceof AbstractFluid) {
            return ((AbstractFluid) fluid).getFilledBucketAmount();
        }
        return 1000;
    }

    public FluidActionResult transfer(@Nullable Fluid type, ItemStack from, IFluidHandler to, int slot) {
        Fluid fluid = fromItem(from.getItem());
        if (fluid != Fluids.EMPTY) {
            if (type == null || fluid == type) {
                // 流体桶
                int filledBucket = fluid instanceof AbstractFluid
                        ? ((AbstractFluid) fluid).getFilledBucketAmount()
                        : net.minecraftforge.fluids.Fluid.BUCKET_VOLUME;
                FluidStack stack = new FluidStack(fluid, filledBucket);
                int fill = to.fill(slot, stack, false);
                if (fill == filledBucket) {
                    to.fill(slot, stack, true);
                    return new FluidActionResult(new ItemStack(Items.BUCKET), stack);
                }
            }
            return new FluidActionResult(from, FluidStack.EMPTY);
        } else {
            // Capability
            LazyOptional<IFluidHandler> capOpt1 = from.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
            if (capOpt1.isPresent()) {
                IFluidHandler handler = capOpt1.orElseThrow(RuntimeException::new);
                FluidStack drain = handler.drain(Integer.MAX_VALUE, false);
                int fill = to.fill(drain, true);
                handler.drain(new FluidStack(drain.getFluid(), fill), true);
                return new FluidActionResult(from, new FluidStack(drain.getFluid(), fill));
            }
            LazyOptional<IFluidHandlerItem> capOpt2 = from.getCapability(net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
            if (capOpt2.isPresent()) {
                IFluidHandlerItem handlerItem = capOpt2.orElseThrow(RuntimeException::new);
                net.minecraftforge.fluids.FluidStack drain = handlerItem.drain(Integer.MAX_VALUE, false);
                int fill = to.fill(convert(drain), true);
                net.minecraftforge.fluids.FluidStack copy = drain.copy();
                copy.amount = fill;
                handlerItem.drain(copy, true);
                return new FluidActionResult(from, new FluidStack(convert(drain.getFluid()), fill));
            }
        }
        return new FluidActionResult(from, FluidStack.EMPTY);
    }

    public FluidActionResult transfer(@Nullable Fluid type, IFluidHandler from, ItemStack to, int slot) {
        Fluid fluid = from.getFluid(slot);
        if (type == null || fluid == type) {
            int amount = getFilledAmount(type);
            if (to.getItem() == Items.BUCKET) {
                FluidStack drain = from.drain(slot, amount, false);
                if (drain.getAmount() == amount) {
                    drain = from.drain(slot, amount, true);
                    return new FluidActionResult(new ItemStack(fluid.getFilledBucket()), drain);
                }
            } else {
                FluidStack fluidStack = from.getFluidStack(slot);
                FluidStack drain = from.drain(slot, fluidStack, false);
                FluidStack fill = drain.copy();
                LazyOptional<IFluidHandler> opt1 = to.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                if (opt1.isPresent()) {
                    opt1.ifPresent(handler -> {
                        int fillAmount = handler.fill(drain, true);
                        fill.setAmount(fillAmount);
                        from.drain(slot, fill, true);
                    });
                    return new FluidActionResult(to, fill);
                }
                LazyOptional<IFluidHandlerItem> opt2 = to.getCapability(net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
                if (opt2.isPresent()) {
                    opt2.ifPresent(handler -> {
                        int fillAmount = handler.fill(convert(drain), true);
                        fill.setAmount(fillAmount);
                        from.drain(slot, fill, true);
                    });
                    return new FluidActionResult(to, fill);
                }
            }
        }
        return new FluidActionResult(to, FluidStack.EMPTY);
    }

    public List<FluidStack> collect(IFluidHandler handler) {
        int size = handler.getSize();
        List<FluidStack> fluids = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            fluids.add(handler.getFluidStack(i));
        }
        return fluids;
    }

    /**
     * 从 ItemStack 中获取流体对象
     * 首先会检查 filledBucketItem，若没有则会从 Capability 中获取
     * @param stack 物品栈
     * @return 流体
     */
    public Fluid fromStack(ItemStack stack) {
        Fluid fluid = fromItem(stack.getItem());
        if (fluid == Fluids.EMPTY) {
            fluid = fromCapability(stack);
        }
        return fluid;
    }

    public Fluid fromItem(Item filledBucket) {
        return Registry.FLUID.stream()
                .filter(fluid -> fluid.getFilledBucket() == filledBucket)
                .findFirst()
                .orElse(Fluids.EMPTY);
    }

    /**
     * 从 Capacity 中获取流体对象
     * @param provider Capability 提供者
     * @return 流体
     */
    public Fluid fromCapability(ICapabilityProvider provider) {
        LazyOptional<IFluidHandler> capVanillaFluidOpt = provider.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
        if (capVanillaFluidOpt.isPresent()) {
            return capVanillaFluidOpt.orElseThrow(RuntimeException::new).getFluid(0);
        } else {
            net.minecraftforge.fluids.FluidStack fluidStack;
            LazyOptional<IFluidHandlerItem> capForgeFluidOpt = provider.getCapability(net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
            IFluidHandlerItem handlerItem = capForgeFluidOpt.orElseThrow(RuntimeException::new);
            if (handlerItem instanceof FluidHandlerItemStackSimple) {
                fluidStack = ((FluidHandlerItemStackSimple) handlerItem).getFluid();
            } else if (handlerItem instanceof FluidHandlerItemStack) {
                fluidStack = ((FluidHandlerItemStack) handlerItem).getFluid();
            } else if (handlerItem instanceof FluidBucketWrapper) {
                fluidStack = ((FluidBucketWrapper) handlerItem).getFluid();
            } else {
                IFluidTankProperties[] properties = handlerItem.getTankProperties();
                fluidStack = properties.length == 0 ? null : properties[0].getContents();
            }
            return fluidStack == null ? Fluids.EMPTY : ECUtils.fluid.convert(fluidStack.getFluid());
        }
    }

    /**
     * 检查两个流体是否等效
     * 通过矿辞检查
     * @param f1 流体 1
     * @param f2 流体 2
     * @return 等效
     */
    public boolean isEquivalent(Fluid f1, Fluid f2) {
        if (f1 == f2) {
            return true;
        }
        if (f1 == null || f2 == null) {
            return false;
        }
        if (f1 instanceof FlowingFluid && f2 instanceof FlowingFluid) {
            return ((FlowingFluid) f1).getStillFluid() == ((FlowingFluid) f2).getStillFluid();
        }
        for (ResourceLocation tagName : f1.getTags()) {
            Tag<Fluid> fluidTag = FluidTags.getCollection().get(tagName);
            if (fluidTag != null && f2.isIn(fluidTag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将 Minecraft 的 Fluid 对象转换为 Forge 的 Fluid 对象
     * @param fluid Minecraft 的 Fluid 对象
     * @return Forge 的 Fluid 对象
     */
    public net.minecraftforge.fluids.Fluid convert(Fluid fluid) {
        return FORGE_FLUIDS.computeIfAbsent(fluid.getRegistryName(), name -> {
            if (fluid instanceof IForgeFluidProvider) {
                return ((IForgeFluidProvider) fluid).getForgeFluid();
            } else {
                return new net.minecraftforge.fluids.Fluid(name.toString(),
                        new ResourceLocation(name.getNamespace(), "block/" + name.getPath()),
                        new ResourceLocation(name.getNamespace(), "block/" + name.getPath()), 0xFFFFFFFF);
            }
        });
    }

    /**
     * 将 Forge 的 Fluid 对象转换为 Minecraft 的 Fluid 对象
     * @param fluid Forge 的 Fluid 对象
     * @return Minecraft 的 Fluid 对象
     */
    public Fluid convert(net.minecraftforge.fluids.Fluid fluid) {
        return Registry.FLUID.getOrDefault(new ResourceLocation(fluid.getName().toLowerCase()));
    }

    /**
     * 将 Forge 的 FluidStack 对象转换为基于 Minecraft Fluid 的 FluidStack 对象
     * @param stack Forge 的 FluidStack 对象
     * @return 基于 Minecraft Fluid 的 FluidStack 对象
     */
    public FluidStack convert(net.minecraftforge.fluids.FluidStack stack) {
        return new FluidStack(convert(stack.getFluid()), stack.amount);
    }

    /**
     * 将基于 Minecraft Fluid 的 FluidStack 对象转换为 Forge 的 FluidStack 对象
     * @param stack 基于 Minecraft Fluid 的 FluidStack 对象
     * @return Forge 的 FluidStack 对象
     */
    public net.minecraftforge.fluids.FluidStack convert(FluidStack stack) {
        return new net.minecraftforge.fluids.FluidStack(convert(stack.getFluid()), stack.getAmount());
    }

    /**
     * 根据 RegistryName 获取流体
     * @param name RegistryName
     * @return 流体
     */
    public Fluid getFluid(ResourceLocation name) {
        return Registry.FLUID.getOrDefault(name);
    }

    /**
     * 根据 RegistryName 获取流体
     * @param name RegistryName
     * @return 流体
     */
    public Fluid getFluid(String name) {
        return getFluid(new ResourceLocation(name.toLowerCase()));
    }
}

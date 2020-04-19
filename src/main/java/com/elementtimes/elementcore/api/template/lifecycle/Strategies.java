package com.elementtimes.elementcore.api.template.lifecycle;

import com.elementtimes.elementcore.api.interfaces.block.ITileEnergyHandler;
import com.elementtimes.elementcore.api.template.capability.SideHandlerType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

/**
 * @see EnergyGeneratorLifecycle.IStrategy
 */
public class Strategies {

    /**
     * 仅向 East 方向发送
     */
    public static EnergyGeneratorLifecycle.IStrategy EAST = context -> sendTo(getSource(context, Direction.EAST), getTarget(context, Direction.EAST, Direction.WEST));

    /**
     * 仅向 West 方向发送
     */
    public static EnergyGeneratorLifecycle.IStrategy WEST = context -> sendTo(getSource(context, Direction.WEST), getTarget(context, Direction.WEST, Direction.EAST));

    /**
     * 仅向 North 方向发送
     */
    public static EnergyGeneratorLifecycle.IStrategy NORTH = context -> sendTo(getSource(context, Direction.NORTH), getTarget(context, Direction.NORTH, Direction.SOUTH));

    /**
     * 仅向 South 方向发送
     */
    public static EnergyGeneratorLifecycle.IStrategy SOUTH = context -> sendTo(getSource(context, Direction.SOUTH), getTarget(context, Direction.SOUTH, Direction.NORTH));

    /**
     * 仅向空机器发送能量
     */
    public static EnergyGeneratorLifecycle.IStrategy EMPTY = context -> {
        for (Direction facing : Direction.values()) {
            IEnergyStorage source = getSource(context, facing);
            IEnergyStorage target = getTarget(context, facing);
            if (target != null && target.canReceive() && target.getEnergyStored() <= 0 && target.getMaxEnergyStored() == 0) {
                if (source != null && source.canExtract() && source.getEnergyStored() > 0) {
                    sendTo(source, target);
                }
            }
        }
    };

    /**
     * 向每个方向平均发送
     */
    public static EnergyGeneratorLifecycle.IStrategy AVERAGE = context -> {
        ArrayList<Pair<IEnergyStorage, IEnergyStorage>> sourceAndTargets = getSourceAndTargets(context, false, false);
        if (!sourceAndTargets.isEmpty()) {
            int energy = getSourceHandler(context).getEnergyStored() / sourceAndTargets.size();
            for (Pair<IEnergyStorage, IEnergyStorage> pair : sourceAndTargets) {
                sendTo(energy, pair.getKey(), pair.getValue());
            }
        }
    };

    /**
     * 向六个方向平均发送，取所有可用方向的最小值
     */
    public static EnergyGeneratorLifecycle.IStrategy AVERAGE_STRICT = context -> {
        ArrayList<Pair<IEnergyStorage, IEnergyStorage>> sourceAndTargets = getSourceAndTargets(context, false, false);
        if (!sourceAndTargets.isEmpty()) {
            int energy = getSourceHandler(context).getEnergyStored() / sourceAndTargets.size();
            Iterator<Pair<IEnergyStorage, IEnergyStorage>> iterator = sourceAndTargets.iterator();
            while (iterator.hasNext()) {
                Pair<IEnergyStorage, IEnergyStorage> pair = iterator.next();
                int extract = pair.getKey().extractEnergy(energy, true);
                int receive = pair.getValue().receiveEnergy(extract, true);
                if (receive > 0) {
                    energy = receive;
                } else {
                    iterator.remove();
                }
            }
            for (Pair<IEnergyStorage, IEnergyStorage> pair : sourceAndTargets) {
                sendTo(energy, pair.getKey(), pair.getValue());
            }
        }
    };

    /**
     * 向每个方向平均发送，尽量保证每个方向存储的能量相同
     */
    public static EnergyGeneratorLifecycle.IStrategy AVERAGE_STORED = context -> {
        ArrayList<Pair<IEnergyStorage, IEnergyStorage>> sourceAndTargets = getSourceAndTargets(context, false, true);
        if (!sourceAndTargets.isEmpty()) {
            int max = sourceAndTargets.get(sourceAndTargets.size() - 1).getValue().getEnergyStored();
            for (Pair<IEnergyStorage, IEnergyStorage> pair : sourceAndTargets) {
                sendTo(max - pair.getValue().getEnergyStored(), pair.getKey(), pair.getValue());
            }
            ArrayList<Pair<IEnergyStorage, IEnergyStorage>> list = getSourceAndTargets(context, false, false);
            if (!list.isEmpty()) {
                int e = getSourceHandler(context).getEnergyStored() / list.size();
                for (Pair<IEnergyStorage, IEnergyStorage> pair : list) {
                    sendTo(e, pair.getKey(), pair.getValue());
                }
            }
        }
    };

    /**
     * 向每个方向依次发送能量，只有前面的满了才向后面发送
     */
    public static EnergyGeneratorLifecycle.IStrategy SEQUENTIAL = context -> {
        for (Pair<IEnergyStorage, IEnergyStorage> pair : getSourceAndTargets(context, false, false)) {
            sendTo(pair.getKey().getEnergyStored(), pair.getKey(), pair.getValue());
        }
    };

    /**
     * 向每个方向依次发送能量，但优先选择空机器
     */
    public static EnergyGeneratorLifecycle.IStrategy EMPTY_SEQUENTIAL = context -> {
        for (Pair<IEnergyStorage, IEnergyStorage> pair : getSourceAndTargets(context, true, false)) {
            sendTo(pair.getKey().getEnergyStored(), pair.getKey(), pair.getValue());
        }
    };

    /**
     * 获取能量源
     * @param context EnergyGeneratorLifecycle 上下文
     * @param facing 目标相对发电机的方向
     * @return 能量源 IEnergyStorage
     */
    private static IEnergyStorage getSource(EnergyGeneratorLifecycle<? extends TileEntity> context, Direction facing) {
        TileEntity te = context.getTe();
        if (te instanceof ITileEnergyHandler) {
            return ((ITileEnergyHandler) te).getEnergyProxy(SideHandlerType.OUTPUT);
        } else {
            return te.getCapability(CapabilityEnergy.ENERGY, facing).orElse(null);
        }
    }

    /**
     * 获取发电机的总能量缓存
     * 对于 ITileEnergyHandler，获取 EnergyHandler，否则获取 null 方向的 Handler
     * @param context EnergyGeneratorLifecycle 上下文
     * @return 总缓存 IEnergyStorage
     */
    private static IEnergyStorage getSourceHandler(EnergyGeneratorLifecycle<? extends TileEntity> context) {
        TileEntity te = context.getTe();
        if (te instanceof ITileEnergyHandler) {
            return ((ITileEnergyHandler) te).getEnergyHandler();
        } else {
            return te.getCapability(CapabilityEnergy.ENERGY).orElse(null);
        }
    }

    /**
     * 获取输出目标
     * @param context EnergyGeneratorLifecycle 上下文
     * @param facing 目标在发电机的哪个方向
     * @return 输出目标
     */
    private static IEnergyStorage getTarget(EnergyGeneratorLifecycle<? extends TileEntity> context, Direction facing) {
        return getTarget(context, facing, facing.getOpposite());
    }

    /**
     * 获取输出目标
     * @param context EnergyGeneratorLifecycle 上下文
     * @param facing 目标在发电机的哪个方向
     * @param opposite 发电机在目标的哪个方向
     * @return 输出目标
     */
    private static IEnergyStorage getTarget(EnergyGeneratorLifecycle<? extends TileEntity> context, Direction facing, Direction opposite) {
        TileEntity te = context.getTe();
        if (!te.hasWorld()) {
            return null;
        }
        TileEntity teTarget = te.getWorld().getTileEntity(te.getPos().offset(facing));
        if (teTarget instanceof ITileEnergyHandler) {
            return ((ITileEnergyHandler) teTarget).getEnergyProxy(SideHandlerType.INPUT);
        } else if (teTarget != null) {
            return teTarget.getCapability(CapabilityEnergy.ENERGY, opposite).orElse(null);
        } else {
            return null;
        }
    }

    /**
     * 获取能量源和输出目标
     * @param context EnergyGeneratorLifecycle 上下文
     * @param emptyFirst 是否将无能量目标提到列表开头
     * @param sort 是否按目标能量多少排序
     * @return Pair 列表，key(left) 为能量源，value(right) 为输出目标
     */
    private static ArrayList<Pair<IEnergyStorage, IEnergyStorage>> getSourceAndTargets(EnergyGeneratorLifecycle<? extends TileEntity> context, boolean emptyFirst, boolean sort) {
        ArrayList<Pair<IEnergyStorage, IEnergyStorage>> s_t = new ArrayList<>();
        for (Direction facing : Direction.values()) {
            IEnergyStorage source = getSource(context, facing);
            IEnergyStorage target = getTarget(context, facing);
            if (target != null && target.canReceive() && target.getEnergyStored() < target.getMaxEnergyStored()) {
                if (source != null && source.canExtract() && source.getEnergyStored() > 0) {
                    if (emptyFirst && target.getEnergyStored() == 0) {
                        s_t.add(0, ImmutablePair.of(source, target));
                    } else {
                        s_t.add(ImmutablePair.of(source, target));
                    }
                }
            }
        }
        if (sort) {
            s_t.sort(Comparator.comparingInt(pair -> pair.getValue().getEnergyStored()));
        }
        return s_t;
    }

    private static void sendTo(int energy, IEnergyStorage source, IEnergyStorage target) {
        if (target != null && target.canReceive()) {
            int extract = source.extractEnergy(energy, true);
            int receive = target.receiveEnergy(extract, true);
            if (receive > 0) {
                int r = target.receiveEnergy(receive, false);
                source.extractEnergy(r, false);
            }
        }
    }

    private static void sendTo(IEnergyStorage source, IEnergyStorage target) {
        sendTo(source.getEnergyStored(), source, target);
    }
}

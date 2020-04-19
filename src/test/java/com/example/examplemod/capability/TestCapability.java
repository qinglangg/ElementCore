package com.example.examplemod.capability;

import com.elementtimes.elementcore.api.annotation.ModCapability;
import com.elementtimes.elementcore.api.annotation.part.Getter;
import com.elementtimes.elementcore.api.annotation.part.Method;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nullable;

// 右键 blockwithte 测试
@ModCapability(factory = @Method(TestCapability.class), storage = @Getter(TestCapability.Storage.class))
public class TestCapability {

    @CapabilityInject(TestCapability.class)
    public static Capability<TestCapability> TEST = null;

    int value = 0;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int add() {
        return value++;
    }

    public static class Storage implements Capability.IStorage<TestCapability> {

        @Nullable
        @Override
        public INBT writeNBT(Capability<TestCapability> capability, TestCapability instance, Direction side) {
            return new IntNBT(instance.value);
        }

        @Override
        public void readNBT(Capability<TestCapability> capability, TestCapability instance, Direction side, INBT nbt) {
            instance.setValue(((IntNBT) nbt).getInt());
        }
    }
}

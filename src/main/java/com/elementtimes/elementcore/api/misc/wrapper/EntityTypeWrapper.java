package com.elementtimes.elementcore.api.misc.wrapper;

import net.minecraft.entity.EntityType;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.annotation.ElementType;
import java.util.Objects;

public class EntityTypeWrapper {

    private final ElementType mElementType;
    private final EntityType<?> mEntityType;
    private final String mClass, mMember;

    public EntityTypeWrapper(EntityType<?> type, ModFileScanData.AnnotationData data) {
        mEntityType = type;
        mElementType = data.getTargetType();
        mClass = data.getClassType().getClassName();
        mMember = data.getMemberName();
    }

    public boolean match(ModFileScanData.AnnotationData data) {
        if (data.getTargetType() == mElementType) {
            return Objects.equals(mClass, data.getClassType().getClassName()) && Objects.equals(mMember, data.getMemberName());
        }
        return false;
    }

    public EntityType<?> getEntityType() {
        return mEntityType;
    }
}

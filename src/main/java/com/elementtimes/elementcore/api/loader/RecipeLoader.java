package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.LoaderHelper;
import com.elementtimes.elementcore.api.annotation.ModRecipeSerializer;
import net.minecraft.item.crafting.IRecipeSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author luqin2007
 */
public class RecipeLoader {

    private boolean isRecipeSerializersLoaded = false;
    private ECModElements mElements;

    List<IRecipeSerializer> serializers = new ArrayList<>();

    public RecipeLoader(ECModElements elements) {
        mElements = elements;
    }

    public List<IRecipeSerializer> serializers() {
        if (!isRecipeSerializersLoaded) {
            mElements.elements.load();
            loadSerializers();
        }
        return serializers;
    }

    private void loadSerializers() {
        LoaderHelper.stream(mElements, ModRecipeSerializer.class).forEach(data -> {
            ECUtils.reflect.create(data.getClassType().getClassName(), IRecipeSerializer.class, mElements.logger).ifPresent(serializer -> {
                String[] split1 = data.getClassType().getClassName().split(".");
                String[] split2 = split1[split1.length - 1].split("$");
                String registerName = LoaderHelper.getDefault(data, split2[split2.length - 1]);
                LoaderHelper.regName(mElements, serializer, registerName);
                this.serializers.add(serializer);
            });
        });
        isRecipeSerializersLoaded = true;
    }
}

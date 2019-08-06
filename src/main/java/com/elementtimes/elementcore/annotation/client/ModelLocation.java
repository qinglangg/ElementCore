package com.elementtimes.elementcore.annotation.client;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

/**
 * 用于记录不同 metadata 的材质注册
 * @author luqin2007
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ModelLocation {

    public final int metadata;
    public final String model;
    public final String property;
    public final String domain;
    public final ResourceLocation resourceLocation;
    public final ModelResourceLocation modelResourceLocation;

    public ModelLocation(int metadata, String modid, String key) {
        this.metadata = metadata;

        int i = key.indexOf(":");
        if (i > 0) {
            domain = key.substring(0, i);
            int j = key.indexOf("#", i);
            if (j > 0) {
                model = key.substring(i+1, j);
                property = key.substring(j+1);
            } else {
                model = key.substring(i+1);
                property = "";
            }
        } else {
            domain = modid;
            int j = key.indexOf("#");
            if (j > 0) {
                model = key.substring(0, j);
                property = key.substring(j+1);
            } else {
                model = key;
                property = "inventory";
            }
        }

        resourceLocation = new ResourceLocation(domain, model);
        modelResourceLocation = new ModelResourceLocation(resourceLocation, property);
    }

    public ModelLocation(int metadata, String key) {
        this(metadata, "minecraft", key);
    }

    public ModelLocation(int metadata, String domain, String model, String property) {
        this.metadata = metadata;
        this.domain = domain;
        this.model = model;
        this.property = property;

        resourceLocation = new ResourceLocation(domain, model);
        modelResourceLocation = new ModelResourceLocation(resourceLocation, property);
    }

    @Override
    public String toString() {
        return "modal=" + domain + ":" + model + "#" + property;
    }
}

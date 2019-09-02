package com.elementtimes.elementcore.api.client;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 用于记录不同 metadata 的材质注册
 * @author luqin2007
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@SideOnly(Side.CLIENT)
public class ModelLocation {

    public final int metadata;
    public final String path;
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
                path = key.substring(i+1, j);
                property = key.substring(j+1);
            } else {
                path = key.substring(i+1);
                property = "";
            }
        } else {
            domain = modid;
            int j = key.indexOf("#");
            if (j > 0) {
                path = key.substring(0, j);
                property = key.substring(j+1);
            } else {
                path = key;
                property = "inventory";
            }
        }

        resourceLocation = new ResourceLocation(domain, path);
        modelResourceLocation = new ModelResourceLocation(resourceLocation, property);
    }

    public ModelLocation(int metadata, String key) {
        this(metadata, "minecraft", key);
    }

    public ModelLocation(int metadata, String domain, String path, String property) {
        this.metadata = metadata;
        this.domain = domain;
        this.path = path;
        this.property = property;

        resourceLocation = new ResourceLocation(domain, path);
        modelResourceLocation = new ModelResourceLocation(resourceLocation, property);
    }

    @Override
    public String toString() {
        return "model=" + domain + ":" + path + "#" + property;
    }
}

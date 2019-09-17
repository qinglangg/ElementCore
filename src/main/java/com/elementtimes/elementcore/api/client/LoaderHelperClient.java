package com.elementtimes.elementcore.api.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

/**
 * 客户端的注册辅助类
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
public class LoaderHelperClient {
    public static ModelResourceLocation getLocationFromState(Map<IBlockState, ModelResourceLocation> locationMap, ModelResourceLocation defLocation, IBlockState state) {
        if (locationMap == null || !locationMap.containsKey(state)) {
            return defLocation;
        }
        return locationMap.get(state);
    }
}

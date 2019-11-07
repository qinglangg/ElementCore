package com.elementtimes.elementcore.api;

import com.elementtimes.elementcore.api.loader.client.BlockClientLoader;
import com.elementtimes.elementcore.api.loader.client.ContainerTypeClientLoader;
import com.elementtimes.elementcore.api.loader.client.ElementClientLoader;
import com.elementtimes.elementcore.api.loader.client.ItemClientLoader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 客户端的内容，主要包括渲染部分
 * @author luqin2007
 */
@OnlyIn(Dist.CLIENT)
public class ECModElementsClient {

    public final ECModElements mElements;
    public final BlockClientLoader blocks;
    public final ItemClientLoader items;
    public final ElementClientLoader elements;
    public final ContainerTypeClientLoader containers;

    public ECModElementsClient(ECModElements elements) {
        mElements = elements;
        this.blocks = new BlockClientLoader(elements);
        this.items = new ItemClientLoader(elements);
        this.elements = new ElementClientLoader(elements);
        this.containers = new ContainerTypeClientLoader(elements);
    }
}

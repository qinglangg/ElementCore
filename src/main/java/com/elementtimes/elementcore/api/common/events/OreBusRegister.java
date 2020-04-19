package com.elementtimes.elementcore.api.common.events;

import com.elementtimes.elementcore.api.common.ECModContainer;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.annotation.enums.GenType;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

/**
 * 处理世界生成事件
 * @author luqin2007
 */
public class OreBusRegister {

    private ECModContainer mContainer;

    public OreBusRegister(ECModContainer container) {
        mContainer = container;
    }

    @SubscribeEvent
    public void onGenerateOre(OreGenEvent.Post event) {
        ECUtils.common.runWithModActive(mContainer.mod, () -> {
            if (!event.getWorld().isRemote) {
                ECModElements elements = mContainer.elements();
                final List<WorldGenerator> worldGenerators = elements.blockWorldGen.get(GenType.Ore);
                if (worldGenerators != null) {
                    for (WorldGenerator generator: worldGenerators) {
                        if (TerrainGen.generateOre(event.getWorld(), event.getRand(), generator, event.getPos(), OreGenEvent.GenerateMinable.EventType.CUSTOM)) {
                            generator.generate(event.getWorld(), event.getRand(), event.getPos());
                        }
                    }
                }
            }
        }, event);
    }
}

package com.elementtimes.elementcore.api.annotation.register;

import com.elementtimes.elementcore.api.ECModContainer;
import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.ECUtils;
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

    private ECModElements mElements;

    public OreBusRegister(ECModElements elements) {
        mElements = elements;
    }

    @SubscribeEvent
    public void onGenerateOre(OreGenEvent.Post event) {
        ECUtils.common.runWithModActive(mElements.container.mod, () -> {
            if (!event.getWorld().isRemote) {
                final List<WorldGenerator> worldGenerators = mElements.blockWorldGen.get(GenType.Ore);
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

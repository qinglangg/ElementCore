package com.elementtimes.elementcore.api.common.event;

import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.annotation.enums.GenType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

/**
 * 区块生成相关
 * @author luqin2007
 */
public class TerrainBusRegister {

    private ECModElements mElements;

    public TerrainBusRegister(ECModElements elements) {
        mElements = elements;
    }

    @SubscribeEvent
    public void onGenerateTree(DecorateBiomeEvent.Post event) {
        ECUtils.common.runWithModActive(mElements.container.mod, () -> {
            if (!event.getWorld().isRemote) {
                final List<WorldGenerator> worldGenerators = mElements.blockWorldGen == null ? null : mElements.blockWorldGen.get(GenType.Tree);
                if (worldGenerators != null) {
                    for (WorldGenerator generator: worldGenerators) {
                        ChunkPos chunkPos = event.getChunkPos();
                        if (TerrainGen.decorate(event.getWorld(), event.getRand(), chunkPos, DecorateBiomeEvent.Decorate.EventType.TREE)) {
                            generator.generate(event.getWorld(), event.getRand(), new BlockPos(chunkPos.getXStart(), 0, chunkPos.getZStart()));
                        }
                    }
                }
            }
        }, event);
    }
}

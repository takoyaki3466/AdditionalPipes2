package com.takoy3466.additional_pipes2.chunkloader;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

/**
 * チャンクローダー 1 台が読み込み続けるチャンクの範囲。元実装: {@code ChunkStore.getLoadArea}。
 * ローダーのあるチャンクを中心とした 3 × 3 チャンク。
 */
public final class ChunkLoadArea {
    /** 中心からの半径 (チャンク数)。 */
    public static final int RADIUS = 1;

    private ChunkLoadArea() {
    }

    /** ローダーのあるチャンクを中心とした範囲 (中心を含む 9 チャンク)。 */
    public static List<ChunkPos> around(BlockPos loaderPos) {
        return around(new ChunkPos(loaderPos));
    }

    public static List<ChunkPos> around(ChunkPos center) {
        List<ChunkPos> area = new ArrayList<>((2 * RADIUS + 1) * (2 * RADIUS + 1));
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                area.add(new ChunkPos(center.x + dx, center.z + dz));
            }
        }
        return area;
    }
}

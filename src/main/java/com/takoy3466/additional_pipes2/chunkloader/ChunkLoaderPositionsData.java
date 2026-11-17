package com.takoy3466.additional_pipes2.chunkloader;

import com.takoy3466.additional_pipes2.AdditionalPipes2;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * ディメンションごとのチャンクローダーの位置。元実装: {@code ChunkStore} (ディメンション番号 → ローダーのチャンク座標)。
 * <p>
 * チャンクの強制読み込み自体は NeoForge の {@link net.neoforged.neoforge.common.world.chunk.TicketController} が
 * ワールドに保存して再起動後に復元する。この記録は、範囲表示 (旧 toggleLasers) のためにローダーの一覧を
 * クライアントへ送る用途と、読み込み時のログ (旧 "Force loading N chunks.") に使う。
 */
public final class ChunkLoaderPositionsData extends SavedData {
    private static final String DATA_NAME = AdditionalPipes2.MODID + "_chunk_loaders";
    private static final String TAG_POSITIONS = "positions";
    private static final Factory<ChunkLoaderPositionsData> FACTORY =
            new Factory<>(ChunkLoaderPositionsData::new, ChunkLoaderPositionsData::load, null);

    private final Set<BlockPos> positions = new LinkedHashSet<>();

    public static ChunkLoaderPositionsData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    private static ChunkLoaderPositionsData load(CompoundTag tag, HolderLookup.Provider registries) {
        ChunkLoaderPositionsData data = new ChunkLoaderPositionsData();
        for (long packed : tag.getLongArray(TAG_POSITIONS)) {
            data.positions.add(BlockPos.of(packed));
        }
        return data;
    }

    public void add(BlockPos pos) {
        if (positions.add(pos.immutable())) {
            setDirty();
        }
    }

    public void remove(BlockPos pos) {
        if (positions.remove(pos)) {
            setDirty();
        }
    }

    public List<BlockPos> positions() {
        return List.copyOf(positions);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put(TAG_POSITIONS, new LongArrayTag(positions.stream().mapToLong(BlockPos::asLong).toArray()));
        return tag;
    }
}

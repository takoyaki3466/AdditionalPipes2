package com.takoy3466.additional_pipes2.chunkloader;

import com.takoy3466.additional_pipes2.AdditionalPipes2;
import com.takoy3466.additional_pipes2.registry.ModBlocks;
import com.takoy3466.additional_pipes2.util.ModLog;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;

/**
 * チャンクローダーのチケット (強制読み込み) の管理。元実装: {@code ChunkLoadingHandler}
 * (旧 Forge の IChunkLoadHandler で、ローダー周辺のチャンクを「アクティブ」かつ「アンロード不可」にしていた)。
 * <p>
 * サーバー側でのみ呼び出す。チケットはローダーの位置を所有者として NeoForge が保存・復元するため、
 * ワールドの終了やチャンクのアンロードでは解放しない。解放するのはローダーが撤去されたとき
 * ({@link #release}) と、ワールド読み込み時にローダーが見つからなかったとき ({@link #validate}) だけ。
 * 旧版と同じく、読み込んだチャンクではエンティティやブロックの更新も行う (ticking = true)。
 */
public final class ChunkLoaderTickets {
    private static final boolean TICKING = true;

    public static final TicketController CONTROLLER = new TicketController(AdditionalPipes2.id("teleport_tether"), ChunkLoaderTickets::validate);

    private ChunkLoaderTickets() {
    }

    public static void registerController(RegisterTicketControllersEvent event) {
        event.register(CONTROLLER);
    }

    /** ローダーの範囲を強制読み込みする。すでに読み込み済みでも安全に呼べる。 */
    public static void activate(ServerLevel level, BlockPos loaderPos) {
        for (ChunkPos chunk : ChunkLoadArea.around(loaderPos)) {
            CONTROLLER.forceChunk(level, loaderPos, chunk.x, chunk.z, true, TICKING);
        }
        ChunkLoaderPositionsData.get(level).add(loaderPos);
        ModLog.info("Teleport tether at {} in {} is force loading chunks around {}", loaderPos, level.dimension().location(),
                new ChunkPos(loaderPos));
    }

    /** ローダーの範囲の強制読み込みをやめる (撤去時)。 */
    public static void release(ServerLevel level, BlockPos loaderPos) {
        for (ChunkPos chunk : ChunkLoadArea.around(loaderPos)) {
            CONTROLLER.forceChunk(level, loaderPos, chunk.x, chunk.z, false, TICKING);
        }
        ChunkLoaderPositionsData.get(level).remove(loaderPos);
        ModLog.info("Teleport tether at {} in {} released its chunks", loaderPos, level.dimension().location());
    }

    /**
     * ワールド読み込み時の検証。チケットの所有者の位置にローダーが無ければ (他の手段で消された場合など)、
     * チケットを捨てて読み込みを止める。
     */
    private static void validate(ServerLevel level, TicketHelper helper) {
        ChunkLoaderPositionsData positions = ChunkLoaderPositionsData.get(level);
        List<BlockPos> stale = new ArrayList<>();
        for (BlockPos owner : helper.getBlockTickets().keySet()) {
            if (!level.getBlockState(owner).is(ModBlocks.TELEPORT_TETHER.get())) {
                stale.add(owner);
            }
        }
        for (BlockPos owner : stale) {
            helper.removeAllTickets(owner);
            positions.remove(owner);
            AdditionalPipes2.LOGGER.warn("Removed chunk loading tickets of a missing teleport tether at {} in {}", owner,
                    level.dimension().location());
        }
        ModLog.info("Force loading chunks for {} teleport tether(s) in {}", helper.getBlockTickets().size() - stale.size(),
                level.dimension().location());
    }

    /** 範囲表示用: 実際にローダーが存在する位置だけを返し、存在しない位置は記録から外す。 */
    public static List<BlockPos> existingLoaders(ServerLevel level) {
        ChunkLoaderPositionsData data = ChunkLoaderPositionsData.get(level);
        List<BlockPos> existing = new ArrayList<>();
        for (BlockPos pos : data.positions()) {
            if (level.isLoaded(pos) && level.getBlockState(pos).is(ModBlocks.TELEPORT_TETHER.get())) {
                existing.add(pos);
            } else if (level.isLoaded(pos)) {
                data.remove(pos);
            }
        }
        return existing;
    }
}

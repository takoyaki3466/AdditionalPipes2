package com.takoy3466.additional_pipes2.chunkloader;

import com.takoy3466.additional_pipes2.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * テレポートテザーのブロックエンティティ。元実装: {@code TileChunkLoader}
 * (旧版は validate で ChunkStore に登録し、invalidate で外していた)。
 * <p>
 * 状態を持たず、読み込まれたときに範囲の強制読み込みを (再) 登録するだけ。チケットは NeoForge が保存するため
 * 通常は再起動後もそのまま残っているが、登録は冪等なので毎回行って取りこぼしを防ぐ。
 */
public class ChunkLoaderBlockEntity extends BlockEntity {

    public ChunkLoaderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TELEPORT_TETHER.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            ChunkLoaderTickets.activate(serverLevel, worldPosition);
        }
    }
}

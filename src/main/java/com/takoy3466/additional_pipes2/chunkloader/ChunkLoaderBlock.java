package com.takoy3466.additional_pipes2.chunkloader;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * テレポートテザー (チャンクローダー)。元実装: {@code BlockChunkLoader} + {@code TileChunkLoader}。
 * <p>
 * 読み込みの開始はブロックエンティティの読み込み時 ({@link ChunkLoaderBlockEntity#onLoad})、
 * 解放はブロックが別のブロックに置き換わったとき (破壊・爆発・ピストンなど) に行う。
 */
public class ChunkLoaderBlock extends Block implements EntityBlock {

    public ChunkLoaderBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChunkLoaderBlockEntity(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
            ChunkLoaderTickets.release(serverLevel, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}

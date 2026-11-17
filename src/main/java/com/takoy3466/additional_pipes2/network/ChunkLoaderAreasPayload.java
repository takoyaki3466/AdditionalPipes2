package com.takoy3466.additional_pipes2.network;

import com.takoy3466.additional_pipes2.AdditionalPipes2;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * プレイヤーのいるディメンションにあるチャンクローダーの位置 (サーバー → クライアント)。範囲表示に使う。
 */
public record ChunkLoaderAreasPayload(List<BlockPos> loaders) implements CustomPacketPayload {
    public static final Type<ChunkLoaderAreasPayload> TYPE = new Type<>(AdditionalPipes2.id("chunk_loader_areas"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChunkLoaderAreasPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), ChunkLoaderAreasPayload::loaders,
            ChunkLoaderAreasPayload::new);

    public ChunkLoaderAreasPayload {
        loaders = List.copyOf(loaders);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

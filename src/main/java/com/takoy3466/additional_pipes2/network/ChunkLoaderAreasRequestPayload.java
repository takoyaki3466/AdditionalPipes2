package com.takoy3466.additional_pipes2.network;

import com.takoy3466.additional_pipes2.AdditionalPipes2;
import com.takoy3466.additional_pipes2.chunkloader.ChunkLoaderTickets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * チャンクローダーの範囲表示を有効にしたクライアントが、今いるディメンションのローダー一覧を求める (クライアント → サーバー)。
 * 旧版はシングルプレイ前提で、クライアントが直接 ChunkStore を読んでいた。
 */
public record ChunkLoaderAreasRequestPayload() implements CustomPacketPayload {
    public static final ChunkLoaderAreasRequestPayload INSTANCE = new ChunkLoaderAreasRequestPayload();
    public static final Type<ChunkLoaderAreasRequestPayload> TYPE = new Type<>(AdditionalPipes2.id("chunk_loader_areas_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChunkLoaderAreasRequestPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    static void handle(ChunkLoaderAreasRequestPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new ChunkLoaderAreasPayload(ChunkLoaderTickets.existingLoaders(player.serverLevel())));
        }
    }
}

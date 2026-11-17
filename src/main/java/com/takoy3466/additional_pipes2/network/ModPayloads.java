package com.takoy3466.additional_pipes2.network;

import com.takoy3466.additional_pipes2.client.ClientPayloadHandlers;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 通信内容の登録。サーバー宛ての処理は各ペイロードに、クライアント宛ての処理は
 * {@link ClientPayloadHandlers} (クライアント専用クラス) に置く。
 * <p>
 * クライアント宛ての処理はラムダの中から呼ぶ。ラムダの本体は実行されるまでクライアント専用クラスを
 * 読み込まないため、専用サーバーでクライアントのクラスが読み込まれることはない。
 */
public final class ModPayloads {
    private static final String PROTOCOL_VERSION = "1";

    private ModPayloads() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(TeleportPipeActionPayload.TYPE, TeleportPipeActionPayload.STREAM_CODEC,
                TeleportPipeActionPayload::handle);
        registrar.playToServer(ChunkLoaderAreasRequestPayload.TYPE, ChunkLoaderAreasRequestPayload.STREAM_CODEC,
                ChunkLoaderAreasRequestPayload::handle);
        registrar.playToClient(TeleportPipeStatePayload.TYPE, TeleportPipeStatePayload.STREAM_CODEC,
                (payload, context) -> ClientPayloadHandlers.handleTeleportState(payload));
        registrar.playToClient(ChunkLoaderAreasPayload.TYPE, ChunkLoaderAreasPayload.STREAM_CODEC,
                (payload, context) -> ClientPayloadHandlers.handleChunkLoaderAreas(payload));
    }
}

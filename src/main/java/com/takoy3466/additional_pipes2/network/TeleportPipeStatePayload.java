package com.takoy3466.additional_pipes2.network;

import com.takoy3466.additional_pipes2.AdditionalPipes2;
import com.takoy3466.additional_pipes2.menu.TeleportPipeView;
import com.takoy3466.additional_pipes2.teleport.FrequencyNameBook;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * テレポートパイプの画面の表示内容 (サーバー → クライアント)。
 */
public record TeleportPipeStatePayload(int containerId, TeleportPipeView view) implements CustomPacketPayload {
    public static final Type<TeleportPipeStatePayload> TYPE = new Type<>(AdditionalPipes2.id("teleport_pipe_state"));
    /** プレイヤー名の最大長 (Minecraft のユーザー名は 16 文字)。 */
    private static final int MAX_OWNER_NAME_LENGTH = 16;

    private static final StreamCodec<ByteBuf, FrequencyNameBook.Entry> ENTRY_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FrequencyNameBook.Entry::frequency,
            ByteBufCodecs.stringUtf8(FrequencyNameBook.MAX_NAME_LENGTH), FrequencyNameBook.Entry::name,
            FrequencyNameBook.Entry::new);

    private static final StreamCodec<ByteBuf, TeleportPipeView> VIEW_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TeleportPipeView::frequency,
            ByteBufCodecs.BOOL, TeleportPipeView::canReceive,
            ByteBufCodecs.VAR_INT, TeleportPipeView::connectedCount,
            ByteBufCodecs.stringUtf8(MAX_OWNER_NAME_LENGTH), TeleportPipeView::ownerName,
            ENTRY_CODEC.apply(ByteBufCodecs.list()), TeleportPipeView::names,
            TeleportPipeView::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportPipeStatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TeleportPipeStatePayload::containerId,
            VIEW_CODEC, TeleportPipeStatePayload::view,
            TeleportPipeStatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

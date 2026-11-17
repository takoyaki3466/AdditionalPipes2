package com.takoy3466.additional_pipes2.network;

import com.takoy3466.additional_pipes2.AdditionalPipes2;
import com.takoy3466.additional_pipes2.menu.TeleportPipeMenu;
import com.takoy3466.additional_pipes2.teleport.FrequencyNameBook;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * テレポートパイプの画面の操作 (クライアント → サーバー)。
 * サーバーは、送信者が今開いているメニューが {@code containerId} の {@link TeleportPipeMenu} である場合だけ処理する。
 *
 * @param value  {@link Action#SET_FREQUENCY} の周波数
 * @param text   {@link Action#ADD_NAME} の名前
 */
public record TeleportPipeActionPayload(int containerId, Action action, int value, String text) implements CustomPacketPayload {
    public static final Type<TeleportPipeActionPayload> TYPE = new Type<>(AdditionalPipes2.id("teleport_pipe_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportPipeActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TeleportPipeActionPayload::containerId,
            Action.STREAM_CODEC, TeleportPipeActionPayload::action,
            ByteBufCodecs.VAR_INT, TeleportPipeActionPayload::value,
            ByteBufCodecs.stringUtf8(FrequencyNameBook.MAX_NAME_LENGTH), TeleportPipeActionPayload::text,
            TeleportPipeActionPayload::new);

    public static TeleportPipeActionPayload of(int containerId, Action action, int value) {
        return new TeleportPipeActionPayload(containerId, action, value, "");
    }

    public static TeleportPipeActionPayload addName(int containerId, String name) {
        return new TeleportPipeActionPayload(containerId, Action.ADD_NAME, 0, name);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** サーバーのメインスレッドで処理する。 */
    static void handle(TeleportPipeActionPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player
                && player.containerMenu instanceof TeleportPipeMenu menu
                && menu.containerId == payload.containerId()) {
            menu.handleAction(player, payload.action(), payload.value(), payload.text());
        }
    }

    /** 操作の種類。 */
    public enum Action {
        SET_FREQUENCY,
        TOGGLE_RECEIVE,
        ADD_NAME,
        REMOVE_NAME;

        static final StreamCodec<ByteBuf, Action> STREAM_CODEC = ByteBufCodecs.idMapper(index -> values()[index], Action::ordinal);
    }
}

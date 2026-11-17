package com.takoy3466.additional_pipes2.client;

import com.takoy3466.additional_pipes2.menu.TeleportPipeMenu;
import com.takoy3466.additional_pipes2.network.ChunkLoaderAreasPayload;
import com.takoy3466.additional_pipes2.network.TeleportPipeStatePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** クライアント宛てペイロードの処理 (クライアント専用, メインスレッドで呼ばれる)。 */
public final class ClientPayloadHandlers {

    private ClientPayloadHandlers() {
    }

    /** 開いているテレポートパイプの画面へ表示内容を渡す。別の画面を開いていれば捨てる。 */
    public static void handleTeleportState(TeleportPipeStatePayload payload) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof TeleportPipeMenu menu && menu.containerId == payload.containerId()) {
            menu.applyView(payload.view());
        }
    }

    public static void handleChunkLoaderAreas(ChunkLoaderAreasPayload payload) {
        ChunkBoundaryOverlay.show(payload.loaders());
    }
}

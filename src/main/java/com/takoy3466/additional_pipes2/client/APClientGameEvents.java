package com.takoy3466.additional_pipes2.client;

import com.takoy3466.additional_pipes2.AdditionalPipes2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** クライアントのゲーム中イベント (キー入力・範囲表示の描画・切断時の後始末)。 */
@EventBusSubscriber(modid = AdditionalPipes2.MODID, value = Dist.CLIENT)
public final class APClientGameEvents {

    private APClientGameEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (APClient.TOGGLE_CHUNK_LOADER_BOUNDARIES.consumeClick()) {
            ChunkBoundaryOverlay.toggle();
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            ChunkBoundaryOverlay.render(event.getPoseStack(), event.getCamera().getPosition());
        }
    }

    /** ワールドから出たら表示を消す (旧版はワールドを閉じるとレーザーごと消えていた)。 */
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ChunkBoundaryOverlay.hide();
    }
}

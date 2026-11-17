package com.takoy3466.additional_pipes2.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.takoy3466.additional_pipes2.chunkloader.ChunkLoadArea;
import com.takoy3466.additional_pipes2.network.ChunkLoaderAreasRequestPayload;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * チャンクローダーの範囲表示 (旧 {@code mod_AdditionalPipes.toggleLasers})。
 * <p>
 * 表示を有効にした時点のプレイヤーの高さに、各ローダーが読み込む 9 チャンクそれぞれの境界 (青) と
 * チャンク中央の目印 (赤, 中央 2 × 2 ブロック) を線で描く。旧版はレーザーエンティティで描いていた。
 * ローダーの一覧はサーバーに問い合わせる (マルチプレイ対応)。表示中のディメンションを離れると消す。
 * クライアントのメインスレッドからのみ使う。
 */
public final class ChunkBoundaryOverlay {
    private static final float[] OUTER_COLOR = {0.25F, 0.4F, 1.0F, 1.0F};
    private static final float[] INNER_COLOR = {1.0F, 0.2F, 0.2F, 1.0F};
    /** チャンク中央の目印の範囲 (旧 insideLaser: チャンク内の 7 - 9)。 */
    private static final int CENTER_MARK_MIN = 7;
    private static final int CENTER_MARK_MAX = 9;

    private static List<BlockPos> loaders = List.of();
    @Nullable
    private static ResourceKey<Level> dimension;
    private static int displayY;
    private static boolean shown;
    private static boolean awaitingResponse;

    private ChunkBoundaryOverlay() {
    }

    /** キー操作: 表示中なら消し、そうでなければサーバーにローダーの一覧を求める。 */
    public static void toggle() {
        if (shown || awaitingResponse) {
            hide();
            return;
        }
        awaitingResponse = true;
        PacketDistributor.sendToServer(ChunkLoaderAreasRequestPayload.INSTANCE);
    }

    /** サーバーからローダーの一覧が届いた。表示の要求中でなければ無視する。 */
    public static void show(List<BlockPos> loaderPositions) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!awaitingResponse || minecraft.player == null || minecraft.level == null) {
            return;
        }
        awaitingResponse = false;
        loaders = List.copyOf(loaderPositions);
        dimension = minecraft.level.dimension();
        displayY = minecraft.player.getBlockY();
        shown = true;
    }

    public static void hide() {
        shown = false;
        awaitingResponse = false;
        loaders = List.of();
        dimension = null;
    }

    public static void render(PoseStack poseStack, Vec3 cameraPosition) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!shown || minecraft.level == null) {
            return;
        }
        if (!minecraft.level.dimension().equals(dimension)) {
            hide();
            return;
        }
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        poseStack.pushPose();
        poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
        for (BlockPos loader : loaders) {
            for (ChunkPos chunk : ChunkLoadArea.around(loader)) {
                drawSquare(poseStack, lines, chunk.getMinBlockX(), chunk.getMinBlockZ(),
                        chunk.getMaxBlockX() + 1, chunk.getMaxBlockZ() + 1, OUTER_COLOR);
                drawSquare(poseStack, lines, chunk.getMinBlockX() + CENTER_MARK_MIN, chunk.getMinBlockZ() + CENTER_MARK_MIN,
                        chunk.getMinBlockX() + CENTER_MARK_MAX, chunk.getMinBlockZ() + CENTER_MARK_MAX, INNER_COLOR);
            }
        }
        poseStack.popPose();
        buffers.endBatch(RenderType.lines());
    }

    private static void drawSquare(PoseStack poseStack, VertexConsumer lines, int minX, int minZ, int maxX, int maxZ, float[] color) {
        LevelRenderer.renderLineBox(poseStack, lines, minX, displayY, minZ, maxX, displayY, maxZ,
                color[0], color[1], color[2], color[3]);
    }
}

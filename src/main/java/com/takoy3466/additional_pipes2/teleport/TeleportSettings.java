package com.takoy3466.additional_pipes2.teleport;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * テレポートパイプ 1 本の設定。元実装: {@code PipeLogicTeleport} の freq / canReceive / owner。
 * <p>
 * 旧版は所有者をユーザー名 (大文字小文字を区別しない) で持っていたが、名前の変更に強い UUID で照合し、
 * 表示用に名前も保存する。
 */
public final class TeleportSettings {
    /** 周波数の範囲 (旧 GuiTeleportPipeNew.setNewFreq: 0 - 99999)。 */
    public static final int MIN_FREQUENCY = 0;
    public static final int MAX_FREQUENCY = 99999;
    private static final String TAG_FREQUENCY = "freq";
    private static final String TAG_CAN_RECEIVE = "canReceive";
    private static final String TAG_OWNER = "owner";
    private static final String TAG_OWNER_NAME = "ownerName";

    private int frequency;
    private boolean canReceive;
    @Nullable
    private UUID owner;
    private String ownerName = "";

    public int frequency() {
        return frequency;
    }

    /** 範囲外の値は範囲内へ丸める。 */
    public void setFrequency(int frequency) {
        this.frequency = clampFrequency(frequency);
    }

    public static int clampFrequency(int frequency) {
        return Math.clamp(frequency, MIN_FREQUENCY, MAX_FREQUENCY);
    }

    public boolean canReceive() {
        return canReceive;
    }

    public void toggleReceive() {
        canReceive = !canReceive;
    }

    @Nullable
    public UUID owner() {
        return owner;
    }

    public String ownerName() {
        return ownerName;
    }

    public boolean hasOwner() {
        return owner != null;
    }

    /** 所有者がまだいなければ設定する (旧 blockActivated: owner が空なら操作したプレイヤー)。 */
    public boolean claimIfUnowned(UUID player, String playerName) {
        if (owner != null) {
            return false;
        }
        owner = player;
        ownerName = playerName;
        return true;
    }

    /**
     * {@code other} と同じテレポート網に属するか (周波数は別途照合する)。
     *
     * @param ownersMustMatch マルチプレイ (専用サーバー) では所有者が同じパイプ同士だけをつなぐ。
     *                        旧版は {@code MutiPlayerProxy.isOnServer()} が偽 (シングルプレイ) なら所有者を問わなかった。
     */
    public boolean sharesNetworkWith(TeleportSettings other, boolean ownersMustMatch) {
        return !ownersMustMatch || Objects.equals(owner, other.owner);
    }

    public void save(CompoundTag tag) {
        tag.putInt(TAG_FREQUENCY, frequency);
        tag.putBoolean(TAG_CAN_RECEIVE, canReceive);
        if (owner != null) {
            tag.putUUID(TAG_OWNER, owner);
        }
        tag.putString(TAG_OWNER_NAME, ownerName);
    }

    public void load(CompoundTag tag) {
        frequency = clampFrequency(tag.getInt(TAG_FREQUENCY));
        canReceive = tag.getBoolean(TAG_CAN_RECEIVE);
        owner = tag.hasUUID(TAG_OWNER) ? tag.getUUID(TAG_OWNER) : null;
        ownerName = tag.getString(TAG_OWNER_NAME);
    }
}

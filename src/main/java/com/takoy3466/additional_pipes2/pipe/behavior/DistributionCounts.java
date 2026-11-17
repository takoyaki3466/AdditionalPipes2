package com.takoy3466.additional_pipes2.pipe.behavior;

import java.util.Arrays;
import net.minecraft.nbt.CompoundTag;

/**
 * 分配パイプの面ごとの送出個数と、現在の面へ送った個数。元実装: {@code PipeLogicDistributor.distData / curTick}。
 * <p>
 * ワールドに依存しない計算だけを持ち、単体テストできるようにしている。
 * 面の番号は {@link net.minecraft.core.Direction#get3DDataValue()} (下, 上, 北, 南, 西, 東) で、旧版の Orientations と同じ並び。
 */
public final class DistributionCounts {
    public static final int SIDES = 6;
    /** GUI の同期 (ContainerData は 16 bit) で表せる上限。旧版に上限は無かった。 */
    public static final int MAX_AMOUNT = Short.MAX_VALUE;
    private static final int DEFAULT_AMOUNT = 1;
    private static final String TAG_AMOUNTS = "distData";
    private static final String TAG_SENT = "curTick";

    private final int[] amounts = new int[SIDES];
    private int sentToCurrent;

    public DistributionCounts() {
        Arrays.fill(amounts, DEFAULT_AMOUNT);
    }

    public int amount(int side) {
        return amounts[side];
    }

    public int sentToCurrent() {
        return sentToCurrent;
    }

    /**
     * GUI の +/- 操作。0 未満にはならず、全ての面が 0 になった場合は全て 1 に戻す (旧 GuiDistributionPipe)。
     */
    public void adjust(int side, int delta) {
        amounts[side] = Math.clamp((long) amounts[side] + delta, 0, MAX_AMOUNT);
        resetIfAllZero();
    }

    /** 同期で受け取った値を設定する (クライアント側の表示用)。 */
    public void set(int side, int amount) {
        amounts[side] = Math.clamp(amount, 0, MAX_AMOUNT);
    }

    /**
     * 現在の面へアイテムを 1 つ送ったことを記録する。
     *
     * @return 現在の面の送出個数に達し、次の面へ切り替えるべきなら true
     */
    public boolean recordSent(int currentSide) {
        sentToCurrent++;
        if (sentToCurrent >= amounts[currentSide]) {
            sentToCurrent = 0;
            return true;
        }
        return false;
    }

    /** 送出個数が 0 の面へは送らない。 */
    public boolean isEnabled(int side) {
        return amounts[side] > 0;
    }

    private void resetIfAllZero() {
        for (int amount : amounts) {
            if (amount > 0) {
                return;
            }
        }
        Arrays.fill(amounts, DEFAULT_AMOUNT);
    }

    public void save(CompoundTag tag) {
        tag.putIntArray(TAG_AMOUNTS, amounts);
        tag.putInt(TAG_SENT, sentToCurrent);
    }

    /** 旧版と同じく、保存値が無い・全て 0 の場合は全面 1 とする。 */
    public void load(CompoundTag tag) {
        int[] saved = tag.getIntArray(TAG_AMOUNTS);
        Arrays.fill(amounts, DEFAULT_AMOUNT);
        if (saved.length == SIDES) {
            for (int side = 0; side < SIDES; side++) {
                amounts[side] = Math.clamp(saved[side], 0, MAX_AMOUNT);
            }
        }
        resetIfAllZero();
        sentToCurrent = Math.max(0, tag.getInt(TAG_SENT));
    }
}

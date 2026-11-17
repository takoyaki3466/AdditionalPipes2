package com.takoy3466.additional_pipes2.teleport;

import com.takoy3466.re_buildcraft.transport.PipeBlockEntity;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 1 つのディメンションで読み込まれているテレポートパイプの一覧 (旧 {@code PipeTeleport.teleportPipes})。
 * <p>
 * サーバー側の {@link net.minecraft.world.level.Level} に保存しないアタッチメントとして付けるため、
 * ワールドを閉じると一覧ごと破棄される。これは AdditionalPipesFix 1.0 の修正
 * (ワールド読み込み時に teleportPipes を作り直し、接続数が増え続けるのを防ぐ) と同じ効果を持つ。
 * <p>
 * 照合を速くするため、種類と周波数で分けて持つ。パイプは最初の tick に登録し、ブロックエンティティが
 * 取り除かれたとき (撤去・チャンクのアンロード) に外す。旧版の removeOldPipes はブロックごと消していたが
 * (Fix 1.0 で修正された「テレポートパイプが消える」不具合)、ここでは一覧から外すだけでワールドには触れない。
 * <p>
 * サーバーのメインスレッドからのみ使う。
 */
public final class TeleportRegistry {
    private final Map<TeleportKind, Map<Integer, Set<PipeBlockEntity>>> pipes = new EnumMap<>(TeleportKind.class);
    private final Map<PipeBlockEntity, Integer> registeredFrequency = new HashMap<>();

    public void register(PipeBlockEntity pipe, TeleportKind kind, int frequency) {
        unregister(pipe, kind);
        pipes.computeIfAbsent(kind, unused -> new HashMap<>())
                .computeIfAbsent(frequency, unused -> new LinkedHashSet<>())
                .add(pipe);
        registeredFrequency.put(pipe, frequency);
    }

    public void unregister(PipeBlockEntity pipe, TeleportKind kind) {
        Integer frequency = registeredFrequency.remove(pipe);
        if (frequency == null) {
            return;
        }
        Map<Integer, Set<PipeBlockEntity>> byFrequency = pipes.get(kind);
        if (byFrequency == null) {
            return;
        }
        Set<PipeBlockEntity> set = byFrequency.get(frequency);
        if (set != null) {
            set.remove(pipe);
            if (set.isEmpty()) {
                byFrequency.remove(frequency);
            }
        }
    }

    /** 同じ種類・周波数で登録されているパイプ (取り除かれたものは含めない)。 */
    public List<PipeBlockEntity> pipesOn(TeleportKind kind, int frequency) {
        Map<Integer, Set<PipeBlockEntity>> byFrequency = pipes.get(kind);
        if (byFrequency == null) {
            return List.of();
        }
        Set<PipeBlockEntity> set = byFrequency.get(frequency);
        if (set == null) {
            return List.of();
        }
        List<PipeBlockEntity> result = new ArrayList<>(set.size());
        for (PipeBlockEntity pipe : set) {
            if (!pipe.isRemoved()) {
                result.add(pipe);
            }
        }
        return result;
    }

    public boolean isRegistered(PipeBlockEntity pipe) {
        return registeredFrequency.containsKey(pipe);
    }

    /** 登録数 (テスト・デバッグ用)。 */
    public int size() {
        return registeredFrequency.size();
    }
}

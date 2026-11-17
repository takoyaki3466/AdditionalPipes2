package com.takoy3466.additional_pipes2.teleport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

/**
 * 周波数に付けた名前の帳簿。元実装: {@code FrequencyMap} + {@code BidiMap}。
 * <p>
 * 名前はテレポートの種類ごと・プレイヤーごとに独立しており、1 人のプレイヤーの中で
 * 周波数と名前は 1 対 1 に対応する (旧 GUI は使用済みの名前を登録させなかった。ここでは帳簿側でも拒否する)。
 * ワールドに依存しないため単体テストできる。保存は {@link FrequencyNamesData} が行う。
 */
public final class FrequencyNameBook {
    /** 名前の最大文字数 (旧 textboxName.setMaxStringLength(20))。 */
    public static final int MAX_NAME_LENGTH = 20;
    private static final String TAG_KIND = "kind";
    private static final String TAG_OWNER = "owner";
    private static final String TAG_ENTRIES = "entries";
    private static final String TAG_FREQUENCY = "freq";
    private static final String TAG_NAME = "name";

    private final Map<TeleportKind, Map<UUID, TreeMap<Integer, String>>> names = new EnumMap<>(TeleportKind.class);

    /** 周波数 {@code frequency} の名前。無ければ null。 */
    @Nullable
    public String nameOf(TeleportKind kind, UUID player, int frequency) {
        TreeMap<Integer, String> book = book(kind, player);
        return book == null ? null : book.get(frequency);
    }

    /** {@code name} が付いている周波数。 */
    public OptionalInt frequencyOf(TeleportKind kind, UUID player, String name) {
        TreeMap<Integer, String> book = book(kind, player);
        if (book != null) {
            for (Map.Entry<Integer, String> entry : book.entrySet()) {
                if (entry.getValue().equals(name)) {
                    return OptionalInt.of(entry.getKey());
                }
            }
        }
        return OptionalInt.empty();
    }

    /** 周波数順の一覧。 */
    public List<Entry> entries(TeleportKind kind, UUID player) {
        TreeMap<Integer, String> book = book(kind, player);
        if (book == null) {
            return List.of();
        }
        List<Entry> result = new ArrayList<>(book.size());
        book.forEach((frequency, name) -> result.add(new Entry(frequency, name)));
        return Collections.unmodifiableList(result);
    }

    /**
     * 名前を付ける (既に名前があれば置き換える)。
     *
     * @return 付けられたら true。名前が空・長すぎる・別の周波数で使用済みなら false
     */
    public boolean setName(TeleportKind kind, UUID player, int frequency, String name) {
        if (!isValidName(name)) {
            return false;
        }
        OptionalInt used = frequencyOf(kind, player, name);
        if (used.isPresent() && used.getAsInt() != frequency) {
            return false;
        }
        names.computeIfAbsent(kind, unused -> new HashMap<>())
                .computeIfAbsent(player, unused -> new TreeMap<>())
                .put(TeleportSettings.clampFrequency(frequency), name);
        return true;
    }

    /** @return 名前を消したら true */
    public boolean removeName(TeleportKind kind, UUID player, int frequency) {
        TreeMap<Integer, String> book = book(kind, player);
        return book != null && book.remove(frequency) != null;
    }

    public static boolean isValidName(String name) {
        return !name.isBlank() && name.length() <= MAX_NAME_LENGTH;
    }

    @Nullable
    private TreeMap<Integer, String> book(TeleportKind kind, UUID player) {
        Map<UUID, TreeMap<Integer, String>> byPlayer = names.get(kind);
        return byPlayer == null ? null : byPlayer.get(player);
    }

    public ListTag save() {
        ListTag list = new ListTag();
        names.forEach((kind, byPlayer) -> byPlayer.forEach((player, book) -> {
            if (book.isEmpty()) {
                return;
            }
            CompoundTag playerTag = new CompoundTag();
            playerTag.putString(TAG_KIND, kind.key());
            playerTag.putUUID(TAG_OWNER, player);
            ListTag entries = new ListTag();
            book.forEach((frequency, name) -> {
                CompoundTag entry = new CompoundTag();
                entry.putInt(TAG_FREQUENCY, frequency);
                entry.putString(TAG_NAME, name);
                entries.add(entry);
            });
            playerTag.put(TAG_ENTRIES, entries);
            list.add(playerTag);
        }));
        return list;
    }

    /**
     * @return 読み込めなかった項目の数 (種類が不明・名前が不正・重複)。呼び出し側でログに出す
     */
    public int load(ListTag list) {
        names.clear();
        int skipped = 0;
        for (int i = 0; i < list.size(); i++) {
            CompoundTag playerTag = list.getCompound(i);
            TeleportKind kind = kindOf(playerTag.getString(TAG_KIND));
            if (kind == null || !playerTag.hasUUID(TAG_OWNER)) {
                skipped++;
                continue;
            }
            UUID player = playerTag.getUUID(TAG_OWNER);
            ListTag entries = playerTag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND);
            for (int j = 0; j < entries.size(); j++) {
                CompoundTag entry = entries.getCompound(j);
                if (!setName(kind, player, entry.getInt(TAG_FREQUENCY), entry.getString(TAG_NAME))) {
                    skipped++;
                }
            }
        }
        return skipped;
    }

    @Nullable
    private static TeleportKind kindOf(String key) {
        for (TeleportKind kind : TeleportKind.values()) {
            if (kind.key().equals(key)) {
                return kind;
            }
        }
        return null;
    }

    /** 周波数と名前の組。 */
    public record Entry(int frequency, String name) {
    }
}

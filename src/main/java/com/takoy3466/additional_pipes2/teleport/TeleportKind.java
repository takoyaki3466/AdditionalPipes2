package com.takoy3466.additional_pipes2.teleport;

import java.util.Locale;

/**
 * テレポートパイプが運ぶもの。リンクと周波数の名前は種類ごとに独立している
 * (旧版はパイプのクラスごとに teleportPipes の照合と {@code ap<種類>freqmap.dat} を分けていた)。
 */
public enum TeleportKind {
    ITEMS,
    FLUIDS,
    POWER;

    /** 保存データのキー。 */
    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }
}

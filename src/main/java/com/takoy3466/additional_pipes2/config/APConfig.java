package com.takoy3466.additional_pipes2.config;

import java.util.EnumMap;
import java.util.Map;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 旧 {@code config/AdditionalPipes.cfg} の項目。
 * <p>
 * ブロック ID / アイテム ID の項目 ({@code ItemTeleport.Id} など) は 1.21.1 ではレジストリ名に置き換わるため存在しない。
 * 旧 {@code saveLagFix} は旧版でも読み込むだけで参照されていなかったため移行していない (MIGRATION_STATUS.md 参照)。
 */
public final class APConfig {
    /** 旧 logLevel の範囲 (0 = 出力しない, 1 = エラー, 2 = 警告, 3 = 情報)。 */
    public static final int LOG_LEVEL_MAX = 3;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** 旧 general.wrenchOpensGui: レンチで右クリックしてもテレポートパイプの画面を開く。 */
    public static final ModConfigSpec.BooleanValue WRENCH_OPENS_GUI;
    /** 旧 general.logLevel。 */
    public static final ModConfigSpec.IntValue LOG_LEVEL;

    private static final Map<FeatureToggle, ModConfigSpec.BooleanValue> FEATURES = new EnumMap<>(FeatureToggle.class);

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("general");
        WRENCH_OPENS_GUI = BUILDER
                .comment("Open the teleport pipe GUI even when right-clicking with a wrench (old wrenchOpensGui).")
                .define("wrenchOpensGui", false);
        LOG_LEVEL = BUILDER
                .comment("Amount of Additional Pipes log output: 0 = none, 1 = errors, 2 = warnings, 3 = info (old logLevel).")
                .defineInRange("logLevel", 0, 0, LOG_LEVEL_MAX);
        BUILDER.pop();

        for (FeatureToggle.Category category : FeatureToggle.Category.values()) {
            BUILDER.push(category.configSection());
            for (FeatureToggle toggle : FeatureToggle.values()) {
                if (toggle.category() == category) {
                    FEATURES.put(toggle, BUILDER.comment(toggle.comment()).define(toggle.configKey(), toggle.defaultValue()));
                }
            }
            BUILDER.pop();
        }
        SPEC = BUILDER.build();
    }

    private APConfig() {
    }

    /**
     * 機能スイッチの現在値。設定ファイルが読み込まれる前 (レシピ条件が先に評価された場合など) は既定値を返す。
     */
    public static boolean isEnabled(FeatureToggle toggle) {
        ModConfigSpec.BooleanValue value = FEATURES.get(toggle);
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    public static boolean wrenchOpensGui() {
        return SPEC.isLoaded() ? WRENCH_OPENS_GUI.get() : WRENCH_OPENS_GUI.getDefault();
    }

    public static int logLevel() {
        return SPEC.isLoaded() ? LOG_LEVEL.get() : LOG_LEVEL.getDefault();
    }
}

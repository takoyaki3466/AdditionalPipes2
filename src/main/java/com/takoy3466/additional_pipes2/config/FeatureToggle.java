package com.takoy3466.additional_pipes2.config;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

/**
 * 旧設定ファイルの機能スイッチ (enableItemTeleport など)。
 * <p>
 * 旧版はスイッチが切られたパイプを登録自体しなかったが、1.21.1 ではレジストリの内容をサーバーとクライアントで
 * 一致させる必要があり、設定ファイルで登録を変えることはできない。そのため、スイッチは
 * 対応するレシピの有効・無効 (レシピ条件 {@code additional_pipes2:feature_enabled}) として再現する。
 */
public enum FeatureToggle implements StringRepresentable {
    ITEM_TELEPORT(Category.ITEMS, "enableItemTeleport", true, "Enable the recipe of the Item Teleport Pipe."),
    ADVANCED_INSERTION(Category.ITEMS, "enableItemsAdvancedInsertion", true, "Enable the recipe of the Advanced Insertion Pipe."),
    ADVANCED_WOOD(Category.ITEMS, "enableItemsAdvancedWood", true, "Enable the recipe of the Advanced Wooden Transport Pipe."),
    DISTRIBUTOR(Category.ITEMS, "enableItemsDistributor", true, "Enable the recipe of the Distribution Transport Pipe."),
    ITEMS_REDSTONE(Category.ITEMS, "enableItemsRedstone", true, "Enable the recipe of the Redstone Transport Pipe."),
    LIQUIDS_REDSTONE(Category.ITEMS, "enableLiquidsRedstone", true, "Enable the recipe of the Waterproof Redstone Pipe."),
    LIQUIDS_TELEPORT(Category.ITEMS, "enableLiquidsTeleport", true, "Enable the recipe of the Waterproof Teleport Pipe."),
    POWER_TELEPORT(Category.ITEMS, "enablePowerTeleport", true, "Enable the recipe of the Power Teleport Pipe."),
    WATERPROOF_REMOVAL(Category.GENERAL_RECIPES, "enableWaterproofRemoval", false,
            "Add recipes that turn waterproof / conductive pipes back into transport pipes (old EnableWaterProofRemoval)."),
    TELEPORT_TETHER(Category.BLOCKS, "teleportTetherCraftable", true, "Enable the recipe of the Teleport Tether (chunk loader).");

    public static final Codec<FeatureToggle> CODEC = StringRepresentable.fromEnum(FeatureToggle::values);

    private final Category category;
    private final String configKey;
    private final boolean defaultValue;
    private final String comment;

    FeatureToggle(Category category, String configKey, boolean defaultValue, String comment) {
        this.category = category;
        this.configKey = configKey;
        this.defaultValue = defaultValue;
        this.comment = comment;
    }

    public Category category() {
        return category;
    }

    public String configKey() {
        return configKey;
    }

    public boolean defaultValue() {
        return defaultValue;
    }

    public String comment() {
        return comment;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** 設定ファイル上の区分 (旧ファイルの item / general / block 区分に対応)。 */
    public enum Category {
        ITEMS("items"),
        GENERAL_RECIPES("recipes"),
        BLOCKS("blocks");

        private final String configSection;

        Category(String configSection) {
            this.configSection = configSection;
        }

        public String configSection() {
            return configSection;
        }
    }
}

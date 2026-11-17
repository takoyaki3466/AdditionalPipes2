package com.takoy3466.additional_pipes2.config;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * 機能スイッチが有効なときだけレシピを読み込むレシピ条件。
 * JSON: {@code {"type": "additional_pipes2:feature_enabled", "feature": "item_teleport"}}
 */
public record FeatureEnabledCondition(FeatureToggle feature) implements ICondition {
    public static final MapCodec<FeatureEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FeatureToggle.CODEC.fieldOf("feature").forGetter(FeatureEnabledCondition::feature)
    ).apply(instance, FeatureEnabledCondition::new));

    @Override
    public boolean test(IContext context) {
        return APConfig.isEnabled(feature);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}

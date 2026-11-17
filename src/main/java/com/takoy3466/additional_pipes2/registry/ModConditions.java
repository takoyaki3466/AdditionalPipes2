package com.takoy3466.additional_pipes2.registry;

import com.mojang.serialization.MapCodec;
import com.takoy3466.additional_pipes2.AdditionalPipes2;
import com.takoy3466.additional_pipes2.config.FeatureEnabledCondition;
import java.util.function.Supplier;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** レシピ条件の登録 (旧設定の機能スイッチをレシピの有効・無効として反映する)。 */
public final class ModConditions {
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, AdditionalPipes2.MODID);

    /** {@code additional_pipes2:feature_enabled}。{@link Recipe} の JSON の {@code neoforge:conditions} で使う。 */
    public static final Supplier<MapCodec<FeatureEnabledCondition>> FEATURE_ENABLED =
            CONDITION_CODECS.register("feature_enabled", () -> FeatureEnabledCondition.CODEC);

    private ModConditions() {
    }

    public static void register(IEventBus modEventBus) {
        CONDITION_CODECS.register(modEventBus);
    }
}

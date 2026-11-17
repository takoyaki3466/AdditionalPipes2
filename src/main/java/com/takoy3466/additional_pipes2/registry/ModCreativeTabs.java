package com.takoy3466.additional_pipes2.registry;

import com.takoy3466.additional_pipes2.AdditionalPipes2;
import com.takoy3466.additional_pipes2.pipe.APPipeType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * クリエイティブタブ。登録済みの全アイテムを登録順に並べる
 * (1.2.5 には Mod 用タブが無く、旧版はテレポートテザーを addCreativeItems で既存の一覧に追加していた)。
 */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AdditionalPipes2.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + AdditionalPipes2.MODID))
                    .icon(() -> ModBlocks.pipes().get(APPipeType.ITEM_TELEPORT).get().asItem().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        for (var entry : ModItems.ITEMS.getEntries()) {
                            output.accept(entry.get());
                        }
                    })
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}

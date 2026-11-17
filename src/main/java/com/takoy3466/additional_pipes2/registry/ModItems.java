package com.takoy3466.additional_pipes2.registry;

import com.takoy3466.additional_pipes2.AdditionalPipes2;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Additional Pipes のアイテム登録。全てブロックアイテム (旧版のパイプアイテムとテレポートテザー)。 */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AdditionalPipes2.MODID);

    static {
        ModBlocks.pipes().values().forEach(ITEMS::registerSimpleBlockItem);
    }

    public static final DeferredItem<BlockItem> TELEPORT_TETHER = ITEMS.registerSimpleBlockItem(ModBlocks.TELEPORT_TETHER);

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}

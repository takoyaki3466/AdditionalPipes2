package com.takoy3466.additional_pipes2.registry;

import com.takoy3466.additional_pipes2.AdditionalPipes2;
import com.takoy3466.additional_pipes2.teleport.TeleportRegistry;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** データアタッチメントの登録。 */
public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, AdditionalPipes2.MODID);

    /**
     * ディメンションごとのテレポートパイプ一覧。保存しない (serializer を持たない) ため、
     * ワールドを閉じると破棄され、次に開いたときは空から作り直される (AdditionalPipesFix 1.0 の修正に相当)。
     */
    public static final Supplier<AttachmentType<TeleportRegistry>> TELEPORT_REGISTRY = ATTACHMENT_TYPES.register(
            "teleport_registry", () -> AttachmentType.builder(TeleportRegistry::new).build());

    private ModAttachments() {
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}

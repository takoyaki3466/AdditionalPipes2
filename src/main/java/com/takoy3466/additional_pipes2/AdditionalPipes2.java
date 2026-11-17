package com.takoy3466.additional_pipes2;

import com.mojang.logging.LogUtils;
import com.takoy3466.additional_pipes2.chunkloader.ChunkLoaderTickets;
import com.takoy3466.additional_pipes2.config.APConfig;
import com.takoy3466.additional_pipes2.network.ModPayloads;
import com.takoy3466.additional_pipes2.registry.ModAttachments;
import com.takoy3466.additional_pipes2.registry.ModBlockEntities;
import com.takoy3466.additional_pipes2.registry.ModBlocks;
import com.takoy3466.additional_pipes2.registry.ModConditions;
import com.takoy3466.additional_pipes2.registry.ModCreativeTabs;
import com.takoy3466.additional_pipes2.registry.ModItems;
import com.takoy3466.additional_pipes2.registry.ModMenus;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Additional Pipes 3.1.0 (Minecraft 1.2.5) の Minecraft 1.21.1 / NeoForge 向け移行版のエントリーポイント。
 * <p>
 * Re-BuildCraft のアドオンとして動作し、パイプの輸送処理・描画・ケーパビリティは Re-BuildCraft のものを共有する。
 * ここでは各レジストリとイベントの接続だけを行い、ゲームロジックは各機能パッケージに置く。
 */
@Mod(AdditionalPipes2.MODID)
public final class AdditionalPipes2 {
    public static final String MODID = "additional_pipes2";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AdditionalPipes2(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModConditions.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        modEventBus.addListener(ModBlocks::addPipesToSharedBlockEntityType);
        modEventBus.addListener(ChunkLoaderTickets::registerController);
        modEventBus.addListener(ModPayloads::register);

        modContainer.registerConfig(ModConfig.Type.COMMON, APConfig.SPEC);
    }

    /** この Mod の名前空間を持つ {@link ResourceLocation} を作る。 */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}

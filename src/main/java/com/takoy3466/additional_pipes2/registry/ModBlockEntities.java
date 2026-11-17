package com.takoy3466.additional_pipes2.registry;

import com.takoy3466.additional_pipes2.AdditionalPipes2;
import com.takoy3466.additional_pipes2.chunkloader.ChunkLoaderBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Additional Pipes のブロックエンティティ登録。
 * パイプは Re-BuildCraft の {@code re_buildcraft:pipe} を共有するため ({@link ModBlocks#addPipesToSharedBlockEntityType})、
 * ここではテレポートテザーだけを登録する。
 */
public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AdditionalPipes2.MODID);

    /** 旧 ModLoader.registerTileEntity(TileChunkLoader.class, "Teleport Tether")。 */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChunkLoaderBlockEntity>> TELEPORT_TETHER =
            BLOCK_ENTITY_TYPES.register("teleport_tether", () -> BlockEntityType.Builder.of(
                    ChunkLoaderBlockEntity::new, ModBlocks.TELEPORT_TETHER.get()).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}

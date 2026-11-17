package com.takoy3466.additional_pipes2.registry;

import com.takoy3466.additional_pipes2.AdditionalPipes2;
import com.takoy3466.additional_pipes2.chunkloader.ChunkLoaderBlock;
import com.takoy3466.additional_pipes2.pipe.APPipeType;
import com.takoy3466.additional_pipes2.pipe.RedstonePipeBlock;
import com.takoy3466.re_buildcraft.transport.DirectionalPipeBlock;
import com.takoy3466.re_buildcraft.transport.PipeBlock;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Additional Pipes のブロック登録。 */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AdditionalPipes2.MODID);

    /**
     * テレポートテザーの硬さ。旧版は硬さを設定しておらず (0, 素手で即座に壊れる)、誤って壊すと読み込みが止まるため、
     * 木材程度の軽い硬さにしている (MIGRATION_STATUS.md 参照)。
     */
    private static final float TETHER_HARDNESS = 1.0F;

    private static final Map<APPipeType, DeferredBlock<PipeBlock>> PIPES = new EnumMap<>(APPipeType.class);

    static {
        for (APPipeType type : APPipeType.values()) {
            PIPES.put(type, BLOCKS.register(type.id(), () -> createPipeBlock(type)));
        }
    }

    /** テレポートテザー (旧ブロック名 "Teleport Tether", 旧 ID 189)。 */
    public static final DeferredBlock<ChunkLoaderBlock> TELEPORT_TETHER = BLOCKS.register("teleport_tether",
            () -> new ChunkLoaderBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(TETHER_HARDNESS)
                    .sound(SoundType.METAL)));

    private ModBlocks() {
    }

    private static PipeBlock createPipeBlock(APPipeType type) {
        if (type.directional()) {
            return new DirectionalPipeBlock(pipeProperties(), type);
        }
        if (type.emitsRedstone()) {
            return new RedstonePipeBlock(pipeProperties(), type);
        }
        return new PipeBlock(pipeProperties(), type);
    }

    /** Re-BuildCraft のパイプと同じ設定 (旧 BlockGenericPipe: ガラス素材・硬さ 0)。 */
    private static BlockBehaviour.Properties pipeProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .instabreak()
                .noOcclusion();
    }

    public static Map<APPipeType, DeferredBlock<PipeBlock>> pipes() {
        return Collections.unmodifiableMap(PIPES);
    }

    /**
     * このアドオンのパイプを Re-BuildCraft のパイプ用ブロックエンティティ型 ({@code re_buildcraft:pipe}) に追加する。
     * これにより輸送処理・描画 (PipeRenderer)・液体/MJ のケーパビリティを Re-BuildCraft のパイプと共有する。
     */
    public static void addPipesToSharedBlockEntityType(BlockEntityTypeAddBlocksEvent event) {
        event.modify(com.takoy3466.re_buildcraft.registry.ModBlockEntities.PIPE.get(), PIPES.values().stream().map(DeferredBlock::get).toArray(Block[]::new));
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}

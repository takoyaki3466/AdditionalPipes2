package com.takoy3466.additional_pipes2.teleport;

import com.takoy3466.additional_pipes2.config.APConfig;
import com.takoy3466.additional_pipes2.menu.TeleportPipeMenu;
import com.takoy3466.additional_pipes2.pipe.APPipeType;
import com.takoy3466.additional_pipes2.registry.ModAttachments;
import com.takoy3466.re_buildcraft.transport.PipeBlockEntity;
import com.takoy3466.re_buildcraft.transport.behavior.PipeBehavior;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * テレポートパイプ 3 種 (アイテム・液体・動力) に共通する、周波数・受信設定・所有者・リンク検索の処理。
 * 元実装: {@code PipeTeleport} + {@code PipeLogicTeleport}。
 * <p>
 * リンクの条件 (旧 getConnectedPipes):
 * <ul>
 *   <li>同じ種類のテレポートパイプで、同じディメンションにあり、読み込まれている</li>
 *   <li>同じ周波数</li>
 *   <li>マルチプレイでは所有者が同じ (シングルプレイ・LAN 公開中のホストでは問わない)</li>
 *   <li>送り先として使うのは「受信」が有効なパイプだけ (接続数の表示などでは受信設定を問わない)</li>
 * </ul>
 * 旧版の一覧は全ディメンション共通だったが、送り先の参照は送り元のワールドの座標で行われていたため
 * ディメンションをまたいだ転送は実際には成立しなかった。ここでは同じディメンションに限定する。
 */
public abstract class AbstractTeleportBehavior implements PipeBehavior {
    private static final String TITLE_KEY = "container.additional_pipes2.teleport_pipe";

    private final TeleportSettings settings = new TeleportSettings();

    /** このパイプが運ぶもの。 */
    public abstract TeleportKind kind();

    public TeleportSettings settings() {
        return settings;
    }

    // ---- 登録 -----------------------------------------------------------------

    @Override
    public void initialize(PipeBlockEntity pipe) {
        TeleportRegistry registry = registry(pipe.getLevel());
        if (registry != null) {
            registry.register(pipe, kind(), settings.frequency());
        }
    }

    @Override
    public void onRemoved(PipeBlockEntity pipe) {
        TeleportRegistry registry = registry(pipe.getLevel());
        if (registry != null) {
            registry.unregister(pipe, kind());
        }
    }

    @Nullable
    private static TeleportRegistry registry(@Nullable Level level) {
        return level instanceof ServerLevel serverLevel ? serverLevel.getData(ModAttachments.TELEPORT_REGISTRY) : null;
    }

    // ---- 所有者と操作 -------------------------------------------------------------

    @Override
    public void onPlaced(PipeBlockEntity pipe, LivingEntity placer) {
        if (placer instanceof Player player && !pipe.getLevel().isClientSide) {
            claim(pipe, player);
        }
    }

    private void claim(PipeBlockEntity pipe, Player player) {
        if (settings.claimIfUnowned(player.getUUID(), player.getGameProfile().getName())) {
            pipe.setChanged();
            pipe.scheduleSync();
        }
    }

    @Override
    public MenuProvider menuProvider(PipeBlockEntity pipe) {
        return new SimpleMenuProvider((containerId, playerInventory, player) -> {
            claim(pipe, player);
            return new TeleportPipeMenu(containerId, playerInventory, pipe, this);
        }, Component.translatable(TITLE_KEY));
    }

    /** 設定 wrenchOpensGui が有効ならレンチでも画面を開く。無効ならレンチでは何もしない (旧 blockActivated)。 */
    @Override
    public boolean onWrench(PipeBlockEntity pipe, Player player) {
        if (!APConfig.wrenchOpensGui()) {
            return false;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(menuProvider(pipe), pipe.getBlockPos());
        }
        return true;
    }

    /** 周波数を変える (サーバー側, GUI から)。 */
    public void setFrequency(PipeBlockEntity pipe, int frequency) {
        settings.setFrequency(frequency);
        TeleportRegistry registry = registry(pipe.getLevel());
        if (registry != null && registry.isRegistered(pipe)) {
            registry.register(pipe, kind(), settings.frequency());
        }
        pipe.setChanged();
    }

    /** 受信の有無を切り替える (サーバー側, GUI から)。 */
    public void toggleReceive(PipeBlockEntity pipe) {
        settings.toggleReceive();
        pipe.setChanged();
    }

    // ---- リンク ---------------------------------------------------------------

    /**
     * このパイプとリンクしている他のテレポートパイプ (旧 getConnectedPipes)。
     *
     * @param includeNonReceivers true なら受信が無効なパイプも含める
     */
    public List<Link> linkedPipes(PipeBlockEntity pipe, boolean includeNonReceivers) {
        TeleportRegistry registry = registry(pipe.getLevel());
        if (registry == null) {
            return List.of();
        }
        boolean ownersMustMatch = ownersMustMatch(pipe.getLevel());
        List<Link> links = new ArrayList<>();
        for (PipeBlockEntity other : registry.pipesOn(kind(), settings.frequency())) {
            if (other == pipe || !(other.behavior() instanceof AbstractTeleportBehavior otherBehavior)) {
                continue;
            }
            TeleportSettings otherSettings = otherBehavior.settings();
            if ((otherSettings.canReceive() || includeNonReceivers) && settings.sharesNetworkWith(otherSettings, ownersMustMatch)) {
                links.add(new Link(other, otherBehavior));
            }
        }
        return links;
    }

    /** シングルプレイ (統合サーバー) では所有者を問わない (旧 MutiPlayerProxy.isOnServer)。 */
    private static boolean ownersMustMatch(Level level) {
        MinecraftServer server = level.getServer();
        return server == null || !server.isSingleplayer();
    }

    /** テレポートパイプ (種類を問わない) か。テレポート先の隣接先からは除外する (転送の連鎖・無限再帰を防ぐ)。 */
    protected static boolean isTeleportPipe(@Nullable BlockEntity blockEntity) {
        return blockEntity instanceof PipeBlockEntity pipe && pipe.type() instanceof APPipeType type && type.isTeleport();
    }

    @Override
    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        settings.save(tag);
    }

    @Override
    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        settings.load(tag);
    }

    /** リンク先のパイプとその振る舞い。 */
    public record Link(PipeBlockEntity pipe, AbstractTeleportBehavior behavior) {
    }
}

package com.takoy3466.additional_pipes2.menu;

import com.takoy3466.additional_pipes2.network.TeleportPipeActionPayload;
import com.takoy3466.additional_pipes2.network.TeleportPipeStatePayload;
import com.takoy3466.additional_pipes2.registry.ModMenus;
import com.takoy3466.additional_pipes2.teleport.AbstractTeleportBehavior;
import com.takoy3466.additional_pipes2.teleport.FrequencyNameBook;
import com.takoy3466.additional_pipes2.teleport.FrequencyNamesData;
import com.takoy3466.additional_pipes2.teleport.TeleportSettings;
import com.takoy3466.re_buildcraft.transport.PipeBlockEntity;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * テレポートパイプの設定画面のメニュー。元実装: {@code GuiTeleportPipeNew} (旧版は GUI が直接パイプの値を書き換えていた)。
 * <p>
 * 1.21.1 では画面の操作はクライアントから {@link TeleportPipeActionPayload} で送り、このメニューがサーバー側で
 * 検証して反映する。表示内容 ({@link TeleportPipeView}) は変化したときにサーバーから {@link TeleportPipeStatePayload} で送る。
 * 周波数は最大 99999 で、メニューの数値同期 (16 bit) に収まらないため独自のペイロードを使う。
 */
public class TeleportPipeMenu extends AbstractContainerMenu {
    @Nullable
    private final PipeBlockEntity pipe;
    @Nullable
    private final AbstractTeleportBehavior behavior;
    @Nullable
    private final ServerPlayer serverPlayer;
    /** サーバー側: 最後に送った状態。クライアント側: 最後に受け取った状態。 */
    private TeleportPipeView view = TeleportPipeView.EMPTY;
    private boolean viewSent;

    public TeleportPipeMenu(int containerId, Inventory playerInventory, PipeBlockEntity pipe, AbstractTeleportBehavior behavior) {
        super(ModMenus.TELEPORT_PIPE.get(), containerId);
        this.pipe = pipe;
        this.behavior = behavior;
        this.serverPlayer = playerInventory.player instanceof ServerPlayer player ? player : null;
    }

    private TeleportPipeMenu(int containerId) {
        super(ModMenus.TELEPORT_PIPE.get(), containerId);
        this.pipe = null;
        this.behavior = null;
        this.serverPlayer = null;
    }

    /** クライアント側。表示内容はサーバーからのペイロードで届く。 */
    public static TeleportPipeMenu createClient(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        extraData.readBlockPos();
        return new TeleportPipeMenu(containerId);
    }

    public TeleportPipeView view() {
        return view;
    }

    /** クライアント側: サーバーから届いた表示内容を反映する。 */
    public void applyView(TeleportPipeView newView) {
        this.view = newView;
    }

    // ---- サーバー側の操作 -----------------------------------------------------------

    /**
     * クライアントからの操作を反映する。値はここで検証し、範囲外の周波数は丸め、不正な名前は無視する。
     */
    public void handleAction(ServerPlayer player, TeleportPipeActionPayload.Action action, int value, String text) {
        if (pipe == null || behavior == null || !stillValid(player)) {
            return;
        }
        TeleportSettings settings = behavior.settings();
        switch (action) {
            case SET_FREQUENCY -> behavior.setFrequency(pipe, value);
            case TOGGLE_RECEIVE -> behavior.toggleReceive(pipe);
            case ADD_NAME -> {
                String name = StringUtil.filterText(text).strip();
                names(player).edit(book -> book.setName(behavior.kind(), player.getUUID(), settings.frequency(), name));
            }
            case REMOVE_NAME -> names(player).edit(book -> book.removeName(behavior.kind(), player.getUUID(), settings.frequency()));
        }
        broadcastChanges();
    }

    private static FrequencyNamesData names(ServerPlayer player) {
        MinecraftServer server = player.server;
        return FrequencyNamesData.get(server);
    }

    private TeleportPipeView currentView() {
        if (pipe == null || behavior == null || serverPlayer == null) {
            return TeleportPipeView.EMPTY;
        }
        TeleportSettings settings = behavior.settings();
        UUID viewer = serverPlayer.getUUID();
        FrequencyNameBook book = names(serverPlayer).book();
        return new TeleportPipeView(settings.frequency(), settings.canReceive(), behavior.linkedPipes(pipe, true).size(),
                settings.ownerName(), book.entries(behavior.kind(), viewer));
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        sendViewIfChanged(false);
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        sendViewIfChanged(true);
    }

    private void sendViewIfChanged(boolean force) {
        if (serverPlayer == null) {
            return;
        }
        TeleportPipeView current = currentView();
        if (force || !viewSent || !current.equals(view)) {
            view = current;
            viewSent = true;
            PacketDistributor.sendToPlayer(serverPlayer, new TeleportPipeStatePayload(containerId, current));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return pipe == null || Container.stillValidBlockEntity(pipe, player);
    }
}

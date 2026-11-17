package com.takoy3466.additional_pipes2.menu;

import com.takoy3466.additional_pipes2.pipe.behavior.AdvancedWoodBehavior;
import com.takoy3466.additional_pipes2.registry.ModMenus;
import com.takoy3466.re_buildcraft.api.menu.PlayerInventorySlots;
import com.takoy3466.re_buildcraft.transport.PipeBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 高度な木製パイプのフィルター画面のメニュー。元実装: {@code CraftingAdvancedWoodPipe} + {@code GuiAdvancedWoodPipe}。
 * <p>
 * フィルター枠は見本なので、クリックするとカーソルのアイテムの写し (1 個) を置き、素手なら消す
 * (Re-BuildCraft のダイヤパイプと同じ操作)。「必要 / 除外」の切り替えはメニューボタン 0 番。
 */
public class AdvancedWoodMenu extends AbstractContainerMenu {
    public static final int BUTTON_TOGGLE_EXCLUDE = 0;
    private static final int SLOT_SIZE = 18;
    private static final int FILTER_LEFT = 8;
    private static final int FILTER_TOP = 18;
    /** 旧 CraftingAdvancedWoodPipe のプレイヤーインベントリ位置。 */
    private static final int PLAYER_INVENTORY_Y = 76;

    @Nullable
    private final PipeBlockEntity pipe;
    @Nullable
    private final AdvancedWoodBehavior behavior;
    private final Container filters;
    private final DataSlot exclude;

    public AdvancedWoodMenu(int containerId, Inventory playerInventory, PipeBlockEntity pipe, AdvancedWoodBehavior behavior) {
        this(containerId, playerInventory, pipe, behavior, behavior.filters(), new ExcludeSlot(behavior));
    }

    private AdvancedWoodMenu(int containerId, Inventory playerInventory, @Nullable PipeBlockEntity pipe,
                             @Nullable AdvancedWoodBehavior behavior, Container filters, DataSlot exclude) {
        super(ModMenus.ADVANCED_WOODEN_PIPE.get(), containerId);
        this.pipe = pipe;
        this.behavior = behavior;
        this.filters = filters;
        this.exclude = exclude;
        for (int column = 0; column < AdvancedWoodBehavior.FILTER_SLOTS; column++) {
            addSlot(new FilterSlot(filters, column, FILTER_LEFT + column * SLOT_SIZE, FILTER_TOP));
        }
        PlayerInventorySlots.add(this::addSlot, playerInventory, PLAYER_INVENTORY_Y);
        addDataSlot(exclude);
    }

    /** クライアント側。フィルターと除外モードはメニューの同期で届く。 */
    public static AdvancedWoodMenu createClient(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        extraData.readBlockPos();
        return new AdvancedWoodMenu(containerId, playerInventory, null, null,
                new SimpleContainer(AdvancedWoodBehavior.FILTER_SLOTS), DataSlot.standalone());
    }

    public boolean isExclude() {
        return exclude.get() != 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_TOGGLE_EXCLUDE || pipe == null || behavior == null) {
            return false;
        }
        behavior.toggleExclude(pipe);
        return true;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < AdvancedWoodBehavior.FILTER_SLOTS) {
            ItemStack carried = getCarried();
            filters.setItem(slotId, carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1));
            if (pipe != null) {
                pipe.setChanged();
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // 見本の枠へは移動させない。
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return pipe == null || Container.stillValidBlockEntity(pipe, player);
    }

    /** 見本用の枠。直接の出し入れはできない。 */
    private static final class FilterSlot extends Slot {
        private FilterSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }

    /** サーバー側: 除外モードを 0/1 で見せる。 */
    private static final class ExcludeSlot extends DataSlot {
        private final AdvancedWoodBehavior behavior;

        private ExcludeSlot(AdvancedWoodBehavior behavior) {
            this.behavior = behavior;
        }

        @Override
        public int get() {
            return behavior.isExclude() ? 1 : 0;
        }

        @Override
        public void set(int value) {
            // サーバー側の値はボタン操作でのみ変える。
        }
    }
}

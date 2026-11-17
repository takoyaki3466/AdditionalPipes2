package com.takoy3466.additional_pipes2.menu;

import com.takoy3466.additional_pipes2.pipe.behavior.DistributionCounts;
import com.takoy3466.additional_pipes2.pipe.behavior.DistributorBehavior;
import com.takoy3466.additional_pipes2.registry.ModMenus;
import com.takoy3466.re_buildcraft.transport.PipeBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 分配パイプの設定画面のメニュー。元実装: {@code GuiDistributionPipe} (旧版は GUI が直接値を書き換えていた)。
 * <p>
 * 面ごとの送出個数は {@link ContainerData} で同期し、+/- ボタンはメニューボタン
 * ({@link #clickMenuButton}) としてサーバーで処理する。ボタン番号は「面 × 2 + (0: 減らす, 1: 増やす)」。
 */
public class DistributorMenu extends AbstractContainerMenu {
    public static final int BUTTONS_PER_SIDE = 2;
    private static final int INCREMENT = 1;

    @Nullable
    private final PipeBlockEntity pipe;
    @Nullable
    private final DistributorBehavior behavior;
    private final ContainerData amounts;

    public DistributorMenu(int containerId, Inventory playerInventory, PipeBlockEntity pipe, DistributorBehavior behavior) {
        this(containerId, pipe, behavior, new CountsData(behavior.counts()));
    }

    private DistributorMenu(int containerId, @Nullable PipeBlockEntity pipe, @Nullable DistributorBehavior behavior, ContainerData amounts) {
        super(ModMenus.DISTRIBUTION_PIPE.get(), containerId);
        this.pipe = pipe;
        this.behavior = behavior;
        this.amounts = amounts;
        addDataSlots(amounts);
    }

    /** クライアント側。値は ContainerData の同期で届く。 */
    public static DistributorMenu createClient(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        extraData.readBlockPos();
        return new DistributorMenu(containerId, null, null, new SimpleContainerData(DistributionCounts.SIDES));
    }

    public int amount(int side) {
        return amounts.get(side);
    }

    public static int buttonId(int side, boolean increase) {
        return side * BUTTONS_PER_SIDE + (increase ? 1 : 0);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (pipe == null || behavior == null || id < 0 || id >= DistributionCounts.SIDES * BUTTONS_PER_SIDE) {
            return false;
        }
        int side = id / BUTTONS_PER_SIDE;
        boolean increase = id % BUTTONS_PER_SIDE == 1;
        behavior.adjustAmount(pipe, side, increase ? INCREMENT : -INCREMENT);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return pipe == null || Container.stillValidBlockEntity(pipe, player);
    }

    /** サーバー側: 分配パイプの値をそのまま見せる ContainerData。 */
    private record CountsData(DistributionCounts counts) implements ContainerData {
        @Override
        public int get(int index) {
            return counts.amount(index);
        }

        @Override
        public void set(int index, int value) {
            counts.set(index, value);
        }

        @Override
        public int getCount() {
            return DistributionCounts.SIDES;
        }
    }
}

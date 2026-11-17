package com.takoy3466.additional_pipes2.pipe.behavior;

import com.takoy3466.additional_pipes2.menu.AdvancedWoodMenu;
import com.takoy3466.re_buildcraft.api.power.PowerProvider;
import com.takoy3466.re_buildcraft.transport.ItemTransport;
import com.takoy3466.re_buildcraft.transport.PipeBlockEntity;
import com.takoy3466.re_buildcraft.transport.TravelingItem;
import com.takoy3466.re_buildcraft.transport.behavior.DiamondFilterBehavior;
import com.takoy3466.re_buildcraft.transport.behavior.PipeBehavior;
import com.takoy3466.re_buildcraft.transport.behavior.WoodSource;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * 高度な木製パイプ。木のパイプと同じく MJ を受けて取り出し元のインベントリからアイテムを取り出すが、
 * 9 個のフィルターに一致するアイテムだけ (除外モードでは一致しないアイテムだけ) を取り出す。
 * 元実装: {@code PipeItemsAdvancedWood} + {@code PipeLogicAdvancedWood}。
 * <p>
 * 旧版のフィルター枠は実物のアイテムを 1 個ずつ置く枠だったが、Re-BuildCraft のダイヤパイプと同じく
 * 見本 (写し) を置く枠にしている。そのためパイプを壊してもフィルターのアイテムは落ちない。
 */
public class AdvancedWoodBehavior implements PipeBehavior {
    public static final int FILTER_SLOTS = 9;
    /** 旧 configure(latency 50, min 1, max 64, activation 1, maxStored 64) から latency を除いたもの。 */
    private static final int MIN_ENERGY_RECEIVED = 1;
    private static final int MAX_ENERGY = 64;
    private static final int MIN_ACTIVATION_ENERGY = 1;
    /** 旧 configurePowerPerdition(64, 1)。 */
    private static final int POWER_LOSS = 64;
    private static final int POWER_LOSS_REGULARITY = 1;
    /** 1 MJ で 1 個を取り出す。 */
    private static final float ENERGY_PER_ITEM = 1.0F;
    private static final String TITLE_KEY = "container.additional_pipes2.advanced_wooden_pipe";
    private static final String TAG_EXCLUDE = "exclude";
    private static final String TAG_FILTERS = "filters";

    private final PowerProvider powerProvider = new PowerProvider();
    private final SimpleContainer filters = new SimpleContainer(FILTER_SLOTS);
    private boolean exclude;

    public AdvancedWoodBehavior() {
        powerProvider.configure(MIN_ENERGY_RECEIVED, MAX_ENERGY, MIN_ACTIVATION_ENERGY, MAX_ENERGY);
        powerProvider.configurePowerPerdition(POWER_LOSS, POWER_LOSS_REGULARITY);
    }

    @Override
    public PowerProvider powerProvider() {
        return powerProvider;
    }

    public SimpleContainer filters() {
        return filters;
    }

    public boolean isExclude() {
        return exclude;
    }

    /** GUI のボタン: 「必要なアイテム」と「除外するアイテム」を切り替える (サーバー側)。 */
    public void toggleExclude(PipeBlockEntity pipe) {
        exclude = !exclude;
        pipe.setChanged();
    }

    @Override
    public void doWork(PipeBlockEntity pipe) {
        Level level = pipe.getLevel();
        Direction source = WoodSource.source(pipe);
        if (level == null || source == null || powerProvider.getEnergyStored() <= 0.0F
                || !(pipe.transport() instanceof ItemTransport items)) {
            return;
        }
        IItemHandler inventory = level.getCapability(Capabilities.ItemHandler.BLOCK,
                pipe.getBlockPos().relative(source), source.getOpposite());
        if (inventory == null) {
            return;
        }
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack available = inventory.extractItem(slot, inventory.getSlotLimit(slot), true);
            if (available.isEmpty() || !canExtract(available)) {
                continue;
            }
            int amount = (int) powerProvider.useEnergy(ENERGY_PER_ITEM, available.getCount(), true);
            ItemStack extracted = inventory.extractItem(slot, amount, false);
            if (!extracted.isEmpty()) {
                items.injectFromSide(extracted, source.getOpposite(), TravelingItem.NORMAL_SPEED);
            }
            return;
        }
        // 取り出せるアイテムが無くても 1 MJ を消費する (旧 doWork)。
        powerProvider.useEnergy(ENERGY_PER_ITEM, ENERGY_PER_ITEM, true);
    }

    /**
     * フィルターのどれかと一致すれば「必要モードなら取り出す / 除外モードなら取り出さない」。
     * どれとも一致しなければその逆 (旧 CanExtract)。フィルターの一致判定はダイヤパイプと同じ。
     */
    boolean canExtract(ItemStack stack) {
        for (int slot = 0; slot < filters.getContainerSize(); slot++) {
            ItemStack filter = filters.getItem(slot);
            if (!filter.isEmpty() && DiamondFilterBehavior.matches(filter, stack)) {
                return !exclude;
            }
        }
        return exclude;
    }

    @Override
    public boolean onWrench(PipeBlockEntity pipe) {
        WoodSource.switchSource(pipe);
        return true;
    }

    @Override
    public void initialize(PipeBlockEntity pipe) {
        WoodSource.switchSourceIfNeeded(pipe);
    }

    @Override
    public void onNeighborChanged(PipeBlockEntity pipe) {
        WoodSource.switchSourceIfNeeded(pipe);
    }

    @Override
    public MenuProvider menuProvider(PipeBlockEntity pipe) {
        return new SimpleMenuProvider((containerId, playerInventory, player) ->
                new AdvancedWoodMenu(containerId, playerInventory, pipe, this), Component.translatable(TITLE_KEY));
    }

    @Override
    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean(TAG_EXCLUDE, exclude);
        CompoundTag filterTag = new CompoundTag();
        ContainerHelper.saveAllItems(filterTag, filters.getItems(), registries);
        tag.put(TAG_FILTERS, filterTag);
    }

    @Override
    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        exclude = tag.getBoolean(TAG_EXCLUDE);
        filters.clearContent();
        ContainerHelper.loadAllItems(tag.getCompound(TAG_FILTERS), filters.getItems(), registries);
    }
}

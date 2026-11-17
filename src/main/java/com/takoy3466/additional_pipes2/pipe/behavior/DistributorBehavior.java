package com.takoy3466.additional_pipes2.pipe.behavior;

import com.takoy3466.additional_pipes2.menu.DistributorMenu;
import com.takoy3466.re_buildcraft.api.transport.IPipeEntry;
import com.takoy3466.re_buildcraft.transport.ItemTransport;
import com.takoy3466.re_buildcraft.transport.PipeBlockEntity;
import com.takoy3466.re_buildcraft.transport.PipeMaterial;
import com.takoy3466.re_buildcraft.transport.TravelingItem;
import com.takoy3466.re_buildcraft.transport.behavior.PipeBehavior;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

/**
 * 分配パイプ。アイテムを「現在の出力面」だけへ送り、面ごとに設定した個数を送るたびに次の面へ切り替える。
 * 元実装: {@code PipeItemsDistributor} + {@code PipeLogicDistributor}。
 * <ul>
 *   <li>出力面になれるのは、送出個数が 1 以上で、木のパイプ以外のパイプ・インベントリがつながっている面</li>
 *   <li>レンチで右クリックすると次の面へ切り替える</li>
 *   <li>それ以外で右クリックすると、面ごとの送出個数を設定する画面を開く</li>
 * </ul>
 * 旧版は出力面をメタデータに持っていたが、見た目 (面ごとに色分けされた腕) は出力面に依存しないため、
 * ここではブロックエンティティの保存データに持つ。
 */
public class DistributorBehavior implements PipeBehavior {
    private static final String TITLE_KEY = "container.additional_pipes2.distribution_pipe";
    private static final String TAG_OUTPUT = "output";
    private static final int NO_OUTPUT = -1;

    private final DistributionCounts counts = new DistributionCounts();
    @Nullable
    private Direction output;

    public DistributionCounts counts() {
        return counts;
    }

    @Nullable
    public Direction output() {
        return output;
    }

    @Override
    public void initialize(PipeBlockEntity pipe) {
        if (output == null) {
            // 旧 onBlockPlaced: 上 (メタデータ 1) から次の有効な面を探す。
            output = Direction.UP;
            switchPosition(pipe);
        }
    }

    @Override
    public boolean outputOpen(PipeBlockEntity pipe, Direction direction) {
        return direction == output;
    }

    @Override
    public void onItemEntered(PipeBlockEntity pipe, TravelingItem item) {
        // 行き先は出力面だけなので、選ばれる前に出力面が有効か確かめておく (旧 switchIfNeeded)。
        switchIfNeeded(pipe);
    }

    @Override
    public List<Direction> filterMovements(PipeBlockEntity pipe, List<Direction> movements, TravelingItem item) {
        if (output != null && movements.contains(output) && counts.recordSent(output.get3DDataValue())) {
            switchPosition(pipe);
        }
        return movements;
    }

    @Override
    public boolean onWrench(PipeBlockEntity pipe) {
        switchPosition(pipe);
        return true;
    }

    @Override
    public void onNeighborChanged(PipeBlockEntity pipe) {
        switchIfNeeded(pipe);
    }

    @Override
    public MenuProvider menuProvider(PipeBlockEntity pipe) {
        return new SimpleMenuProvider((containerId, playerInventory, player) ->
                new DistributorMenu(containerId, playerInventory, pipe, this), Component.translatable(TITLE_KEY));
    }

    /** GUI の +/- 操作 (サーバー側)。 */
    public void adjustAmount(PipeBlockEntity pipe, int side, int delta) {
        counts.adjust(side, delta);
        switchIfNeeded(pipe);
        pipe.setChanged();
    }

    /** 現在の面の次から順に、出力面になれる面を探して切り替える (旧 switchPosition)。見つからなければそのまま。 */
    void switchPosition(PipeBlockEntity pipe) {
        int start = output == null ? 0 : output.get3DDataValue() + 1;
        for (int step = 0; step < DistributionCounts.SIDES; step++) {
            Direction candidate = Direction.from3DDataValue((start + step) % DistributionCounts.SIDES);
            if (isValidOutput(pipe, candidate)) {
                setOutput(pipe, candidate);
                return;
            }
        }
    }

    /** 現在の面が出力面になれなければ切り替える (旧 switchIfNeeded)。 */
    void switchIfNeeded(PipeBlockEntity pipe) {
        if (output == null || !isValidOutput(pipe, output)) {
            switchPosition(pipe);
        }
    }

    private void setOutput(PipeBlockEntity pipe, Direction direction) {
        if (output != direction) {
            output = direction;
            pipe.setChanged();
        }
    }

    private boolean isValidOutput(PipeBlockEntity pipe, Direction direction) {
        Level level = pipe.getLevel();
        if (level == null || !counts.isEnabled(direction.get3DDataValue()) || !pipe.isConnected(direction)) {
            return false;
        }
        BlockPos neighborPos = pipe.getBlockPos().relative(direction);
        BlockEntity neighbor = level.getBlockEntity(neighborPos);
        if (neighbor instanceof PipeBlockEntity next) {
            return next.transport() instanceof ItemTransport && next.type().material() != PipeMaterial.WOOD;
        }
        return neighbor instanceof IPipeEntry
                || level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, direction.getOpposite()) != null;
    }

    @Override
    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        counts.save(tag);
        tag.putInt(TAG_OUTPUT, output == null ? NO_OUTPUT : output.get3DDataValue());
    }

    @Override
    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        counts.load(tag);
        int saved = tag.contains(TAG_OUTPUT) ? tag.getInt(TAG_OUTPUT) : NO_OUTPUT;
        output = saved >= 0 && saved < DistributionCounts.SIDES ? Direction.from3DDataValue(saved) : null;
    }
}

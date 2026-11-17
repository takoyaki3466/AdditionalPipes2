package com.takoy3466.additional_pipes2.pipe.behavior;

import com.takoy3466.re_buildcraft.transport.PipeBlockEntity;
import com.takoy3466.re_buildcraft.transport.TravelingItem;
import com.takoy3466.re_buildcraft.transport.behavior.PipeBehavior;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * 高度な挿入パイプ。隣に空きのあるインベントリがあれば、パイプより優先してそこへアイテムを送る。
 * 元実装: {@code PipeItemsAdvancedInsertion.filterPossibleMovements}。
 * <p>
 * 旧版は中心でインベントリへ直接挿入し、入りきらなかった分を再び行き先選びに回していた。
 * ここでは行き先を「全量が入るインベントリ」→「一部でも入るインベントリ」→「通常の行き先」の順に絞り込み、
 * 実際の挿入は Re-BuildCraft のアイテム輸送 (出口でのインベントリ挿入) に任せる。
 * 全量が入るインベントリを優先することで、出口での入りきらない分の発生を旧版と同程度に抑える。
 */
public class AdvancedInsertionBehavior implements PipeBehavior {

    @Override
    public void onItemEntered(PipeBlockEntity pipe, TravelingItem item) {
        ItemSpeeds.applyStoneFriction(item);
    }

    @Override
    public List<Direction> filterMovements(PipeBlockEntity pipe, List<Direction> movements, TravelingItem item) {
        Level level = pipe.getLevel();
        if (level == null) {
            return movements;
        }
        List<Direction> fullFit = new ArrayList<>();
        List<Direction> partialFit = new ArrayList<>();
        for (Direction direction : movements) {
            int accepted = insertableCount(level, pipe.getBlockPos(), direction, item.stack());
            if (accepted >= item.stack().getCount()) {
                fullFit.add(direction);
            } else if (accepted > 0) {
                partialFit.add(direction);
            }
        }
        if (!fullFit.isEmpty()) {
            return fullFit;
        }
        return partialFit.isEmpty() ? movements : partialFit;
    }

    /** {@code direction} の隣がパイプ以外のインベントリなら、{@code stack} のうち入る個数。そうでなければ 0。 */
    private static int insertableCount(Level level, BlockPos pos, Direction direction, ItemStack stack) {
        BlockPos neighborPos = pos.relative(direction);
        if (level.getBlockEntity(neighborPos) instanceof PipeBlockEntity) {
            return 0;
        }
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, direction.getOpposite());
        if (handler == null) {
            return 0;
        }
        ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, stack.copy(), true);
        return stack.getCount() - remainder.getCount();
    }
}

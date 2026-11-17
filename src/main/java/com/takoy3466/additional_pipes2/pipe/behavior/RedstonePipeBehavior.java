package com.takoy3466.additional_pipes2.pipe.behavior;

import com.takoy3466.additional_pipes2.pipe.RedstonePipeBlock;
import com.takoy3466.re_buildcraft.transport.FluidTransport;
import com.takoy3466.re_buildcraft.transport.PipeBlockEntity;
import com.takoy3466.re_buildcraft.transport.TravelingItem;
import com.takoy3466.re_buildcraft.transport.behavior.PipeBehavior;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * レッドストーン輸送パイプ / 防水レッドストーンパイプ。条件を満たす間、全方向へ強いレッドストーン信号 (15) を出す。
 * 元実装: {@code PipeItemsRedstone} / {@code PipeLiquidsRedstone} の isPoweringTo / isIndirectlyPoweringTo。
 * <ul>
 *   <li>アイテム: パイプ内をアイテムが通過中</li>
 *   <li>液体: 中心のバッファが満杯 ({@link FluidTransport#LIQUID_IN_PIPE} = 250 mB 以上, 旧 getCenter() &gt;= 250)</li>
 * </ul>
 * 信号の状態はブロックステート {@link RedstonePipeBlock#POWERED} に持ち、変化したときだけ周囲を更新する。
 */
public final class RedstonePipeBehavior implements PipeBehavior {
    private static final int FULL_SIGNAL = 15;

    private final Predicate<PipeBlockEntity> activeCondition;
    private final boolean slowsItems;

    private RedstonePipeBehavior(Predicate<PipeBlockEntity> activeCondition, boolean slowsItems) {
        this.activeCondition = activeCondition;
        this.slowsItems = slowsItems;
    }

    /** レッドストーン輸送パイプ: アイテムが入っている間だけ信号を出す。 */
    public static RedstonePipeBehavior forItems() {
        return new RedstonePipeBehavior(pipe -> !pipe.transport().isEmpty(), true);
    }

    /** 防水レッドストーンパイプ: 中心の液体が満杯の間だけ信号を出す。 */
    public static RedstonePipeBehavior forFluids() {
        return new RedstonePipeBehavior(pipe -> pipe.transport() instanceof FluidTransport fluids
                && fluids.centerAmount() >= FluidTransport.LIQUID_IN_PIPE, false);
    }

    @Override
    public void onItemEntered(PipeBlockEntity pipe, TravelingItem item) {
        if (slowsItems) {
            ItemSpeeds.applyStoneFriction(item);
        }
    }

    @Override
    public void tick(PipeBlockEntity pipe) {
        Level level = pipe.getLevel();
        BlockState state = pipe.getBlockState();
        if (level == null || !state.hasProperty(RedstonePipeBlock.POWERED)) {
            return;
        }
        boolean active = activeCondition.test(pipe);
        if (state.getValue(RedstonePipeBlock.POWERED) != active) {
            BlockPos pos = pipe.getBlockPos();
            level.setBlock(pos, state.setValue(RedstonePipeBlock.POWERED, active), Block.UPDATE_ALL);
            notifyStrongPowerNeighbors(level, pos, state.getBlock());
        }
    }

    /** 強い信号はブロック越しに伝わるため、隣のさらに隣まで更新を伝える (レバーと同じ扱い)。 */
    private static void notifyStrongPowerNeighbors(Level level, BlockPos pos, Block block) {
        level.updateNeighborsAt(pos, block);
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(pos.relative(direction), block);
        }
    }

    @Override
    public int redstoneSignal(PipeBlockEntity pipe, Direction direction) {
        return isPowered(pipe) ? FULL_SIGNAL : 0;
    }

    @Override
    public int directRedstoneSignal(PipeBlockEntity pipe, Direction direction) {
        return redstoneSignal(pipe, direction);
    }

    private static boolean isPowered(PipeBlockEntity pipe) {
        BlockState state = pipe.getBlockState();
        return state.hasProperty(RedstonePipeBlock.POWERED) && state.getValue(RedstonePipeBlock.POWERED);
    }
}

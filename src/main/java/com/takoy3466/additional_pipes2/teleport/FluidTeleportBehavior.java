package com.takoy3466.additional_pipes2.teleport;

import com.takoy3466.re_buildcraft.transport.FluidTransport;
import com.takoy3466.re_buildcraft.transport.PipeBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * 液体テレポートパイプ (防水テレポートパイプ)。入ってくる液体を自分のバッファには入れず、
 * リンクしている受信パイプのどれか 1 本 (無作為) の隣にある液体パイプ (テレポートパイプ以外) へ直接入れる。
 * 元実装: {@code PipeLiquidsTeleport.fill}。
 * <p>
 * 旧版と同じく、受信パイプが無い・隣のパイプが受け取らない場合は液体を受け取らない (0 を返す)。
 * 液体は Re-BuildCraft の液体ハンドラ ({@link IFluidHandler}) を通して受け渡す。
 */
public class FluidTeleportBehavior extends AbstractTeleportBehavior {

    @Override
    public TeleportKind kind() {
        return TeleportKind.FLUIDS;
    }

    @Override
    public OptionalInt interceptFluidFill(PipeBlockEntity pipe, Direction side, FluidStack resource, boolean execute) {
        Level level = pipe.getLevel();
        if (level == null || level.isClientSide) {
            return OptionalInt.of(0);
        }
        List<Link> receivers = linkedPipes(pipe, false);
        if (receivers.isEmpty()) {
            return OptionalInt.of(0);
        }
        RandomSource random = level.random;
        PipeBlockEntity receiver = receivers.get(random.nextInt(receivers.size())).pipe();
        List<IFluidHandler> outlets = outlets(level, receiver);
        IFluidHandler.FluidAction action = execute ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE;
        int used = 0;
        while (!outlets.isEmpty() && used <= 0) {
            used = outlets.remove(random.nextInt(outlets.size())).fill(resource.copy(), action);
        }
        return OptionalInt.of(Math.max(used, 0));
    }

    /** 受信パイプの隣の液体パイプ (テレポートパイプ以外) の、受信パイプ側の面の液体ハンドラ (旧 getPossibleLiquidMovements)。 */
    private static List<IFluidHandler> outlets(Level level, PipeBlockEntity receiver) {
        List<IFluidHandler> outlets = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(receiver.getBlockPos().relative(direction));
            if (!isTeleportPipe(neighbor) && neighbor instanceof PipeBlockEntity next && next.transport() instanceof FluidTransport fluids) {
                outlets.add(fluids.handler(direction.getOpposite()));
            }
        }
        return outlets;
    }
}

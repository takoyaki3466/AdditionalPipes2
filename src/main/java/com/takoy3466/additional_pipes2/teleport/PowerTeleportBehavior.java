package com.takoy3466.additional_pipes2.teleport;

import com.takoy3466.re_buildcraft.transport.PipeBlockEntity;
import com.takoy3466.re_buildcraft.transport.PowerTransport;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * 動力テレポートパイプ。元実装: {@code PipePowerTeleport}。
 * <ul>
 *   <li>MJ が入ってくると、リンクしている受信パイプのうち「隣の動力パイプが MJ を要求している」ものへ均等に分け、
 *       さらに各受信パイプの中で要求している隣のパイプへ均等に分けて渡す (旧 receiveEnergy)。距離による損失は無い。</li>
 *   <li>受信が有効なパイプに MJ の要求が届くと、リンクしている全てのパイプ (受信設定を問わない) の隣の動力パイプへ
 *       同じ量の要求を伝える。これにより送り側の動力源が送り側のテレポートパイプへ MJ を流す (旧 requestEnergy)。</li>
 * </ul>
 * MJ の計算は Re-BuildCraft の動力パイプ ({@link PowerTransport}) と同じ単位・同じ受け渡しメソッドを使う。
 * <p>
 * 旧版との違い: 旧版は各受信パイプの分け前を隣のパイプ 1 本ごとに「分け前の全量」を渡しており
 * (分割後の値 powerToSend2 を使っていない)、隣のパイプが複数あると MJ が増えていた。ここでは正しく分割する。
 * また、要求しているパイプが無いときは MJ を横取りせず、通常の動力パイプとして扱う。
 */
public class PowerTeleportBehavior extends AbstractTeleportBehavior {

    @Override
    public TeleportKind kind() {
        return TeleportKind.POWER;
    }

    @Override
    public boolean interceptPower(PipeBlockEntity pipe, Direction from, double amount) {
        Level level = pipe.getLevel();
        if (level == null || level.isClientSide || amount <= 0.0) {
            return false;
        }
        List<List<Outlet>> receiversInNeed = new ArrayList<>();
        for (Link link : linkedPipes(pipe, false)) {
            List<Outlet> needing = outlets(level, link.pipe(), true);
            if (!needing.isEmpty()) {
                receiversInNeed.add(needing);
            }
        }
        if (receiversInNeed.isEmpty()) {
            return false;
        }
        double perReceiver = amount / receiversInNeed.size();
        for (List<Outlet> needing : receiversInNeed) {
            double perOutlet = perReceiver / needing.size();
            for (Outlet outlet : needing) {
                outlet.transport().receiveEnergy(level, outlet.sideOfNeighbor(), perOutlet);
            }
        }
        return true;
    }

    @Override
    public void onPowerRequested(PipeBlockEntity pipe, Direction from, int amount) {
        Level level = pipe.getLevel();
        if (level == null || level.isClientSide || amount <= 0 || !settings().canReceive()) {
            return;
        }
        for (Link link : linkedPipes(pipe, true)) {
            for (Outlet outlet : outlets(level, link.pipe(), false)) {
                outlet.transport().requestEnergy(level, outlet.sideOfNeighbor(), amount);
            }
        }
    }

    /**
     * {@code teleportPipe} の隣にある、つながった動力パイプ (テレポートパイプ以外)。
     *
     * @param onlyRequesting true なら MJ を要求しているものだけ (旧 TeleportNeedsPower)
     */
    private static List<Outlet> outlets(Level level, PipeBlockEntity teleportPipe, boolean onlyRequesting) {
        List<Outlet> outlets = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (!teleportPipe.isConnected(direction) || !teleportPipe.outputOpen(direction)) {
                continue;
            }
            PowerTransport transport = neighborPowerPipe(level, teleportPipe, direction);
            if (transport != null && (!onlyRequesting || transport.isRequestingPower())) {
                outlets.add(new Outlet(transport, direction.getOpposite()));
            }
        }
        return outlets;
    }

    @Nullable
    private static PowerTransport neighborPowerPipe(Level level, PipeBlockEntity pipe, Direction direction) {
        BlockEntity neighbor = level.getBlockEntity(pipe.getBlockPos().relative(direction));
        if (isTeleportPipe(neighbor)) {
            return null;
        }
        return neighbor instanceof PipeBlockEntity next && next.transport() instanceof PowerTransport power ? power : null;
    }

    /** 隣の動力パイプと、その隣から見たテレポートパイプ側の面。 */
    private record Outlet(PowerTransport transport, Direction sideOfNeighbor) {
    }
}

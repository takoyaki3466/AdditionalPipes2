package com.takoy3466.additional_pipes2.teleport;

import com.takoy3466.additional_pipes2.pipe.behavior.ItemSpeeds;
import com.takoy3466.re_buildcraft.api.transport.IPipeEntry;
import com.takoy3466.re_buildcraft.transport.ItemTransport;
import com.takoy3466.re_buildcraft.transport.PipeBlockEntity;
import com.takoy3466.re_buildcraft.transport.TravelingItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * アイテムテレポートパイプ。中心に着いたアイテムを、リンクしている受信パイプのどれか (無作為) の
 * 隣にあるパイプ (テレポートパイプ以外) へ直接送り込む。元実装: {@code PipeItemTeleport}。
 * <p>
 * 送り先が無い場合、旧版は来た方向へ跳ね返すか (リンク先なし)、落としていた (リンク先が全て受け取れない)。
 * ここではどちらの場合も通常のパイプとして扱い、Re-BuildCraft のアイテム輸送に任せる
 * (つながっている他のパイプへ流れるか、跳ね返る。アイテムを落とす場合も Re-BuildCraft の規則に従う)。
 */
public class ItemTeleportBehavior extends AbstractTeleportBehavior {

    @Override
    public TeleportKind kind() {
        return TeleportKind.ITEMS;
    }

    @Override
    public void onItemEntered(PipeBlockEntity pipe, TravelingItem item) {
        ItemSpeeds.applyDefaultFriction(item);
    }

    @Override
    public boolean consumeAtCenter(PipeBlockEntity pipe, TravelingItem item) {
        Level level = pipe.getLevel();
        if (level == null || level.isClientSide) {
            return false;
        }
        List<Link> receivers = new ArrayList<>(linkedPipes(pipe, false));
        RandomSource random = level.random;
        while (!receivers.isEmpty()) {
            Link receiver = receivers.remove(random.nextInt(receivers.size()));
            if (insertIntoRandomOutlet(level, receiver.pipe(), item, random)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 受信パイプ {@code receiver} の隣の、アイテムを受け取れるパイプ (テレポートパイプ以外) から無作為に 1 つ選んで渡す
     * (旧 addToRandomPipeEntry)。
     */
    private static boolean insertIntoRandomOutlet(Level level, PipeBlockEntity receiver, TravelingItem item, RandomSource random) {
        List<Direction> outlets = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(receiver.getBlockPos().relative(direction));
            if (!isTeleportPipe(neighbor) && neighbor instanceof IPipeEntry entry && entry.acceptsItems()) {
                outlets.add(direction);
            }
        }
        if (outlets.isEmpty()) {
            return false;
        }
        Direction heading = outlets.get(random.nextInt(outlets.size()));
        BlockEntity neighbor = level.getBlockEntity(receiver.getBlockPos().relative(heading));
        if (neighbor instanceof PipeBlockEntity next && next.transport() instanceof ItemTransport items) {
            // パイプへはアイテムの速度を保ったまま入れる (旧版は同じアイテムエンティティを移していた)。
            items.injectFromSide(item.stack(), heading, item.speed());
        } else if (neighbor instanceof IPipeEntry entry) {
            entry.injectItem(item.stack(), heading);
        }
        return true;
    }
}

package com.takoy3466.additional_pipes2.pipe;

import com.takoy3466.additional_pipes2.pipe.behavior.AdvancedInsertionBehavior;
import com.takoy3466.additional_pipes2.pipe.behavior.AdvancedWoodBehavior;
import com.takoy3466.additional_pipes2.pipe.behavior.DistributorBehavior;
import com.takoy3466.additional_pipes2.pipe.behavior.RedstonePipeBehavior;
import com.takoy3466.additional_pipes2.teleport.FluidTeleportBehavior;
import com.takoy3466.additional_pipes2.teleport.ItemTeleportBehavior;
import com.takoy3466.additional_pipes2.teleport.PowerTeleportBehavior;
import com.takoy3466.re_buildcraft.transport.PipeKind;
import com.takoy3466.re_buildcraft.transport.PipeMaterial;
import com.takoy3466.re_buildcraft.transport.PipeSpec;
import com.takoy3466.re_buildcraft.transport.behavior.PipeBehavior;
import java.util.function.Supplier;

/**
 * Additional Pipes のパイプ一覧 (旧 {@code mod_AdditionalPipes.createPipe} の呼び出し)。
 * <p>
 * 素材 ({@link PipeMaterial}) は旧版で各パイプが使っていた {@code PipeLogic*} の接続制限に合わせている。
 * <ul>
 *   <li>高度な挿入パイプ・レッドストーン輸送パイプ: PipeLogicStone → {@link PipeMaterial#STONE} (丸石とつながらない)</li>
 *   <li>防水レッドストーンパイプ: PipeLogicGold → {@link PipeMaterial#GOLD} (液体では制限なし)</li>
 *   <li>高度な木製パイプ: 木・高度な木製パイプとつながらない → {@link PipeMaterial#WOOD}</li>
 *   <li>分配パイプ: 制限なし → {@link PipeMaterial#GENERIC}</li>
 *   <li>テレポートパイプ: パイプとだけつながり、テレポートパイプ同士はつながらない (PipeLogicTeleport)</li>
 * </ul>
 */
public enum APPipeType implements PipeSpec {
    ITEM_TELEPORT("item_teleport_pipe", PipeKind.ITEMS, PipeMaterial.GENERIC, ItemTeleportBehavior::new, Flags.TELEPORT),
    FLUID_TELEPORT("waterproof_teleport_pipe", PipeKind.FLUIDS, PipeMaterial.GENERIC, FluidTeleportBehavior::new, Flags.TELEPORT),
    POWER_TELEPORT("power_teleport_pipe", PipeKind.POWER, PipeMaterial.GENERIC, PowerTeleportBehavior::new, Flags.TELEPORT),
    DISTRIBUTOR("distribution_transport_pipe", PipeKind.ITEMS, PipeMaterial.GENERIC, DistributorBehavior::new, Flags.NONE),
    ADVANCED_WOOD("advanced_wooden_transport_pipe", PipeKind.ITEMS, PipeMaterial.WOOD, AdvancedWoodBehavior::new, Flags.DIRECTIONAL),
    ADVANCED_INSERTION("advanced_insertion_pipe", PipeKind.ITEMS, PipeMaterial.STONE, AdvancedInsertionBehavior::new, Flags.NONE),
    REDSTONE("redstone_transport_pipe", PipeKind.ITEMS, PipeMaterial.STONE, RedstonePipeBehavior::forItems, Flags.REDSTONE),
    FLUID_REDSTONE("waterproof_redstone_pipe", PipeKind.FLUIDS, PipeMaterial.GOLD, RedstonePipeBehavior::forFluids, Flags.REDSTONE);

    /**
     * 液体テレポートパイプと防水レッドストーンパイプの流量 (旧 {@code flowRate = 80})。
     * 旧版の {@code travelDelay = 2} は Re-BuildCraft の液体輸送に遅延の概念が無いため反映していない。
     */
    private static final int FAST_FLUID_FLOW_RATE = 80;
    /** Re-BuildCraft の液体パイプの既定流量 (液体以外のパイプでは使われない)。 */
    private static final int DEFAULT_FLUID_FLOW_RATE = 50;

    private final String id;
    private final PipeKind kind;
    private final PipeMaterial material;
    private final Supplier<? extends PipeBehavior> behaviorFactory;
    private final Flags flags;

    APPipeType(String id, PipeKind kind, PipeMaterial material, Supplier<? extends PipeBehavior> behaviorFactory, Flags flags) {
        this.id = id;
        this.kind = kind;
        this.material = material;
        this.behaviorFactory = behaviorFactory;
        this.flags = flags;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public PipeKind kind() {
        return kind;
    }

    @Override
    public PipeMaterial material() {
        return material;
    }

    @Override
    public boolean directional() {
        return flags == Flags.DIRECTIONAL;
    }

    @Override
    public PipeBehavior createBehavior() {
        return behaviorFactory.get();
    }

    @Override
    public int fluidFlowRate() {
        return kind == PipeKind.FLUIDS ? FAST_FLUID_FLOW_RATE : DEFAULT_FLUID_FLOW_RATE;
    }

    /** テレポートパイプ同士はつながらない (旧 PipeLogicTeleport.isPipeConnected)。 */
    @Override
    public boolean acceptsPipe(PipeSpec other) {
        if (isTeleport() && other instanceof APPipeType otherType && otherType.isTeleport()) {
            return false;
        }
        return PipeSpec.super.acceptsPipe(other);
    }

    /** テレポートパイプはパイプ以外とつながらない (旧 PipeLogicTeleport は TileGenericPipe 以外を拒否)。 */
    @Override
    public boolean connectsToNonPipeBlocks() {
        return !isTeleport();
    }

    public boolean isTeleport() {
        return flags == Flags.TELEPORT;
    }

    /** 中身があるときレッドストーン信号を出し、見た目が変わるパイプか。 */
    public boolean emitsRedstone() {
        return flags == Flags.REDSTONE;
    }

    /** ブロックの作り分けに関わる性質。 */
    private enum Flags {
        NONE,
        DIRECTIONAL,
        TELEPORT,
        REDSTONE
    }
}

package com.takoy3466.additional_pipes2.pipe;

import com.takoy3466.re_buildcraft.transport.PipeBlock;
import com.takoy3466.re_buildcraft.transport.PipeSpec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * レッドストーン輸送パイプ・防水レッドストーンパイプのブロック。
 * 信号を出しているかを {@link #POWERED} として持ち、モデルのテクスチャを切り替える
 * (旧版は {@code isPowering} に応じて RS.png / RSP.png などを差し替えていた)。
 */
public class RedstonePipeBlock extends PipeBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public RedstonePipeBlock(Properties properties, PipeSpec pipeType) {
        super(properties, pipeType);
        registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }
}

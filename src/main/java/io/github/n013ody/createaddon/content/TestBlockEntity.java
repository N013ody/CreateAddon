package io.github.n013ody.createaddon.content;

import io.github.n013ody.createaddon.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TestBlockEntity extends KineticBlockEntity {
    public TestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TESTBLOCK.get(), pos, state);
    }
}


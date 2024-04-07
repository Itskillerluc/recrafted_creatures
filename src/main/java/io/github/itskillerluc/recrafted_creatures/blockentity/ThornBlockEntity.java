package io.github.itskillerluc.recrafted_creatures.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ThornBlockEntity extends BlockEntity {
    private int timer;
    private int time = 0;

    public ThornBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, int timer) {
        super(pType, pPos, pBlockState);
        this.timer = timer;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("timer", timer);
        pTag.putInt("time", time);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        timer = pTag.getInt("timer");
        time = pTag.getInt("time");
    }

    public void tick() {
        time++;
        if (time > timer) {
            if (getLevel() == null) return;
            getLevel().setBlock(getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
        }
    }
}

package io.github.itskillerluc.recrafted_creatures.blockentity;

import io.github.itskillerluc.recrafted_creatures.block.EggBlock;
import io.github.itskillerluc.recrafted_creatures.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public class EggBlockEntity <T extends AgeableMob> extends BlockEntity {
    private final int maxHatchTime;
    private final int stages;
    private int hatchTimer;
    private final Function<Level, T> entity;

    public EggBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pPos, BlockState pBlockState, int maxHatchTime, Function<Level, T> entity, int stages) {
        super(blockEntityType, pPos, pBlockState);
        this.maxHatchTime = maxHatchTime;
        this.entity = entity;
        this.stages = stages;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("hatchTime" ,hatchTimer);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        hatchTimer = pTag.getInt("hatchTime");
    }

    public void tick(){
        hatchTimer++;
        if (hatchTimer % (maxHatchTime / stages) == 0) {
            var stage = getBlockState().getValue(EggBlock.HATCH);
            level.setBlock(getBlockPos(), getBlockState().setValue(EggBlock.HATCH, stage + 1), 2);
            level.playSound(null, getBlockPos(), SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.7F, 0.9F + level.random.nextFloat() * 0.2F);
        }
        if (hatchTimer > maxHatchTime){
            for (int i = 0; i < this.getBlockState().getValue(BlockRegistry.OWL_EGG_BLOCK.get().getEggs()); i++) {
                var entityInstance = entity.apply(this.getLevel());
                entityInstance.setPos(new Vec3(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()));
                entityInstance.setAge(-24000);
                level.addFreshEntity(entityInstance);
                this.getLevel().destroyBlock(this.getBlockPos(), false);
            }
            level.playSound(null, getBlockPos(), SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.7F, 0.9F + level.random.nextFloat() * 0.2F);
        }
    }
}

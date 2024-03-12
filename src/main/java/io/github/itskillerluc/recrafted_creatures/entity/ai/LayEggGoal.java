package io.github.itskillerluc.recrafted_creatures.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.BiPredicate;

public class LayEggGoal <T extends PathfinderMob & EggLaying> extends MoveToBlockGoal {
    private final T entity;
    private final BlockState eggs;
    private final BiPredicate<LevelReader, BlockPos> isValidPos;

    public LayEggGoal(T entity, double pSpeedModifier, BlockState eggs, BiPredicate<LevelReader, BlockPos> isValidPos) {
        super(entity, pSpeedModifier, 16);
        this.entity = entity;
        this.eggs = eggs;
        this.isValidPos = isValidPos;
    }
    public boolean canUse() {
        return this.entity.hasEgg() && super.canUse();
    }
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.entity.hasEgg();
    }
    public void tick() {
        super.tick();
        BlockPos blockpos = entity.blockPosition();
        if (this.isReachedTarget()) {
            if (entity.getEggLayCounter() < 1) {
                entity.setEggLaying(true);
            } else if (entity.getEggLayCounter() > this.adjustedTickDelay(200)) {
                Level level = entity.level();
                level.playSound(null, blockpos, SoundEvents.TURTLE_LAY_EGG, SoundSource.BLOCKS, 0.3F, 0.9F + level.random.nextFloat() * 0.2F);
                BlockPos blockpos1 = this.blockPos.above();
                level.setBlock(blockpos1, eggs, 3);
                level.gameEvent(GameEvent.BLOCK_PLACE, blockpos1, GameEvent.Context.of(entity, eggs));
                entity.setHasEgg(false);
                entity.setEggLaying(false);
                entity.setInLove(600);
            }

            if (entity.getEggLaying()) {
                entity.setEggLayCounter(entity.getEggLayCounter() + 1);
            }
        }

    }
    protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
        return pLevel.isEmptyBlock(pPos.above()) && isValidPos.test(pLevel, pPos);
    }
}

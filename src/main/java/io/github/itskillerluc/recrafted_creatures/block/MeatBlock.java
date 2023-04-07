package io.github.itskillerluc.recrafted_creatures.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class MeatBlock extends Block {
    static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 3);
    private final int foodValue;
    private final int saturationValue;

    public MeatBlock(Properties pProperties, int foodValue, int saturationValue) {
        super(pProperties);
        this.foodValue = foodValue;
        this.saturationValue = saturationValue;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(BITES);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return Block.box(0, 0, 0, 16, 16 - 4 * pState.getValue(BITES), 16);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        if (pLevel.isClientSide() | !pPlayer.getFoodData().needsFood() | pHand != InteractionHand.MAIN_HAND){
            return InteractionResult.FAIL;
        }
        if (pState.getValue(BITES) == 3){
            pLevel.destroyBlock(pPos, false);
            pPlayer.getFoodData().eat(foodValue, saturationValue);
            return InteractionResult.SUCCESS;
        }
        pPlayer.getFoodData().eat(foodValue, saturationValue);
        pLevel.setBlock(pPos, pState.setValue(BITES, pState.getValue(BITES) + 1), 0b11);
        return InteractionResult.SUCCESS;
    }
}

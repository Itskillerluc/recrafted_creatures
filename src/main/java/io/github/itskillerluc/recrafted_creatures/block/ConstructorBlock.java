package io.github.itskillerluc.recrafted_creatures.block;

import io.github.itskillerluc.recrafted_creatures.blockentity.ConstructorBlockEntity;
import io.github.itskillerluc.recrafted_creatures.entity.Beaver;
import io.github.itskillerluc.recrafted_creatures.registries.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ConstructorBlock extends BaseEntityBlock {
    public ConstructorBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ConstructorBlockEntity(BlockEntityRegistry.CONSTRUCTOR.get(), pPos, pState);
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof ConstructorBlockEntity constructorBlockEntity) {
            return constructorBlockEntity.usedBy() ? InteractionResult.sidedSuccess(pLevel.isClientSide) : InteractionResult.FAIL;
        } else {
            return InteractionResult.FAIL;
        }
    }

    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    /**
     * Called by BlockItem after this block has been placed.
     */
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @javax.annotation.Nullable LivingEntity pPlacer, ItemStack pStack) {
        if (!pLevel.isClientSide) {
            if (pPlacer != null) {
                BlockEntity blockentity = pLevel.getBlockEntity(pPos);
                if (blockentity instanceof ConstructorBlockEntity constructorBlockEntity) {
                    constructorBlockEntity.createdBy(pPlacer);
                    constructorBlockEntity.detectSize();
                    constructorBlockEntity.setShowBoundingBox(true);

                    if (pLevel.getBlockEntity(constructorBlockEntity.getCorner()) instanceof ConstructorBlockEntity be) {
                        be.setCorner(pPos);
                        be.setStructureSize(constructorBlockEntity.getStructureSize());
                        be.setStructurePos(constructorBlockEntity.getStructurePos());
                    }
                }
            }

            for (Beaver entitiesOfClass : pLevel.getEntitiesOfClass(Beaver.class, AABB.ofSize(pPos.getCenter(), 10, 10, 10))) {
                entitiesOfClass.possibleBuildPos = pPos;
            }
        }
    }
}

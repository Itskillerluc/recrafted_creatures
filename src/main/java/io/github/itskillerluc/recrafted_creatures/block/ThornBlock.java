package io.github.itskillerluc.recrafted_creatures.block;

import io.github.itskillerluc.recrafted_creatures.blockentity.ThornBlockEntity;
import io.github.itskillerluc.recrafted_creatures.registries.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class ThornBlock extends Block implements EntityBlock {
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public ThornBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(TOP);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ThornBlockEntity(BlockEntityRegistry.THORN_BLOCK.get(), pPos, pState, 200);
    }

    @Override
    public float getDestroyProgress(BlockState pState, Player pPlayer, BlockGetter pLevel, BlockPos pPos) {
        return pPlayer.getMainHandItem().is(Items.SHEARS) ? super.getDestroyProgress(pState, pPlayer, pLevel, pPos) : -1;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide() ? null : (pLevel1, pPos, pState1, pBlockEntity) -> ((ThornBlockEntity) pBlockEntity).tick();
    }
}

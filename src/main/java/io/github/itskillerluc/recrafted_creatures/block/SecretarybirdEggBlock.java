package io.github.itskillerluc.recrafted_creatures.block;

import io.github.itskillerluc.recrafted_creatures.blockentity.EggBlockEntity;
import io.github.itskillerluc.recrafted_creatures.entity.Chameleon;
import io.github.itskillerluc.recrafted_creatures.entity.Secretarybird;
import io.github.itskillerluc.recrafted_creatures.registries.BlockEntityRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.BlockRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SecretarybirdEggBlock extends EggBlock {
    private static final VoxelShape ONE_EGG_AABB = Block.box(6, 0, 6, 10, 7, 10);
    public SecretarybirdEggBlock(Properties pProperties) {
        super(pProperties, 2, ONE_EGG_AABB);
    }

    @Override
    protected Block getEggBlock() {
        return BlockRegistry.SECRETARYBIRD_EGG_BLOCk.get();
    }

    @Override
    public boolean onValidBlock(BlockGetter pLevel, BlockPos pPos) {
        return pLevel.getBlockState(pPos.below()).is(BlockTags.LEAVES);
    }

    @Override
    protected boolean canDestroyEgg(Level pLevel, Entity pEntity) {
        if (!(pEntity instanceof Secretarybird) && !(pEntity instanceof Bat)) {
            if (!(pEntity instanceof LivingEntity)) {
                return false;
            } else {
                return pEntity instanceof Player || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(pLevel, pEntity);
            }
        } else {
            return false;
        }
    }

    @Override
    public IntegerProperty getEggs() {
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new EggBlockEntity<>(BlockEntityRegistry.SECRETARYBIRD_EGG.get(), pPos, pState, 4500, level -> EntityRegistry.CHAMELEON.get().create(level), 2, null);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide() ? null : (pLevel1, pPos, pState1, pBlockEntity) -> ((EggBlockEntity<?>) pBlockEntity).tick();
    }

    @javax.annotation.Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState();
    }

    public @NotNull VoxelShape getShape(BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return ONE_EGG_AABB;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HATCH);
    }
}

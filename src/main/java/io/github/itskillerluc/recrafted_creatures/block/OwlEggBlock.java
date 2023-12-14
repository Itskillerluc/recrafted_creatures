package io.github.itskillerluc.recrafted_creatures.block;

import io.github.itskillerluc.recrafted_creatures.blockentity.EggBlockEntity;
import io.github.itskillerluc.recrafted_creatures.entity.Chameleon;
import io.github.itskillerluc.recrafted_creatures.entity.Owl;
import io.github.itskillerluc.recrafted_creatures.registries.BlockEntityRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.BlockRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class OwlEggBlock extends EggBlock {
    public static final IntegerProperty EGGS = IntegerProperty.create("owl_eggs", 1, 3);
    private static final VoxelShape ONE_EGG_AABB = Block.box(3.0D, 0.0D, 3.0D, 12.0D, 7.0D, 12.0D);
    public static final int MIN_EGGS = 1;
    public static final int MAX_EGGS = 3;

    public OwlEggBlock(Properties pProperties) {
        super(pProperties, 2, MIN_EGGS, MAX_EGGS, ONE_EGG_AABB, ONE_EGG_AABB);
    }

    @Override
    protected Block getEggBlock() {
        return BlockRegistry.OWL_EGG_BLOCK.get();
    }

    @Override
    public boolean onValidBlock(BlockGetter pLevel, BlockPos pPos) {
        return pLevel.getBlockState(pPos.below()).is(BlockTags.LEAVES);
    }

    @Override
    protected boolean canDestroyEgg(Level pLevel, Entity pEntity) {
        if (!(pEntity instanceof Owl) && !(pEntity instanceof Bat)) {
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
        return EGGS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new EggBlockEntity<>(BlockEntityRegistry.OWL_EGG.get(), pPos, pState, 4500, level -> EntityRegistry.OWL.get().create(level), 2, EGGS);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide() ? null : (pLevel1, pPos, pState1, pBlockEntity) -> ((EggBlockEntity<?>) pBlockEntity).tick();
    }
}

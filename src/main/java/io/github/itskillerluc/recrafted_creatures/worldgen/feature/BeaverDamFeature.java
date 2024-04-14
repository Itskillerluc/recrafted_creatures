package io.github.itskillerluc.recrafted_creatures.worldgen.feature;

import com.mojang.serialization.Codec;
import io.github.itskillerluc.recrafted_creatures.registries.BlockRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.AABB;

public class BeaverDamFeature extends Feature<NoneFeatureConfiguration> {
    public BeaverDamFeature(Codec<NoneFeatureConfiguration> pCodec) {
        super(pCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> pContext) {
        BlockPos surfacePos = getSurfacePos(pContext.level(), pContext.origin());
        int xWidth = getWidth(pContext.level(), surfacePos, true);
        int zWidth = getWidth(pContext.level(), surfacePos, false);
        Direction.Axis axis = xWidth < zWidth ? Direction.Axis.X : Direction.Axis.Z;
        int width = axis == Direction.Axis.X ? xWidth : zWidth;

        if (width > 32) return false;
        BlockPos start = surfacePos.relative(axis, -(width / 2));
        BlockPos end = surfacePos.relative(axis, width / 2);


        float maxDepth = getMaxDepth(pContext.level(), start, end) - 1;

        float baseWidth = 0.4f * width + 2;
        for (int i = 0; i < width + 15; i++) {
            for (float j = -baseWidth; j < baseWidth; j++) {
                int y = (int) (- (maxDepth / (baseWidth * baseWidth)) * (j * j));
                int offset = 0;
                BlockPos pos = axis == Direction.Axis.X ? new BlockPos(i + start.getX(), y + start.getY() + 1, (int) (j + start.getZ())) : new BlockPos((int) (j + start.getX()), y + start.getY() + 1, i + start.getZ());
                while (pContext.level().isWaterAt(pos.relative(Direction.DOWN, offset)) || pContext.level().isWaterAt(pos.relative(Direction.DOWN, offset + 1))) {
                    float random = pContext.random().nextFloat();
                    if (random > 0.75) {
                        pContext.level().setBlock(pos.relative(Direction.DOWN, offset), Blocks.MUD.defaultBlockState(), 3);
                    } else if (random > 0.5) {
                        pContext.level().setBlock(pos.relative(Direction.DOWN, offset), Blocks.MANGROVE_ROOTS.defaultBlockState(), 3);
                    } else if (random > 0.3) {
                        pContext.level().setBlock(pos.relative(Direction.DOWN, offset), Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState(), 3);
                    } else if (random > 0.2) {
                        pContext.level().setBlock(pos.relative(Direction.DOWN, offset), BlockRegistry.MUDDY_STICK_BUNDLE.get().defaultBlockState(), 3);
                    } else if (random > 0.1) {
                        pContext.level().setBlock(pos.relative(Direction.DOWN, offset), BlockRegistry.STICK_BUNDLE.get().defaultBlockState(), 3);
                    } else {
                        pContext.level().setBlock(pos.relative(Direction.DOWN, offset), Blocks.OAK_WOOD.defaultBlockState(), 3);
                    }
                    offset++;
                }
            }
        }
        for (int i = 0; i < width + 5; i++) {
            for (float j = -baseWidth; j < baseWidth; j++) {
                int y = (int) (- (maxDepth / (baseWidth * baseWidth)) * (j * j));
                BlockPos pos = axis == Direction.Axis.X ? new BlockPos(i + start.getX(), y + start.getY() + 1, (int) (j + start.getZ())) : new BlockPos((int) (j + start.getX()), y + start.getY() + 1, i + start.getZ());
                if (pContext.random().nextFloat() > 0.65) {
                    if (pContext.level().getBlockState(pos).is(Blocks.DIRT)) continue;
                    pContext.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }


        for (int i = 0; i < 3; i++) {
            var beaver = EntityRegistry.BEAVER.get().create(pContext.level().getLevel());
            if (beaver == null) continue;
            BlockPos pos1;
            BlockPos pos2;
            if (axis == Direction.Axis.X) {
                pos1 = start.offset(0, 1, (int) baseWidth);
                pos2 = end.offset(0, 1, (int) -baseWidth);
            } else {
                pos1 = start.offset((int) baseWidth, 1, 0);
                pos2 = end.offset((int) -baseWidth, 1, 0);
            }
            beaver.finalizeSpawn(pContext.level(), pContext.level().getCurrentDifficultyAt(pContext.origin()), MobSpawnType.STRUCTURE, null, null);

            beaver.setPos(pContext.random().nextIntBetweenInclusive(Math.min(pos1.getX(), pos2.getX()),
                            Math.max(pos1.getX(), pos2.getX())),
                    pos1.getY() + 1,
                    pContext.random().nextIntBetweenInclusive(Math.min(pos1.getZ(), pos2.getZ()), Math.max(pos1.getZ(), pos2.getZ())));
            beaver.setPersistenceRequired();
            pContext.level().addFreshEntity(beaver);
        }
        return true;
    }

    private BlockPos getSurfacePos(WorldGenLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = pos.mutable();
        while (level.getBlockState(mutablePos.above()).is(Blocks.WATER)) {
            mutablePos.move(Direction.UP);
        }
        return mutablePos.immutable();
    }
    private int getWidth(WorldGenLevel level, BlockPos pos, boolean x) {
        int left = 0, right = 0;

        boolean found = false;
        int offset = 0;
        while (!found) {
            if (offset > 16) return 100;
            if (level.getBlockState(pos.relative(x ? Direction.Axis.X : Direction.Axis.Z, offset)).is(Blocks.WATER)) {
                offset++;
            } else {
                right = Math.abs(offset);
                found = true;
            }
        }
        found = false;
        offset = 0;
        while (!found) {
            if (offset < -16) return 100;
            if (level.getBlockState(pos.relative(x ? Direction.Axis.X : Direction.Axis.Z, offset)).is(Blocks.WATER)) {
                offset--;
            } else {
                left = Math.abs(offset);
                found = true;
            }
        }
        if (left > 16 || right > 16) return 100;
        return left + right + 1;
    }

    private int getDepthAt(WorldGenLevel level, BlockPos position) {
        int offset = 0;
        while (level.isWaterAt(position.above(offset))) {
            offset--;
        }
        return Math.abs(offset);
    }

    private int getMaxDepth(WorldGenLevel level, BlockPos start, BlockPos end) {
        Vec3i difference = end.subtract(start);
        int maxDepth = 0;
        for (int x = 0; x <= difference.getX(); x++) {
            for (int z = 0; z <= difference.getZ(); z++) {
                maxDepth = Math.max(getDepthAt(level, start.offset(x, 0, z)), maxDepth);
            }
        }
        return maxDepth;
    }
}

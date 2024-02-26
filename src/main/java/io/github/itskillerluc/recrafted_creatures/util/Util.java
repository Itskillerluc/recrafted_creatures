package io.github.itskillerluc.recrafted_creatures.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

import static net.minecraft.world.entity.ai.util.LandRandomPos.generateRandomPosTowardDirection;
import static net.minecraft.world.entity.ai.util.LandRandomPos.movePosUpOutOfSolid;

public class Util {
    public static BlockPos moveOutOfSolid(BlockPos pPos, int pMaxY, Predicate<BlockPos> pPosPredicate, Direction direction) {
        if (!pPosPredicate.test(pPos)) {
            return pPos;
        } else {
            BlockPos blockpos;
            blockpos = pPos.relative(direction);
            while (Math.abs(pPos.get(direction.getAxis()) - blockpos.get(direction.getAxis())) < pMaxY && pPosPredicate.test(blockpos)) {
                blockpos = blockpos.relative(direction);
            }
            return blockpos;
        }
    }

    public static BlockPos moveOutOfSolidClose(BlockPos pos, int maxY, Predicate<BlockPos> predicate, Entity entity) {
        Direction dir = directionBetweenPos(pos, entity.blockPosition());

        BlockPos up = moveOutOfSolid(pos, maxY, predicate, Direction.UP);
        BlockPos directional = moveOutOfSolid(pos, maxY, predicate, dir);

        int distanceUp = up.distManhattan(pos);
        int distanceDirectional = directional.distManhattan(pos);

        return distanceUp > distanceDirectional ? up : directional;
    }

    public static Direction directionBetweenPos(BlockPos pos1, BlockPos pos2) {
        int xDiff = pos1.getX() - pos2.getX();
        int yDiff = pos1.getY() - pos2.getY();
        int zDiff = pos1.getZ() - pos2.getZ();

        return Direction.getNearest(xDiff, yDiff, zDiff);
    }

    public static Vec3 getPosInDirection(PathfinderMob pMob, int pRadius, int pYRange, Vec3 pVectorPosition, boolean pShortCircuit, double angle) {
        return RandomPos.generateRandomPos(pMob, () -> {
            BlockPos blockpos = RandomPos.generateRandomDirectionWithinRadians(pMob.getRandom(), pRadius, pYRange, 0, pVectorPosition.x, pVectorPosition.z, angle);
            if (blockpos == null) {
                return null;
            } else {
                BlockPos blockpos1 = generateRandomPosTowardDirection(pMob, pRadius, pShortCircuit, blockpos);
                return blockpos1 == null ? null : movePosUpOutOfSolid(pMob, blockpos1);
            }
        });
    }
}

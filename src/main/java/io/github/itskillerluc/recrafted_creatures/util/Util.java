package io.github.itskillerluc.recrafted_creatures.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
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

    public static Vec3 getJumpVector(Mob pMob, Vec3 pTarget, int pAngle, double maxJumpVelocity) {
        Vec3 vec3 = pMob.position();
        Vec3 vec31 = (new Vec3(pTarget.x - vec3.x, 0.0D, pTarget.z - vec3.z)).normalize().scale(0.5D);
        pTarget = pTarget.subtract(vec31);
        Vec3 vec32 = pTarget.subtract(vec3);
        float f = (float)pAngle * (float)Math.PI / 180.0F;
        double d0 = Math.atan2(vec32.z, vec32.x);
        double d1 = vec32.subtract(0.0D, vec32.y, 0.0D).lengthSqr();
        double d2 = Math.sqrt(d1);
        double d3 = vec32.y;
        double d4 = Math.sin(2.0F * f);
        double d5 = 0.08D;
        double d6 = Math.pow(Math.cos(f), 2.0D);
        double d7 = Math.sin(f);
        double d8 = Math.cos(f);
        double d9 = Math.sin(d0);
        double d10 = Math.cos(d0);
        double d11 = d1 * 0.08D / (d2 * d4 - 2.0D * d3 * d6);
        if (d11 < 0.0D) {
            return null;
        } else {
            double d12 = Math.sqrt(d11);
            if (d12 > maxJumpVelocity) {
                return null;
            } else {
                double d13 = d12 * d8;
                double d14 = d12 * d7;
                int i = Mth.ceil(d2 / d13) * 2;
                double d15 = 0.0D;
                Vec3 vec33 = null;
                EntityDimensions entitydimensions = pMob.getDimensions(Pose.LONG_JUMPING);

                for(int j = 0; j < i - 1; ++j) {
                    d15 += d2 / (double)i;
                    double d16 = d7 / d8 * d15 - Math.pow(d15, 2.0D) * 0.08D / (2.0D * d11 * Math.pow(d8, 2.0D));
                    double d17 = d15 * d10;
                    double d18 = d15 * d9;
                    Vec3 vec34 = new Vec3(vec3.x + d17, vec3.y + d16, vec3.z + d18);
                    if (vec33 != null && !isClearTransition(pMob, entitydimensions, vec33, vec34)) {
                        return null;
                    }

                    vec33 = vec34;
                }

                return (new Vec3(d13 * d10, d14, d13 * d9)).scale(0.95F);
            }
        }
    }

    public static boolean isClearTransition(Mob pMob, EntityDimensions pDimensions, Vec3 pStart, Vec3 pEnd) {
        Vec3 vec3 = pEnd.subtract(pStart);
        double d0 = Math.min(pDimensions.width, pDimensions.height);
        int i = Mth.ceil(vec3.length() / d0);
        Vec3 vec31 = vec3.normalize();
        Vec3 vec32 = pStart;

        for(int j = 0; j < i; ++j) {
            vec32 = j == i - 1 ? pEnd : vec32.add(vec31.scale(d0 * (double)0.9F));
            if (!pMob.level().noCollision(pMob, pDimensions.makeBoundingBox(vec32))) {
                return false;
            }
        }

        return true;
    }
}

package io.github.itskillerluc.recrafted_creatures.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

public class Util {
    public static BlockPos moveOutOfSolid(BlockPos pPos, int pMaxY, Predicate<BlockPos> pPosPredicate, Direction direction) {
        if (!pPosPredicate.test(pPos)) {
            return pPos;
        } else {
            BlockPos blockpos;
            for(blockpos = pPos.above(); blockpos.getY() < pMaxY && pPosPredicate.test(blockpos); blockpos = blockpos.above()) {
            }

            return blockpos;
        }
    }

//    public static BlockPos moveOutOfSolidClose(BlockPos pos, int maxY, Predicate<BlockPos> predicate, Entity entity) {
//        Direction dir = directionBetweenPos(pos, entity.blockPosition());
//
//    }

    public static Direction directionBetweenPos(BlockPos pos1, BlockPos pos2) {
        int xDiff = pos1.getX() - pos2.getX();
        int yDiff = pos1.getY() - pos2.getY();
        int zDiff = pos1.getZ() - pos2.getZ();

        return Direction.getNearest(xDiff, yDiff, zDiff);
    }
}

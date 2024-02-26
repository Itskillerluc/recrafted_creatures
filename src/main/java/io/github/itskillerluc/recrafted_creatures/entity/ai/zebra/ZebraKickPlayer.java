package io.github.itskillerluc.recrafted_creatures.entity.ai.zebra;

import io.github.itskillerluc.recrafted_creatures.entity.Zebra;
import io.github.itskillerluc.recrafted_creatures.entity.ai.zebra.ZebraAI;
import io.github.itskillerluc.recrafted_creatures.registries.MemoryModuleRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ZebraKickPlayer extends Behavior<Zebra> {
    public ZebraKickPlayer() {
        super(Map.of(
                MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.ANGRY_AT, MemoryStatus.VALUE_ABSENT,
                MemoryModuleRegistry.HERD.get(), MemoryStatus.REGISTERED,
                MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel pLevel, @NotNull Zebra zebra) {
        if (zebra.isTamed()) return false;
        Player player = zebra.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER).get();
        Vec3 bodyDirection = Vec3.directionFromRotation(0, zebra.getVisualRotationYInDegrees());
        Vec3 relativePlayerPos = zebra.position().subtract(player.position());
        double difference = relativePlayerPos.dot(bodyDirection);

        if (player.distanceToSqr(zebra) <= 4 && difference   > 1.2f && difference < 4.0f) {
            return player.isAlive();
        }
        return false;
    }

    @Override
    protected void start(ServerLevel pLevel, @NotNull Zebra zebra, long pGameTime) {
        Player player = zebra.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER).get();

        zebra.getBrain().setMemoryWithExpiry(MemoryModuleType.IS_PANICKING, true, 600L);
        zebra.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, player, 600L);
        ZebraAI.alertOthers(zebra, player);

        pLevel.broadcastEntityEvent(zebra, (byte) 8);
        zebra.doHurtTarget(player);
        player.knockback(1.4f, zebra.getX() - player.getX(), zebra.getZ() - player.getZ());
    }
}

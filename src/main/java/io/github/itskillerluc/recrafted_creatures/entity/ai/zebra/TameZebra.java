package io.github.itskillerluc.recrafted_creatures.entity.ai.zebra;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TameZebra extends Behavior<AbstractHorse> {
    private double posX;
    private double posY;
    private double posZ;

    public TameZebra() {
        super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED));
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel pLevel, AbstractHorse pOwner) {
        if (!pOwner.isTamed() && pOwner.isVehicle()) {
            Vec3 vec3 = DefaultRandomPos.getPos(pOwner, 5, 4);
            if (vec3 == null) {
                return false;
            } else {
                this.posX = vec3.x;
                this.posY = vec3.y;
                this.posZ = vec3.z;
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    protected void start(ServerLevel pLevel, AbstractHorse pEntity, long pGameTime) {
        pEntity.getNavigation().moveTo(this.posX, this.posY, this.posZ, 1.2f);
    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, AbstractHorse pEntity, long pGameTime) {
        return !pEntity.isTamed() && !pEntity.getNavigation().isDone() && pEntity.isVehicle();
    }

    @Override
    protected void tick(ServerLevel pLevel, AbstractHorse pOwner, long pGameTime) {
        if (!pOwner.isTamed() && pOwner.getRandom().nextInt(25) == 0) {
            Entity entity = pOwner.getPassengers().get(0);
            if (entity == null) {
                return;
            }

            if (entity instanceof Player) {
                int i = pOwner.getTemper();
                int j = pOwner.getMaxTemper();
                if (j > 0 && pOwner.getRandom().nextInt(j) < i && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(pOwner, (Player)entity)) {
                    pOwner.tameWithName((Player)entity);
                    return;
                }

                pOwner.modifyTemper(5);
            }

            pOwner.ejectPassengers();
            pOwner.makeMad();
            pOwner.level().broadcastEntityEvent(pOwner, (byte)6);
        }
    }
}

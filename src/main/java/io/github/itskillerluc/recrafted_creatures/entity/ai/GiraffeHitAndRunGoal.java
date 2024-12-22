package io.github.itskillerluc.recrafted_creatures.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class GiraffeHitAndRunGoal extends Goal {
    private final Mob giraffe;
    @Nullable
    private LivingEntity attacker = null;
    private int cooldown = 0;

    public GiraffeHitAndRunGoal(Mob pGiraffe) {
        this.giraffe = pGiraffe;
    }

    @Override
    public boolean canUse() {
        LivingEntity attacker = this.giraffe.getLastHurtByMob();
        if (attacker == null || this.giraffe.distanceToSqr(attacker) > 16.0D) {
            return false;
        }
        this.attacker = attacker;
        return true;
    }


    @Override
    public void tick() {
        if (this.cooldown > 0) {
            this.cooldown--;
        }
        if (this.attacker != null && this.cooldown == 0) {
            this.giraffe.getLookControl().setLookAt(this.attacker, 30.0F, 30.0F);
            this.giraffe.doHurtTarget(this.attacker);
            this.cooldown = 40;
        } else {
            Vec3 de = DefaultRandomPos.getPos((PathfinderMob) this.giraffe, 10, 4);
            if (de == null) {
                return;
            } else {
                this.giraffe.getNavigation().moveTo(de.x, de.y, de.z, this.giraffe.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue());
            }
        }

        if (this.giraffe.hasPassenger(e -> e instanceof Player)) {
            this.giraffe.ejectPassengers();
        }
    }

    @Override
    public void stop() {
        this.attacker = null;
        this.cooldown = 0;
    }


}

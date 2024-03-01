package io.github.itskillerluc.recrafted_creatures.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class TamableRCMob extends TamableAnimal {
    private static final EntityDataSerializer<Command> COMMAND_SERIALIZER = EntityDataSerializer.simpleEnum(Command.class);
    static final EntityDataAccessor<Command> COMMAND = SynchedEntityData.defineId(TamableRCMob.class, COMMAND_SERIALIZER);

    protected TamableRCMob(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(COMMAND, Command.FOLLOWING);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("command", entityData.get(COMMAND).ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        entityData.set(COMMAND, Command.values()[pCompound.getInt("command")]);
    }

    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        var result = super.mobInteract(pPlayer, pHand);
        if (result == InteractionResult.PASS && this.isTame()) {
            cycleCommand(pPlayer);
            return InteractionResult.SUCCESS;
        }
        return result;
    }

    public void cycleCommand(Player player) {
        entityData.set(COMMAND, switch (entityData.get(COMMAND)) {
            case STAYING -> Command.FOLLOWING;
            case FOLLOWING -> Command.WANDERING;
            case WANDERING -> Command.STAYING;
        });
        if (level().isClientSide()) {
            player.displayClientMessage(Component.translatableWithFallback("recrafted_creatures.command.cycle", "Ordered %s to %s", getName(), entityData.get(COMMAND).name), true);
        }
        setOrderedToSit(entityData.get(COMMAND) == Command.STAYING);
    }

    public enum Command {
        STAYING(Component.translatableWithFallback("recrafted_creatures.command.staying", "Stay")),
        FOLLOWING(Component.translatableWithFallback("recrafted_creatures.command.following", "Follow")),
        WANDERING(Component.translatableWithFallback("recrafted_creatures.command.wandering", "Wander"));

        public final Component name;

        Command(Component name) {
            this.name = name;
        }
    }

    static {
        EntityDataSerializers.registerSerializer(COMMAND_SERIALIZER);
    }
}

package io.github.itskillerluc.recrafted_creatures.capability;

import io.github.itskillerluc.recrafted_creatures.entity.ai.Herd;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IHerd extends INBTSerializable<CompoundTag> {
    Map<UUID, Herd> getHerdMap();
    void addHerd(UUID uuid, Herd herd);
    void removeHerd(UUID id);
    Herd getHerd(UUID id);
}

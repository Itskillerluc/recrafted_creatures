package io.github.itskillerluc.recrafted_creatures.capability;

import io.github.itskillerluc.recrafted_creatures.entity.ai.Herd;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HerdCap implements IHerd {
    private final Map<UUID, Herd> herdMap = new HashMap<>();

    @Override
    public Map<UUID, Herd> getHerdMap() {
        return herdMap;
    }

    @Override
    public void addHerd(UUID uuid, Herd herd) {
        herdMap.put(uuid, herd);
    }

    @Override
    public void removeHerd(UUID id) {
        herdMap.remove(id);
    }

    @Override
    public Herd getHerd(UUID id) {
        return herdMap.get(id);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<UUID, Herd> uuidHerdEntry : herdMap.entrySet()) {
            tag.put(uuidHerdEntry.getKey().toString(), uuidHerdEntry.getValue().save(new CompoundTag()));
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        herdMap.clear();
        for (String key : nbt.getAllKeys()) {
            herdMap.put(UUID.fromString(key), Herd.load(nbt.getCompound(key)));
        }
    }
}

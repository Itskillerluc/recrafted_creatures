package io.github.itskillerluc.recrafted_creatures.entity.ai;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Herd extends SavedData {
    public final List<UUID> animals;
    public final UUID id;
    public final int maxSize;

    @Override
    public @NotNull CompoundTag save(CompoundTag pCompoundTag) {
        ListTag tag = new ListTag();
        for (UUID animal : animals) {
            tag.add(NbtUtils.createUUID(animal));
        }
        pCompoundTag.put("animals", tag);
        pCompoundTag.putUUID("id", id);
        pCompoundTag.putInt("maxSize", maxSize);
        return pCompoundTag;
    }

    public static Herd load(CompoundTag tag) {
        return new Herd(new ArrayList<>(tag.getList("herd", Tag.TAG_INT_ARRAY).stream().map(NbtUtils::loadUUID).toList()), tag.getUUID("id"), tag.getInt("maxSize"));
    }

    public Herd(List<UUID> animals, UUID id, int maxSize) {
        this.animals = animals;
        this.id = id;
        this.maxSize = maxSize;
    }
}

package io.github.itskillerluc.recrafted_creatures.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;

public interface IExtraTick extends INBTSerializable<CompoundTag> {
    List<LevelChunk> getChunks();
    void addChunk(LevelChunk chunk);
    void removeChunk(LevelChunk chunk);
}

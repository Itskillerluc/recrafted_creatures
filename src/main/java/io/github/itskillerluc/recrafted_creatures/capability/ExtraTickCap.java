package io.github.itskillerluc.recrafted_creatures.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;

public class ExtraTickCap implements IExtraTick {
    private final List<LevelChunk> chunks = new ArrayList<>();


    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (LevelChunk chunk : chunks) {
            CompoundTag pos = NbtUtils.writeBlockPos(chunk.getPos().getWorldPosition());
            StringTag level = StringTag.valueOf(chunk.getLevel().dimension().location().toString());
            var compound = new CompoundTag();
            compound.put("pos", pos);
            compound.put("dim", level);
            list.add(compound);
        }
        tag.put("chunks", list);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        chunks.clear();
        var server = ServerLifecycleHooks.getCurrentServer();
        ListTag list = nbt.getList("chunks", Tag.TAG_COMPOUND);
        for (Tag tag : list) {
            BlockPos pos = NbtUtils.readBlockPos(((CompoundTag) tag).getCompound("pos"));
            ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION , new ResourceLocation(((CompoundTag) tag).getString("dim")));
            var level = server.getLevel(dim);
            if (level == null) return;
            chunks.add(level.getChunkAt(pos));
        }
    }

    @Override
    public List<LevelChunk> getChunks() {
        return chunks;
    }

    @Override
    public void addChunk(LevelChunk chunk) {
        chunks.add(chunk);
    }

    @Override
    public void removeChunk(LevelChunk chunk) {
        chunks.remove(chunk);
    }
}

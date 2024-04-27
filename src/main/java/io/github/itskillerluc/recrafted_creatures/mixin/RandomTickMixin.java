package io.github.itskillerluc.recrafted_creatures.mixin;

import io.github.itskillerluc.recrafted_creatures.capability.ExtraTickProvider;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerChunkCache.class)
@Debug(export = true)
public class RandomTickMixin {
    @Redirect(method = "tickChunks()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;tickChunk(Lnet/minecraft/world/level/chunk/LevelChunk;I)V"))
    private void recraftedcreatures$tickChunk(ServerLevel level, LevelChunk chunk, int ticks) {
        chunk.getLevel().getCapability(ExtraTickProvider.EXTRA_TICK_CAP).ifPresent(cap -> {
            if (cap.getChunks().contains(chunk)) {
                level.tickChunk(chunk, ticks * 500);
            }
        });
    }
}
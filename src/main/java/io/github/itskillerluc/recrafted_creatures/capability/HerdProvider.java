package io.github.itskillerluc.recrafted_creatures.capability;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HerdProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(RecraftedCreatures.MODID, "herds");
    public static Capability<IHerd> HERD_CAP = CapabilityManager.get(new CapabilityToken<>() {});

    private IHerd herd = null;
    private final LazyOptional<IHerd> optional = LazyOptional.of(this::createHerd);

    private IHerd createHerd() {
        if (this.herd == null) {
            this.herd = new HerdCap();
        }
        return herd;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == HERD_CAP) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return createHerd().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createHerd().deserializeNBT(nbt);
    }
}

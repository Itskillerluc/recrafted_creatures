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

public class ExtraTickProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(RecraftedCreatures.MODID, "extra_tick");
    public static Capability<IExtraTick> EXTRA_TICK_CAP = CapabilityManager.get(new CapabilityToken<>() {});

    private IExtraTick cap = null;
    private final LazyOptional<IExtraTick> optional = LazyOptional.of(this::createExtraTick);

    private IExtraTick createExtraTick() {
        if (this.cap == null) {
            this.cap = new ExtraTickCap();
        }
        return cap;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == EXTRA_TICK_CAP) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return createExtraTick().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createExtraTick().deserializeNBT(nbt);
    }
}

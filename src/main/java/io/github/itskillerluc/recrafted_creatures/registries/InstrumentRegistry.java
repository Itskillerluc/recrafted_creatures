package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Instrument;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class InstrumentRegistry {
    public static DeferredRegister<Instrument> INSTRUMENTS = DeferredRegister.create(Registries.INSTRUMENT, RecraftedCreatures.MODID);


    public static final RegistryObject<Instrument> MEGAPHONE = INSTRUMENTS.register("megaphone",
            () -> new Instrument(SoundRegistry.MEGAPHONE_SOUND.getHolder().get(), 20, 256));
}

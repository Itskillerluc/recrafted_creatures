package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry{
        public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, RecraftedCreatures.MODID);

        public static final RegistryObject<SoundEvent> GIRAFFE_DEATH = register("entity.giraffe_death");
        public static final RegistryObject<SoundEvent> GIRAFFE_HURT = register("entity.giraffe_hurt");
        public static final RegistryObject<SoundEvent> GIRAFFE_SOUND = register("entity.giraffe_sound");

        public static final RegistryObject<SoundEvent> RED_PANDA_DEATH = register("entity.red_panda_death");
        public static final RegistryObject<SoundEvent> RED_PANDA_HURT = register("entity.red_panda_hurt");
        public static final RegistryObject<SoundEvent> RED_PANDA_SOUND = register("entity.red_panda_sound");

        public static final RegistryObject<SoundEvent> MAMMOTH_DEATH = register("entity.mammoth_death");
        public static final RegistryObject<SoundEvent> MAMMOTH_HURT = register("entity.mammoth_hurt");
        public static final RegistryObject<SoundEvent> MAMMOTH_SOUND = register("entity.mammoth_sound");
        public static final RegistryObject<SoundEvent> MAMMOTH_TRUMPET = register("entity.mammoth_trumpet");


        private static RegistryObject<SoundEvent> register(String name) {
            return SOUNDS.register(name, () -> new SoundEvent(new ResourceLocation(RecraftedCreatures.MODID, name)));
        }
}

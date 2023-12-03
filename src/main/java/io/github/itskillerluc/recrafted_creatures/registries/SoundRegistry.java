package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry{
        public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, RecraftedCreatures.MODID);

        public static final RegistryObject<SoundEvent> RED_PANDA_DEATH = register("entity.red_panda_death");
        public static final RegistryObject<SoundEvent> RED_PANDA_HURT = register("entity.red_panda_hurt");
        public static final RegistryObject<SoundEvent> RED_PANDA_AMBIENCE = register("entity.red_panda_ambience");

        public static final RegistryObject<SoundEvent> MAMMOTH_DEATH = register("entity.mammoth_death");
        public static final RegistryObject<SoundEvent> MAMMOTH_HURT = register("entity.mammoth_hurt");
        public static final RegistryObject<SoundEvent> MAMMOTH_AMBIENCE = register("entity.mammoth_ambience");
        public static final RegistryObject<SoundEvent> MAMMOTH_TRUMPET = register("entity.mammoth_trumpet");

        public static final RegistryObject<SoundEvent> CHAMELEON_HURT = register("entity.chameleon_hurt");

        public static final RegistryObject<SoundEvent> MARMOT_ALERT = register("entity.marmot_alert");
        public static final RegistryObject<SoundEvent> MARMOT_AMBIENCE = register("entity.marmot_ambience");
        public static final RegistryObject<SoundEvent> MARMOT_HURT = register("entity.marmot_hurt");

        public static final RegistryObject<SoundEvent> OWL_DEATH = register("entity.owl_death");
        public static final RegistryObject<SoundEvent> OWL_HOOT = register("entity.owl_hoot");
        public static final RegistryObject<SoundEvent> OWL_AMBIENCE = register("entity.owl_ambience");

        public static final RegistryObject<SoundEvent> MEGAPHONE_SOUND = register("item.megaphone_sound");


        private static RegistryObject<SoundEvent> register(String name) {
            return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(RecraftedCreatures.MODID, name)));
        }
}

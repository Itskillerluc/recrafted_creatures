package io.github.itskillerluc.recrafted_creatures.advancement;

import com.google.gson.JsonObject;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class BeaverBuildTrigger extends SimpleCriterionTrigger<BeaverBuildTrigger.TriggerInstance> {
    public static BeaverBuildTrigger INSTANCE;
    static final ResourceLocation ID = new ResourceLocation(RecraftedCreatures.MODID, "beaver_build");

    public ResourceLocation getId() {
        return ID;
    }

    public BeaverBuildTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
        return new BeaverBuildTrigger.TriggerInstance(pPredicate);
    }

    public void trigger(ServerPlayer pPlayer) {
        this.trigger(pPlayer, (p_23687_) -> true);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        public TriggerInstance(ContextAwarePredicate pPlayer) {
            super(BeaverBuildTrigger.ID, pPlayer);
        }


        public JsonObject serializeToJson(SerializationContext pConditions) {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            return jsonobject;
        }
    }
}

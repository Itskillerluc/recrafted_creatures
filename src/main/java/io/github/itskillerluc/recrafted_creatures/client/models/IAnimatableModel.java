package io.github.itskillerluc.recrafted_creatures.client.models;

import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;

import java.util.Optional;

public interface IAnimatableModel {
    ModelPart root();
    Vector3f getVectorCache();
    default Optional<ModelPart> getAnyDescendantWithName(String pName) {
        return pName.equals("root") ? Optional.of(this.root()) : this.root().getAllParts().filter((p_233400_) -> p_233400_.hasChild(pName)).findFirst().map((p_233397_) -> p_233397_.getChild(pName));
    }
}

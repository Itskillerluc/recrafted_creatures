package io.github.itskillerluc.recrafted_creatures.entity.ai;

import net.minecraft.world.entity.item.ItemEntity;

public interface FoodSearching {
    ItemEntity getItemTarget();
    void setItemTarget(ItemEntity target);
}

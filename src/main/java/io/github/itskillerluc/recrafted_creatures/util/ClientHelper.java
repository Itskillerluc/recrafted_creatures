package io.github.itskillerluc.recrafted_creatures.util;

import io.github.itskillerluc.recrafted_creatures.blockentity.ConstructorBlockEntity;
import io.github.itskillerluc.recrafted_creatures.entity.Owl;
import io.github.itskillerluc.recrafted_creatures.screen.ConstructorScreen;
import io.github.itskillerluc.recrafted_creatures.screen.DeliveryScreen;
import net.minecraft.client.Minecraft;

public class ClientHelper {
    public static void openDeliveryScreen(Owl owl) {
        Minecraft.getInstance().setScreen(new DeliveryScreen(owl));
    }

    public static void openConstructorScreen(ConstructorBlockEntity blockEntity) {
        Minecraft.getInstance().setScreen(new ConstructorScreen(blockEntity));
    }
}

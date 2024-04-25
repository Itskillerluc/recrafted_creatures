package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.blockentity.ConstructorBlockEntity;
import io.github.itskillerluc.recrafted_creatures.blockentity.EggBlockEntity;
import io.github.itskillerluc.recrafted_creatures.blockentity.ThornBlockEntity;
import io.github.itskillerluc.recrafted_creatures.menu.BeaverMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MenuRegistry {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, RecraftedCreatures.MODID);

    public static final RegistryObject<MenuType<BeaverMenu>> BEAVER_MENU = MENU_TYPES.register("beaver_menu",
            () -> IForgeMenuType.create(BeaverMenu::new));
}

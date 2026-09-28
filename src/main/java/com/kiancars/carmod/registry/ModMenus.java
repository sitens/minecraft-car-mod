package com.kiancars.carmod.registry;

import com.kiancars.carmod.CarMod;
import com.kiancars.carmod.menu.BigCraftingMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, CarMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<BigCraftingMenu>> CRAFTING_TABLE_4X4 =
            MENUS.register("crafting_table_4x4", () -> IMenuTypeExtension.create(
                    (windowId, inv, data) -> BigCraftingMenu.fourByFour(windowId, inv, ContainerLevelAccess.NULL)));

    public static final DeferredHolder<MenuType<?>, MenuType<BigCraftingMenu>> CRAFTING_TABLE_5X5 =
            MENUS.register("crafting_table_5x5", () -> IMenuTypeExtension.create(
                    (windowId, inv, data) -> BigCraftingMenu.fiveByFive(windowId, inv, ContainerLevelAccess.NULL)));

    private ModMenus() {
    }
}

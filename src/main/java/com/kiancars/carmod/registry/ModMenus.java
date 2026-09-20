package com.kiancars.carmod.registry;

import com.kiancars.carmod.CarMod;
import com.kiancars.carmod.menu.CraftingTable4x4Menu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, CarMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<CraftingTable4x4Menu>> CRAFTING_TABLE_4X4 =
            MENUS.register("crafting_table_4x4",
                    () -> IMenuTypeExtension.create((windowId, inv, data) ->
                            new CraftingTable4x4Menu(windowId, inv)));

    private ModMenus() {
    }
}

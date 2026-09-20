package com.kiancars.carmod;

import com.kiancars.carmod.registry.ModBlocks;
import com.kiancars.carmod.registry.ModCreativeTabs;
import com.kiancars.carmod.registry.ModEntities;
import com.kiancars.carmod.registry.ModItems;
import com.kiancars.carmod.registry.ModMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Main mod entry point. Registers deferred registers to the mod event bus.
 * <p>
 * Package/mod id are placeholders ({@code carmod}) until the mod has a real
 * name — rename via find/replace across the project when that's decided.
 */
@Mod(CarMod.MOD_ID)
public class CarMod {

    public static final String MOD_ID = "carmod";

    public CarMod(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
    }
}

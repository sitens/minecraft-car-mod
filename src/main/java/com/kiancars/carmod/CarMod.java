package com.kiancars.carmod;

import com.kiancars.carmod.event.ModEvents;
import com.kiancars.carmod.registry.ModBlocks;
import com.kiancars.carmod.registry.ModCreativeTabs;
import com.kiancars.carmod.registry.ModEffects;
import com.kiancars.carmod.registry.ModEntities;
import com.kiancars.carmod.registry.ModItems;
import com.kiancars.carmod.registry.ModLootModifiers;
import com.kiancars.carmod.registry.ModMenus;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(CarMod.MOD_ID)
public class CarMod {

    public static final String MOD_ID = "carmod";

    public CarMod(IEventBus modEventBus) {
        // Shaped recipes are capped at 3x3 unless raised; the big tables need up to 5x5.
        ShapedRecipePattern.setCraftingSize(5, 5);

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModLootModifiers.MODIFIERS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);

        modEventBus.addListener(ModEvents::onAttributes);
        NeoForge.EVENT_BUS.addListener(ModEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(ModEvents::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(ModEvents::onItemFinished);
        NeoForge.EVENT_BUS.addListener(ModEvents::onExplosion);
    }
}

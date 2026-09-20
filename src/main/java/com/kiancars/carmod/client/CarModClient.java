package com.kiancars.carmod.client;

import com.kiancars.carmod.CarMod;
import com.kiancars.carmod.client.renderer.CarRenderer;
import com.kiancars.carmod.client.screen.CraftingTable4x4Screen;
import com.kiancars.carmod.registry.ModEntities;
import com.kiancars.carmod.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

// NOTE: confirmed against real 1.21.11 sources — EventBusSubscriber dropped
// its bus()/Bus element entirely; it now only targets the mod event bus,
// selected here for the client distribution via value = Dist.CLIENT.
@EventBusSubscriber(modid = CarMod.MOD_ID, value = Dist.CLIENT)
public final class CarModClient {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CAR.get(), CarRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.CRAFTING_TABLE_4X4.get(), CraftingTable4x4Screen::new);
    }

    private CarModClient() {
    }
}

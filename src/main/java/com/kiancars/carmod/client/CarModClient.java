package com.kiancars.carmod.client;

import com.kiancars.carmod.CarMod;
import com.kiancars.carmod.client.model.HoesacBossModel;
import com.kiancars.carmod.client.renderer.CarRenderer;
import com.kiancars.carmod.client.renderer.HoesacBossRenderer;
import com.kiancars.carmod.client.renderer.HoesacRenderer;
import com.kiancars.carmod.client.screen.BigCraftingScreen;
import com.kiancars.carmod.client.screen.HeroWorkshopScreen;
import com.kiancars.carmod.registry.ModEntities;
import com.kiancars.carmod.registry.ModMenus;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

// EventBusSubscriber in this NeoForge version has no bus() option; it picks
// the mod bus for these registration events on its own.
@EventBusSubscriber(modid = CarMod.MOD_ID, value = Dist.CLIENT)
public final class CarModClient {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CAR.get(), CarRenderer::new);
        event.<Zombie>registerEntityRenderer(ModEntities.HOESAC.get(), HoesacRenderer::new);
        event.registerEntityRenderer(ModEntities.HOESAC_BOSS.get(), HoesacBossRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(HoesacBossModel.LAYER, HoesacBossModel::createLayer);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.CRAFTING_TABLE_4X4.get(), BigCraftingScreen::new);
        event.register(ModMenus.CRAFTING_TABLE_5X5.get(), BigCraftingScreen::new);
        event.register(ModMenus.AI_WORKSHOP.get(), HeroWorkshopScreen::new);
    }

    private CarModClient() {
    }
}

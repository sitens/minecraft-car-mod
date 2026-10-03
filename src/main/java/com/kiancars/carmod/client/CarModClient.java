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
import com.kiancars.carmod.hero.HeroNetwork;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;
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

    private static final KeyMapping.Category KEY_CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(CarMod.MOD_ID, "hero"));
    private static final KeyMapping ABILITY_KEY =
            new KeyMapping("key.carmod.ability", GLFW.GLFW_KEY_G, KEY_CATEGORY);
    private static final KeyMapping CYCLE_KEY =
            new KeyMapping("key.carmod.cycle_ability", GLFW.GLFW_KEY_H, KEY_CATEGORY);

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.registerCategory(KEY_CATEGORY);
        event.register(ABILITY_KEY);
        event.register(CYCLE_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (ABILITY_KEY.consumeClick()) {
            ClientPacketDistributor.sendToServer(new HeroNetwork.AbilityPayload(0));
        }
        while (CYCLE_KEY.consumeClick()) {
            ClientPacketDistributor.sendToServer(new HeroNetwork.AbilityPayload(1));
        }
    }

    private CarModClient() {
    }
}

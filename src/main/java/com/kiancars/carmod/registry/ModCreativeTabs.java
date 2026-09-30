package com.kiancars.carmod.registry;

import com.kiancars.carmod.CarMod;
import com.kiancars.carmod.entity.CarType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, CarMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CAR_TAB =
            CREATIVE_TABS.register("car_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.carmod.car_tab"))
                    .icon(() -> new ItemStack(ModItems.CRAFTING_TABLE_4X4.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.CRAFTING_TABLE_4X4.get());
                        output.accept(ModItems.CRAFTING_TABLE_5X5.get());
                        for (CarType type : CarType.values()) {
                            output.accept(ModItems.byCarType(type).get());
                        }
                        output.accept(ModItems.TACK.get());
                        output.accept(ModItems.WOODEN_BUCKET.get());
                        output.accept(ModItems.WOODEN_WATER_BUCKET.get());
                        output.accept(ModItems.WOODEN_LAVA_BUCKET.get());
                        output.accept(ModItems.HOESAC_SPAWN_EGG.get());
                        output.accept(ModItems.HOESAC_BOSS_SPAWN_EGG.get());
                        ModItems.junkFoods().forEach(item -> output.accept(item.get()));
                        ModItems.verticalSlabItems().values().forEach(item -> output.accept(item.get()));
                    })
                    .build());

    private ModCreativeTabs() {
    }
}

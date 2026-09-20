package com.kiancars.carmod.registry;

import com.kiancars.carmod.CarMod;
import com.kiancars.carmod.entity.CarEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, CarMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<CarEntity>> CAR =
            ENTITY_TYPES.register("car", () -> EntityType.Builder.<CarEntity>of(CarEntity::new, MobCategory.MISC)
                    .sized(1.5F, 0.8F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(CarMod.MOD_ID, "car"))));

    private ModEntities() {
    }
}

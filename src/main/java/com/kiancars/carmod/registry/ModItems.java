package com.kiancars.carmod.registry;

import com.kiancars.carmod.CarMod;
import com.kiancars.carmod.entity.CarType;
import com.kiancars.carmod.item.CarItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CarMod.MOD_ID);

    public static final DeferredItem<BlockItem> CRAFTING_TABLE_4X4 =
            ITEMS.registerSimpleBlockItem("crafting_table_4x4", ModBlocks.CRAFTING_TABLE_4X4);

    private static final Map<CarType, DeferredItem<CarItem>> CAR_ITEMS = new EnumMap<>(CarType.class);

    static {
        for (CarType type : CarType.values()) {
            // Rarity climbs with crafting difficulty; purely cosmetic (name color).
            Rarity rarity = switch (type) {
                case SEDAN, PICKUP, OFFROADER -> Rarity.COMMON;
                case SPORTS_CAR, LIMOUSINE, RACER -> Rarity.UNCOMMON;
                case MONSTER_TRUCK, ARMORED_CAR -> Rarity.RARE;
                case HOVER_PROTOTYPE, GOLDEN_LUXURY -> Rarity.EPIC;
            };
            CAR_ITEMS.put(type, ITEMS.register("car_" + type.getId(),
                    () -> new CarItem(type, new Item.Properties().stacksTo(1).rarity(rarity))));
        }
    }

    public static DeferredItem<CarItem> byCarType(CarType type) {
        return CAR_ITEMS.get(type);
    }

    private ModItems() {
    }
}

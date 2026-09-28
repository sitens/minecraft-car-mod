package com.kiancars.carmod.registry;

import com.kiancars.carmod.CarMod;
import com.kiancars.carmod.entity.CarType;
import com.kiancars.carmod.item.CarItem;
import com.kiancars.carmod.item.WoodenBucketItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Items must be created with their registry name already set in this
 * Minecraft version, so everything goes through {@code registerItem} /
 * {@code registerSimpleBlockItem} (the plain {@code register} doesn't set it
 * and crashes the game on startup).
 */
public final class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CarMod.MOD_ID);

    public static final DeferredItem<BlockItem> CRAFTING_TABLE_4X4 =
            ITEMS.registerSimpleBlockItem("crafting_table_4x4", ModBlocks.CRAFTING_TABLE_4X4);

    public static final DeferredItem<BlockItem> CRAFTING_TABLE_5X5 =
            ITEMS.registerSimpleBlockItem("crafting_table_5x5", ModBlocks.CRAFTING_TABLE_5X5);

    public static final DeferredItem<DoubleHighBlockItem> TACK =
            ITEMS.registerItem("tack", props -> new DoubleHighBlockItem(ModBlocks.TACK.get(), props),
                    props -> props.useBlockDescriptionPrefix());

    public static final DeferredItem<WoodenBucketItem> WOODEN_BUCKET =
            ITEMS.registerItem("wooden_bucket", props -> new WoodenBucketItem(Fluids.EMPTY, props),
                    props -> props.durability(WoodenBucketItem.MAX_DURABILITY));

    public static final DeferredItem<WoodenBucketItem> WOODEN_WATER_BUCKET =
            ITEMS.registerItem("wooden_water_bucket", props -> new WoodenBucketItem(Fluids.WATER, props),
                    props -> props.durability(WoodenBucketItem.MAX_DURABILITY));

    public static final DeferredItem<WoodenBucketItem> WOODEN_LAVA_BUCKET =
            ITEMS.registerItem("wooden_lava_bucket", props -> new WoodenBucketItem(Fluids.LAVA, props),
                    props -> props.durability(WoodenBucketItem.MAX_DURABILITY));

    public static final DeferredItem<SpawnEggItem> HOESAC_SPAWN_EGG =
            ITEMS.registerItem("hoesac_spawn_egg", SpawnEggItem::new,
                    props -> props.spawnEgg(ModEntities.HOESAC.get()));

    private static final Map<CarType, DeferredItem<CarItem>> CAR_ITEMS = new EnumMap<>(CarType.class);
    private static final Map<String, DeferredItem<BlockItem>> VERTICAL_SLAB_ITEMS = new LinkedHashMap<>();

    static {
        for (CarType type : CarType.values()) {
            // Rarity climbs with crafting difficulty; purely cosmetic (name color).
            Rarity rarity = switch (type) {
                case SEDAN, PICKUP, OFFROADER -> Rarity.COMMON;
                case SPORTS_CAR, LIMOUSINE, RACER -> Rarity.UNCOMMON;
                case MONSTER_TRUCK, ARMORED_CAR -> Rarity.RARE;
                case HOVER_PROTOTYPE, GOLDEN_LUXURY -> Rarity.EPIC;
            };
            CAR_ITEMS.put(type, ITEMS.registerItem("car_" + type.getId(),
                    props -> new CarItem(type, props),
                    props -> props.stacksTo(1).rarity(rarity)));
        }

        ModBlocks.verticalSlabs().forEach((base, block) ->
                VERTICAL_SLAB_ITEMS.put(base, ITEMS.registerSimpleBlockItem(base + "_vertical_slab", block)));
    }

    public static DeferredItem<CarItem> byCarType(CarType type) {
        return CAR_ITEMS.get(type);
    }

    public static Map<String, DeferredItem<BlockItem>> verticalSlabItems() {
        return Collections.unmodifiableMap(VERTICAL_SLAB_ITEMS);
    }

    private ModItems() {
    }
}

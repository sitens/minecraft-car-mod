package com.kiancars.carmod.registry;

import com.kiancars.carmod.CarMod;
import com.kiancars.carmod.entity.CarType;
import com.kiancars.carmod.item.CarItem;
import com.kiancars.carmod.item.WoodenBucketItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Items must be created with their registry name already set in this
 * Minecraft version, so everything goes through {@code registerItem} /
 * {@code registerSimpleBlockItem} (the plain {@code register} doesn't set it
 * and crashes the game on startup).
 */
public final class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CarMod.MOD_ID);

    // ---------------------------------------------------------------- junk food
    // Every junk food fills 2.5 hunger bars (5 points). All 10 are in the
    // carmod:junk_food item tag, which is what attracts hOesaC.

    private static final FoodProperties BEANS = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.3F)
            .build();

    private static final FoodProperties JUNK_FOOD = new FoodProperties.Builder()
            .nutrition(10)
            .saturationModifier(0.3F)
            .build();

    private static final List<DeferredItem<Item>> JUNK_FOODS = new ArrayList<>();

    public static final DeferredItem<Item> DORINOS = junkFood("dorinos", props -> props);
    public static final DeferredItem<Item> LAYZ = junkFood("layz", props -> props);
    public static final DeferredItem<Item> FRITOZ = junkFood("fritoz", props -> props);
    public static final DeferredItem<Item> LAYZ_BBQ = junkFood("layz_bbq", props -> props);
    public static final DeferredItem<Item> CANDY_BAR = junkFood("candy_bar", props -> props);
    public static final DeferredItem<Item> GUMMY_WORMS = junkFood("gummy_worms", props -> props);
    public static final DeferredItem<Item> DONUT = junkFood("donut", props -> props);
    public static final DeferredItem<Item> LOLLIPOP = junkFood("lollipop", props -> props);

    public static final DeferredItem<Item> SODA =
            registerJunkFood("soda", props -> props.food(JUNK_FOOD, Consumables.DEFAULT_DRINK)
                    .usingConvertsTo(Items.GLASS_BOTTLE).stacksTo(16));

    public static final DeferredItem<Item> POPCORN =
            registerJunkFood("popcorn", props -> props.food(JUNK_FOOD)
                    .usingConvertsTo(Items.BOWL).stacksTo(16));

    public static final DeferredItem<Item> PIZZA_SLICE = junkFood("pizza_slice", props -> props);
    public static final DeferredItem<Item> HOT_DOG = junkFood("hot_dog", props -> props);
    public static final DeferredItem<Item> ICE_CREAM = junkFood("ice_cream", props -> props);
    public static final DeferredItem<Item> COTTON_CANDY = junkFood("cotton_candy", props -> props);
    public static final DeferredItem<Item> CHEEZY_PUFFS = junkFood("cheezy_puffs", props -> props);

    /** 3 hunger bars and the Farting effect (see ModEvents). Not junk food: it doesn't attract hOesaC. */
    public static final DeferredItem<Item> CAN_OF_BEANS =
            ITEMS.registerItem("can_of_beans", Item::new, props -> props.food(BEANS).stacksTo(16));

    public static final DeferredItem<BlockItem> ALTAR =
            ITEMS.registerSimpleBlockItem("altar", ModBlocks.ALTAR);

    private static DeferredItem<Item> junkFood(String name, UnaryOperator<Item.Properties> extra) {
        return registerJunkFood(name, props -> extra.apply(props.food(JUNK_FOOD)));
    }

    private static DeferredItem<Item> registerJunkFood(String name, UnaryOperator<Item.Properties> props) {
        DeferredItem<Item> item = ITEMS.registerItem(name, Item::new, props);
        JUNK_FOODS.add(item);
        return item;
    }

    public static List<DeferredItem<Item>> junkFoods() {
        return Collections.unmodifiableList(JUNK_FOODS);
    }

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

    public static final DeferredItem<SpawnEggItem> HOESAC_BOSS_SPAWN_EGG =
            ITEMS.registerItem("hoesac_final_boss_spawn_egg", SpawnEggItem::new,
                    props -> props.spawnEgg(ModEntities.HOESAC_BOSS.get()));

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

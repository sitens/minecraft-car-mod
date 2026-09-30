package com.kiancars.carmod.loot;

import com.kiancars.carmod.registry.ModItems;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.List;

/**
 * Sprinkles junk food (and now and then a can of beans) into every chest
 * loot table: villages, abandoned villages, pillager outposts, trial
 * chambers, dungeons, mineshafts, and every other structure with a chest.
 * Tables are matched by name ("chests/...") instead of one by one, so
 * structures added by other mods are covered too.
 */
public class JunkFoodLootModifier extends LootModifier {

    public static final MapCodec<JunkFoodLootModifier> CODEC =
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, JunkFoodLootModifier::new));

    private static final float JUNK_FOOD_CHANCE = 0.45F;
    private static final float BEANS_CHANCE = 0.12F;

    public JunkFoodLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (!context.getQueriedLootTableId().getPath().startsWith("chests/")) {
            return loot;
        }
        RandomSource random = context.getRandom();
        List<? extends net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item>> foods = ModItems.junkFoods();
        if (random.nextFloat() < JUNK_FOOD_CHANCE) {
            int kinds = 1 + random.nextInt(2);
            for (int i = 0; i < kinds; i++) {
                loot.add(new ItemStack(foods.get(random.nextInt(foods.size())).get(), 1 + random.nextInt(3)));
            }
        }
        if (random.nextFloat() < BEANS_CHANCE) {
            loot.add(new ItemStack(ModItems.CAN_OF_BEANS.get(), 1 + random.nextInt(2)));
        }
        return loot;
    }

    @Override
    public MapCodec<? extends net.neoforged.neoforge.common.loot.IGlobalLootModifier> codec() {
        return CODEC;
    }
}

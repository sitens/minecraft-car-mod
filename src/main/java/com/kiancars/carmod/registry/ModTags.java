package com.kiancars.carmod.registry;

import com.kiancars.carmod.CarMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public final class ModTags {

    /** Items that make hOesaC show up, and the only things he takes from you. */
    public static final TagKey<Item> JUNK_FOOD =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(CarMod.MOD_ID, "junk_food"));

    public static final ResourceKey<DamageType> TACK_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(CarMod.MOD_ID, "tack"));

    public static final ResourceKey<DamageType> HOESAC_ROLL =
            ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(CarMod.MOD_ID, "hoesac_roll"));

    public static final ResourceKey<DamageType> HOESAC_SUFFOCATE =
            ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(CarMod.MOD_ID, "hoesac_suffocate"));

    public static boolean isCarryingJunkFood(LivingEntity entity) {
        return entity instanceof Player player && player.getInventory().contains(JUNK_FOOD);
    }

    private ModTags() {
    }
}

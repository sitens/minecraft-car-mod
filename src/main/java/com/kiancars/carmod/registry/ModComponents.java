package com.kiancars.carmod.registry;

import com.kiancars.carmod.CarMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModComponents {

    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CarMod.MOD_ID);

    /** Which AI design tier a piece of hero gear is. Items without it aren't hero gear. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> HERO_TIER =
            COMPONENTS.registerComponentType("hero_tier",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /** The search the AI built this gear from, so its powers can be worked out again. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> HERO_THEME =
            COMPONENTS.registerComponentType("hero_theme",
                    builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    private ModComponents() {
    }
}

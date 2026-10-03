package com.kiancars.carmod.hero;

import com.kiancars.carmod.CarMod;
import com.kiancars.carmod.menu.HeroWorkshopMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** The two messages the client can send: "craft this" and "use my ability". */
public final class HeroNetwork {

    /** Asks the server to craft one piece of gear from a search at a tier. */
    public record CraftPayload(String query, int tier, int gear) implements CustomPacketPayload {
        public static final Type<CraftPayload> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CarMod.MOD_ID, "hero_craft"));
        public static final StreamCodec<RegistryFriendlyByteBuf, CraftPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.stringUtf8(64), CraftPayload::query,
                ByteBufCodecs.VAR_INT, CraftPayload::tier,
                ByteBufCodecs.VAR_INT, CraftPayload::gear,
                CraftPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Ability key presses. {@code action}: 0 = use the suit ability, 1 = cycle the selected ability. */
    public record AbilityPayload(int action) implements CustomPacketPayload {
        public static final Type<AbilityPayload> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(CarMod.MOD_ID, "hero_ability"));
        public static final StreamCodec<RegistryFriendlyByteBuf, AbilityPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, AbilityPayload::action, AbilityPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(CraftPayload.TYPE, CraftPayload.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof HeroWorkshopMenu menu && menu.stillValid(player)) {
                HeroGear gear = HeroGear.byIndex(payload.gear());
                if (gear != null) {
                    HeroWorkshopMenu.craft(player, payload.query(), payload.tier(), gear);
                }
            }
        });
        registrar.playToServer(AbilityPayload.TYPE, AbilityPayload.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) {
                if (payload.action() == 0) {
                    HeroAbilities.useSuitAbility(player);
                } else {
                    HeroAbilities.cycle(player);
                }
            }
        });
    }

    private HeroNetwork() {
    }
}

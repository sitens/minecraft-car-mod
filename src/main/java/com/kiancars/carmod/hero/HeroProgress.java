package com.kiancars.carmod.hero;

import com.kiancars.carmod.registry.ModAttachments;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.player.Player;

/**
 * One player's hero progress, saved with the player and kept after dying.
 *
 * @param tier  the highest gear tier the AI has designed for this player
 * @param kills non-hostile mobs defeated since the last upgrade
 */
public record HeroProgress(int tier, int kills) {

    public static final HeroProgress START = new HeroProgress(1, 0);

    public static final MapCodec<HeroProgress> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.INT.optionalFieldOf("tier", 1).forGetter(HeroProgress::tier),
            Codec.INT.optionalFieldOf("kills", 0).forGetter(HeroProgress::kills)
    ).apply(inst, HeroProgress::new));

    public static HeroProgress of(Player player) {
        return player.getData(ModAttachments.HERO_PROGRESS);
    }

    public static void save(Player player, HeroProgress progress) {
        player.setData(ModAttachments.HERO_PROGRESS, progress);
    }

    public int needed() {
        return HeroGenerator.killsNeeded(tier);
    }

    public boolean canUpgrade() {
        return kills >= needed();
    }

    public HeroProgress withKill() {
        return new HeroProgress(tier, kills + 1);
    }

    /** Moves up one tier; any extra kills carry over toward the next one. */
    public HeroProgress upgraded() {
        return new HeroProgress(tier + 1, Math.max(0, kills - needed()));
    }
}

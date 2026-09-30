package com.kiancars.carmod.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Every half second (10 ticks) you get blasted about a block into the air
 * and let out a toot. Fall damage is cancelled while it lasts so the ride
 * is funny instead of deadly.
 * <p>
 * There's no real fart sound in vanilla Minecraft, so this uses the
 * pufferfish blow-out noise pitched down. A real sound needs an .ogg file.
 */
public class FartingEffect extends MobEffect {

    public static final int PULSE_TICKS = 10;
    /** Upward speed per toot. Measured on a test pig: each toot lifts about 1 block (movement is applied before gravity, so the maths is not just v*v/2g). */
    private static final double BOOST = 0.475;

    public FartingEffect() {
        super(MobEffectCategory.NEUTRAL, 0x8A7A2B);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % PULSE_TICKS == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x, BOOST, motion.z);
        entity.resetFallDistance();
        entity.hurtMarked = true;
        float pitch = 0.45F + level.getRandom().nextFloat() * 0.25F;
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.PUFFER_FISH_BLOW_OUT, SoundSource.PLAYERS, 1.2F, pitch);
        return true;
    }
}

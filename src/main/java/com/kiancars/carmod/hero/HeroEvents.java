package com.kiancars.carmod.hero;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Optional;

/**
 * Makes hero gear do things: the suit's passive powers, tool powers while
 * held, weapon powers when you hit something, and counting the non-hostile
 * mobs you defeat toward the next tier.
 */
public final class HeroEvents {

    /** Stops a power (like Shockwave) from triggering more powers when it hurts something. */
    private static boolean applyingPower;

    // ------------------------------------------------------ passive powers

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }
        applyPassives(player, player.getItemBySlot(EquipmentSlot.CHEST));
        ItemStack held = player.getMainHandItem();
        if (HeroItems.gearOf(held) != null && HeroItems.gearOf(held) != HeroGear.SUIT) {
            applyPassives(player, held);
        }
    }

    private static void applyPassives(ServerPlayer player, ItemStack gear) {
        HeroDesign design = HeroItems.designOf(gear);
        if (design == null) {
            return;
        }
        for (HeroDesign.PowerLevel power : design.powers()) {
            String effectId = power.power().effectId();
            if (power.power().kind() != HeroPower.Kind.PASSIVE || effectId == null) {
                continue;
            }
            Optional<Holder.Reference<MobEffect>> effect =
                    BuiltInRegistries.MOB_EFFECT.get(Identifier.withDefaultNamespace(effectId));
            // Refreshed every second with a short timer, so taking the gear off ends the power quickly.
            effect.ifPresent(holder -> player.addEffect(
                    new MobEffectInstance(holder, 60, power.level() - 1, true, false, false)));
        }
    }

    // ------------------------------------------------------- weapon powers

    /** Execute adds damage before armor is applied. */
    public static void onDamagePre(LivingDamageEvent.Pre event) {
        HeroDesign design = attackerWeapon(event.getSource());
        if (design == null) {
            return;
        }
        LivingEntity target = event.getEntity();
        for (HeroDesign.PowerLevel power : design.powers()) {
            if (power.power() == HeroPower.EXECUTE && target.getHealth() <= target.getMaxHealth() * 0.3F) {
                event.setNewDamage(event.getNewDamage() * (1.0F + 0.25F * power.level()));
            }
        }
    }

    public static void onDamagePost(LivingDamageEvent.Post event) {
        if (applyingPower || !(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        HeroDesign design = attackerWeapon(event.getSource());
        if (design == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity target = event.getEntity();
        float damage = event.getNewDamage();
        applyingPower = true;
        try {
            for (HeroDesign.PowerLevel power : design.powers()) {
                applyHitPower(level, player, target, damage, power);
            }
        } finally {
            applyingPower = false;
        }
    }

    private static void applyHitPower(ServerLevel level, ServerPlayer player, LivingEntity target, float damage,
                                      HeroDesign.PowerLevel power) {
        int lvl = power.level();
        switch (power.power()) {
            case IGNITE -> target.igniteForSeconds(2 + lvl * 2);
            case LIGHTNING -> {
                if (level.getRandom().nextFloat() < 0.10F + 0.05F * lvl) {
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
                    if (bolt != null) {
                        bolt.snapTo(target.getX(), target.getY(), target.getZ());
                        bolt.setCause(player);
                        level.addFreshEntity(bolt);
                    }
                }
            }
            case LIFESTEAL -> player.heal(damage * 0.10F * lvl);
            case FROSTBITE -> {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60 + 20 * lvl, Math.min(lvl, 4)));
                target.setTicksFrozen(target.getTicksFrozen() + 40 * lvl);
            }
            case VENOM -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 100 + 20 * lvl, Math.max(lvl - 1, 0)));
            case DECAY -> target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60 + 20 * lvl, Math.max(lvl - 1, 0)));
            case WEAKEN -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120 + 40 * lvl, Math.max(lvl - 1, 0)));
            case SMASH -> target.knockback(0.5 * lvl, player.getX() - target.getX(), player.getZ() - target.getZ());
            case SHOCKWAVE -> {
                DamageSource source = level.damageSources().playerAttack(player);
                List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.0),
                        e -> e != target && e != player && e.isAlive());
                for (LivingEntity other : nearby) {
                    other.hurtServer(level, source, damage * 0.30F * lvl);
                }
            }
            default -> {
            }
        }
    }

    /** The AI design of the hero weapon the player just hit with, or null if this wasn't a hero melee hit. */
    private static HeroDesign attackerWeapon(DamageSource source) {
        if (!(source.getEntity() instanceof Player player) || source.getDirectEntity() != player) {
            return null;
        }
        HeroDesign design = HeroItems.designOf(player.getMainHandItem());
        return design == null || design.gear() == HeroGear.SUIT ? null : design;
    }

    // ------------------------------------------------------------ progress

    /** Defeating a non-hostile mob counts toward the next suit. */
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        LivingEntity dead = event.getEntity();
        if (!(dead instanceof Mob) || dead instanceof Enemy || dead instanceof Player) {
            return;
        }
        if (dead instanceof TamableAnimal pet && pet.isOwnedBy(player)) {
            return;
        }
        HeroProgress progress = HeroProgress.of(player).withKill();
        HeroProgress.save(player, progress);
        player.displayClientMessage(Component.literal(
                "Hero progress: " + Math.min(progress.kills(), progress.needed()) + " / " + progress.needed()
                        + " toward Mk " + (progress.tier() + 1)), true);
        if (progress.kills() == progress.needed()) {
            player.sendSystemMessage(Component.literal(
                    "The AI has designed Mk " + (progress.tier() + 1) + " gear! Visit an AI Crafting Table to upgrade."));
        }
    }

    private HeroEvents() {
    }
}

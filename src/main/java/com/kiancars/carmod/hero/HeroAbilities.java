package com.kiancars.carmod.hero;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Makes the active superpowers work: the suit's ability key, right-clicking
 * with hero tools, and always-on flight.
 */
public final class HeroAbilities {

    /** Per player: when each ability is ready again (game time, in ticks). */
    private static final Map<UUID, Map<HeroAbility, Long>> READY = new HashMap<>();
    /** Per player: which ability is selected on the suit (index 0) and on a held tool (index 1). */
    private static final Map<UUID, int[]> SELECTED = new HashMap<>();
    /** Players we gave flight to, so we can take it away again (and only then). */
    private static final Set<UUID> FLYERS = new HashSet<>();

    // ------------------------------------------------------------ entry points

    /** The ability key was pressed: use the selected suit ability. */
    public static void useSuitAbility(ServerPlayer player) {
        ItemStack suit = player.getItemBySlot(EquipmentSlot.CHEST);
        HeroDesign design = HeroItems.designOf(suit);
        if (design == null || design.gear() != HeroGear.SUIT) {
            player.displayClientMessage(Component.literal("Wear a Hero Suit to use its abilities."), true);
            return;
        }
        List<HeroDesign.AbilityLevel> list = design.activeAbilities();
        if (list.isEmpty()) {
            player.displayClientMessage(Component.literal("This suit has no abilities you trigger (flight is always on)."), true);
            return;
        }
        activate(player, list.get(selected(player, 0) % list.size()));
    }

    /** The cycle key: switch the selected ability of the held hero tool, or of the suit. */
    public static void cycle(ServerPlayer player) {
        HeroDesign held = HeroItems.designOf(player.getMainHandItem());
        boolean tool = held != null && held.gear() != HeroGear.SUIT;
        HeroDesign design = tool ? held : HeroItems.designOf(player.getItemBySlot(EquipmentSlot.CHEST));
        if (design == null) {
            return;
        }
        List<HeroDesign.AbilityLevel> list = design.activeAbilities();
        if (list.isEmpty()) {
            return;
        }
        int slot = tool ? 1 : 0;
        int next = (selected(player, slot) + 1) % list.size();
        SELECTED.computeIfAbsent(player.getUUID(), id -> new int[2])[slot] = next;
        player.displayClientMessage(Component.literal("Selected: " + list.get(next).ability().label()), true);
    }

    /** Right-clicking with a hero tool or blade uses its selected ability. */
    public static void useHeldAbility(ServerPlayer player, ItemStack stack) {
        HeroDesign design = HeroItems.designOf(stack);
        if (design == null || design.gear() == HeroGear.SUIT) {
            return;
        }
        List<HeroDesign.AbilityLevel> list = design.activeAbilities();
        if (!list.isEmpty()) {
            activate(player, list.get(selected(player, 1) % list.size()));
        }
    }

    private static int selected(ServerPlayer player, int slot) {
        return SELECTED.computeIfAbsent(player.getUUID(), id -> new int[2])[slot];
    }

    // ------------------------------------------------------------------ flight

    /** Called regularly: gives or takes back flight depending on the worn suit. */
    public static void tickFlight(ServerPlayer player) {
        HeroDesign suit = HeroItems.designOf(player.getItemBySlot(EquipmentSlot.CHEST));
        int level = 0;
        if (suit != null && suit.gear() == HeroGear.SUIT) {
            for (HeroDesign.AbilityLevel a : suit.abilities()) {
                if (a.ability() == HeroAbility.FLIGHT) {
                    level = a.level();
                }
            }
        }
        var abilities = player.getAbilities();
        if (level > 0) {
            float speed = 0.05F * (1.0F + 0.2F * level);
            if (!abilities.mayfly || abilities.getFlyingSpeed() != speed) {
                abilities.mayfly = true;
                abilities.setFlyingSpeed(speed);
                player.onUpdateAbilities();
            }
            FLYERS.add(player.getUUID());
        } else if (FLYERS.remove(player.getUUID()) && !player.isCreative() && !player.isSpectator()) {
            abilities.mayfly = false;
            abilities.flying = false;
            abilities.setFlyingSpeed(0.05F);
            player.onUpdateAbilities();
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, true, false, false));
        }
    }

    public static void forget(ServerPlayer player) {
        READY.remove(player.getUUID());
        SELECTED.remove(player.getUUID());
        FLYERS.remove(player.getUUID());
    }

    // -------------------------------------------------------------- activation

    private static void activate(ServerPlayer player, HeroDesign.AbilityLevel use) {
        HeroAbility ability = use.ability();
        long now = player.level().getGameTime();
        Map<HeroAbility, Long> ready = READY.computeIfAbsent(player.getUUID(), id -> new HashMap<>());
        long readyAt = ready.getOrDefault(ability, 0L);
        if (now < readyAt) {
            player.displayClientMessage(Component.literal(
                    ability.label() + " is recharging (" + ((readyAt - now + 19) / 20) + "s)"), true);
            return;
        }
        if (!(player.level() instanceof ServerLevel level) || !perform(level, player, ability, use.level())) {
            return;
        }
        ready.put(ability, now + ability.cooldownTicks(use.level()));
    }

    private static boolean perform(ServerLevel level, ServerPlayer player, HeroAbility ability, int lvl) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        switch (ability) {
            case TELEPORT -> {
                return teleport(level, player, HeroAbility.range(lvl));
            }
            case PHASE -> {
                return phase(level, player, 4 + lvl * 2);
            }
            case WEB -> {
                return web(level, player, HeroAbility.range(lvl), lvl);
            }
            case DASH -> {
                push(player, look.x * (1.6 + 0.2 * lvl), 0.15, look.z * (1.6 + 0.2 * lvl));
                sound(level, player, SoundEvents.PHANTOM_FLAP);
                return true;
            }
            case SUPER_JUMP -> {
                push(player, look.x * 0.5, 1.1 + 0.15 * lvl, look.z * 0.5);
                sound(level, player, SoundEvents.RABBIT_JUMP);
                return true;
            }
            case SLAM -> {
                double radius = 3 + lvl;
                hurtAround(level, player, player.position(), radius, 4 + 2 * lvl, true);
                level.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.2, player.getZ(), 6, radius / 3, 0.1, radius / 3, 0.0);
                sound(level, player, SoundEvents.GENERIC_EXPLODE.value());
                return true;
            }
            case LIGHTNING_STRIKE -> {
                Vec3 target = lookTarget(player, 60);
                for (int i = 0; i < 1 + lvl / 3; i++) {
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
                    if (bolt != null) {
                        bolt.snapTo(target.x + (i == 0 ? 0 : level.getRandom().nextInt(7) - 3), target.y,
                                target.z + (i == 0 ? 0 : level.getRandom().nextInt(7) - 3));
                        bolt.setCause(player);
                        level.addFreshEntity(bolt);
                    }
                }
                return true;
            }
            case FIREBALL -> {
                int count = Math.min(1 + lvl / 2, 5);
                for (int i = 0; i < count; i++) {
                    Vec3 dir = look.add((level.getRandom().nextDouble() - 0.5) * 0.1 * (count - 1),
                            (level.getRandom().nextDouble() - 0.5) * 0.05 * (count - 1), (level.getRandom().nextDouble() - 0.5) * 0.1 * (count - 1));
                    SmallFireball ball = new SmallFireball(level, player, dir.scale(0.6));
                    ball.setPos(eye.x + look.x, eye.y - 0.2 + look.y, eye.z + look.z);
                    level.addFreshEntity(ball);
                }
                sound(level, player, SoundEvents.BLAZE_SHOOT);
                return true;
            }
            case FREEZE_BLAST -> {
                double radius = 5 + lvl;
                for (LivingEntity e : around(level, player, player.position(), radius)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100 + 20 * lvl, 3));
                    e.setTicksFrozen(300);
                    e.hurtServer(level, level.damageSources().freeze(), 2 + lvl);
                }
                level.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 1, player.getZ(), 80, radius / 2, 0.8, radius / 2, 0.02);
                sound(level, player, SoundEvents.PLAYER_HURT_FREEZE);
                return true;
            }
            case FORCE_PUSH -> {
                for (LivingEntity e : around(level, player, eye.add(look.scale(5)), 7)) {
                    Vec3 away = e.position().subtract(player.position()).normalize();
                    if (away.dot(look) > 0.2) {
                        push(e, away.x * (1.5 + 0.3 * lvl), 0.5, away.z * (1.5 + 0.3 * lvl));
                    }
                }
                level.sendParticles(ParticleTypes.CLOUD, eye.x + look.x * 2, eye.y + look.y * 2, eye.z + look.z * 2, 25, 0.6, 0.6, 0.6, 0.05);
                sound(level, player, SoundEvents.WIND_CHARGE_BURST.value());
                return true;
            }
            case GRAVITY_PULL -> {
                for (LivingEntity e : around(level, player, player.position(), 8 + lvl * 2)) {
                    Vec3 to = player.position().subtract(e.position()).normalize();
                    push(e, to.x * 1.2, 0.3, to.z * 1.2);
                }
                level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(), 60, 1.0, 1.0, 1.0, 0.5);
                sound(level, player, SoundEvents.ENDERMAN_TELEPORT);
                return true;
            }
            case CLOAK -> {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, (10 + 2 * lvl) * 20, 0, true, false, true));
                sound(level, player, SoundEvents.ILLUSIONER_MIRROR_MOVE);
                return true;
            }
            case TIME_FREEZE -> {
                for (LivingEntity e : around(level, player, player.position(), 8 + lvl)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, (3 + lvl) * 20, 6));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, (3 + lvl) * 20, 3));
                }
                level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1, player.getZ(), 50, 3.0, 1.0, 3.0, 0.01);
                sound(level, player, SoundEvents.BELL_RESONATE);
                return true;
            }
            case HEAL_BURST -> {
                player.heal(6 + 2 * lvl);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
                level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.5, player.getZ(), 10, 0.5, 0.5, 0.5, 0.0);
                sound(level, player, SoundEvents.PLAYER_LEVELUP);
                return true;
            }
            case DRAIN -> {
                float stolen = 0;
                for (LivingEntity e : around(level, player, player.position(), 4 + lvl)) {
                    float amount = 2 + lvl;
                    e.hurtServer(level, level.damageSources().playerAttack(player), amount);
                    stolen += amount * 0.5F;
                }
                player.heal(stolen);
                level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY() + 1, player.getZ(), 15, 1.0, 0.5, 1.0, 0.1);
                sound(level, player, SoundEvents.WITCH_DRINK);
                return true;
            }
            case EXPLODE -> {
                Vec3 target = lookTarget(player, 40);
                level.explode(player, target.x, target.y, target.z, 2.0F + 0.5F * lvl, Level.ExplosionInteraction.NONE);
                return true;
            }
            case SHIELD -> {
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, (5 + lvl) * 20, 3));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, (5 + lvl) * 20, 3));
                level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1, player.getZ(), 30, 0.6, 0.9, 0.6, 0.02);
                sound(level, player, SoundEvents.BEACON_ACTIVATE);
                return true;
            }
            case SENSE -> {
                for (LivingEntity e : around(level, player, player.position(), 20 + lvl * 4)) {
                    e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                }
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200, 0, true, false, false));
                sound(level, player, SoundEvents.AMETHYST_BLOCK_RESONATE);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    // ----------------------------------------------------------------- helpers

    private static void sound(ServerLevel level, Player player, net.minecraft.sounds.SoundEvent sound) {
        level.playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void push(net.minecraft.world.entity.Entity entity, double x, double y, double z) {
        entity.setDeltaMovement(x, y, z);
        entity.hurtMarked = true;
        entity.fallDistance = 0;
    }

    private static List<LivingEntity> around(ServerLevel level, Player player, Vec3 center, double radius) {
        AABB box = new AABB(center, center).inflate(radius);
        return level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && e.isAlive() && e.position().distanceToSqr(center) <= radius * radius);
    }

    private static void hurtAround(ServerLevel level, ServerPlayer player, Vec3 center, double radius, float damage, boolean knock) {
        DamageSource source = level.damageSources().playerAttack(player);
        for (LivingEntity e : around(level, player, center, radius)) {
            e.hurtServer(level, source, damage);
            if (knock) {
                Vec3 away = e.position().subtract(center).normalize();
                push(e, away.x * 0.8, 0.5, away.z * 0.8);
            }
        }
    }

    /** Where the player is looking: the first block hit, or the end of the reach. */
    private static HitResult trace(Player player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(range));
        return player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
    }

    private static Vec3 lookTarget(Player player, double range) {
        return trace(player, range).getLocation();
    }

    private static boolean teleport(ServerLevel level, ServerPlayer player, int range) {
        HitResult hit = trace(player, range);
        Vec3 target = hit.getLocation();
        if (hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK) {
            target = target.add(block.getDirection().getStepX() * 0.6, block.getDirection().getStepY() * 0.6 - (block.getDirection().getStepY() > 0 ? 0 : 1.0),
                    block.getDirection().getStepZ() * 0.6);
        } else {
            target = target.subtract(0, 1.0, 0);
        }
        Vec3 spot = findFree(level, player, target);
        if (spot == null) {
            player.displayClientMessage(Component.literal("No room to teleport there."), true);
            return false;
        }
        level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(), 40, 0.4, 0.8, 0.4, 0.4);
        player.teleportTo(spot.x, spot.y, spot.z);
        player.fallDistance = 0;
        level.sendParticles(ParticleTypes.PORTAL, spot.x, spot.y + 1, spot.z, 40, 0.4, 0.8, 0.4, 0.4);
        level.playSound(null, BlockPos.containing(spot), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    /** Finds a spot the player fits in at or just above the wanted position. */
    private static Vec3 findFree(ServerLevel level, Player player, Vec3 want) {
        AABB box = player.getBoundingBox();
        for (double dy = 0; dy <= 3; dy += 0.5) {
            Vec3 p = want.add(0, dy, 0);
            AABB moved = box.move(p.x - player.getX(), p.y - player.getY(), p.z - player.getZ());
            if (level.noCollision(player, moved)) {
                return p;
            }
        }
        return null;
    }

    /** Slips through walls: goes forward to the first open space beyond whatever is in the way. */
    private static boolean phase(ServerLevel level, ServerPlayer player, int maxDistance) {
        Vec3 dir = player.getLookAngle();
        Vec3 flat = new Vec3(dir.x, 0, dir.z);
        if (flat.lengthSqr() < 1.0E-4) {
            flat = new Vec3(0, 0, 1);
        }
        flat = flat.normalize();
        AABB box = player.getBoundingBox();
        boolean wasBlocked = false;
        for (double d = 0.5; d <= maxDistance; d += 0.5) {
            AABB moved = box.move(flat.x * d, 0, flat.z * d);
            if (!level.noCollision(player, moved)) {
                wasBlocked = true;
            } else if (wasBlocked) {
                level.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1, player.getZ(), 40, 0.4, 0.8, 0.4, 0.05);
                player.teleportTo(player.getX() + flat.x * d, player.getY(), player.getZ() + flat.z * d);
                level.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1, player.getZ(), 40, 0.4, 0.8, 0.4, 0.05);
                level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 0.6F);
                return true;
            }
        }
        player.displayClientMessage(Component.literal(wasBlocked ? "The wall is too thick." : "There is no wall to slip through."), true);
        return false;
    }

    /** Shoots a web and pulls the player toward the spot they are looking at. */
    private static boolean web(ServerLevel level, ServerPlayer player, int range, int lvl) {
        HitResult hit = trace(player, range);
        if (hit.getType() != HitResult.Type.BLOCK) {
            player.displayClientMessage(Component.literal("Nothing in range to stick a web to."), true);
            return false;
        }
        Vec3 pull = hit.getLocation().subtract(player.getEyePosition());
        double dist = pull.length();
        Vec3 dir = pull.normalize();
        double power = Math.min(0.6 + 0.12 * lvl + dist * 0.03, 2.6);
        push(player, dir.x * power, dir.y * power + 0.35, dir.z * power);
        Vec3 eye = player.getEyePosition();
        for (int i = 1; i < 10; i++) {
            Vec3 p = eye.add(pull.scale(i / 10.0));
            level.sendParticles(ParticleTypes.CLOUD, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.SPIDER_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.6F);
        return true;
    }

    private HeroAbilities() {
    }
}

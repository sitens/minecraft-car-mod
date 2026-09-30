package com.kiancars.carmod.entity;

import com.kiancars.carmod.registry.ModTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * hOesaC Final Boss: a 7x7x7 round blob with 100 hearts and a purple boss
 * bar. He doesn't path-find (he's far too big); a tiny state machine steers
 * him straight at his target instead:
 * <ul>
 *   <li><b>Chase</b> - waddle toward the player.</li>
 *   <li><b>Roll</b> - charge in a line; touching a player hurts 2 hearts
 *       through armor and effects.</li>
 *   <li><b>Slam</b> - jump up and land on the player; anyone under him takes
 *       heavy suffocation damage.</li>
 * </ul>
 * Tacks stop a roll: touching one makes him bounce away (and costs him
 * 2 hearts), then he tries again. See {@link #onTackHit}.
 */
public class HoesacBossEntity extends Monster {

    private static final float MAX_HEALTH = 200.0F;     // 100 hearts
    private static final float ROLL_DAMAGE = 4.0F;      // 2 hearts
    private static final float SLAM_DAMAGE = 14.0F;     // 7 hearts
    private static final float ROLL_MAX_SPEED = 0.85F;
    private static final float CHASE_MAX_SPEED = 0.22F;
    private static final int PLAYER_HIT_COOLDOWN = 20;
    private static final String LAST_ROLL_HIT_KEY = "carmod_last_roll_hit";

    private enum State { CHASE, ROLL, SLAM }

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)
            .setDarkenScreen(true);

    private State state = State.CHASE;
    private int stateTicks;
    private int cooldown = 60;
    private int bounceTicks;
    private boolean slamLaunched;
    private Vec3 rollDirection = Vec3.ZERO;

    /** How far the ball has rolled, in radians. Only used for drawing. */
    public float rollAngle;
    public float rollAngleO;

    public HoesacBossEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 500;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.STEP_HEIGHT, 2.0)
                .add(Attributes.ATTACK_DAMAGE, ROLL_DAMAGE);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ------------------------------------------------------------ physics

    @Override
    public void travel(Vec3 input) {
        // Gravity has to be added to the stored motion itself (not just to this
        // tick's step), or he floats down at a constant crawl instead of falling.
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, -this.getGravity(), 0.0));
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 after = this.getDeltaMovement();
        double drag = this.onGround() ? 0.88 : 0.985;
        this.setDeltaMovement(after.x * drag, after.y * 0.98, after.z * drag);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isInWall() {
        return false;
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    // ---------------------------------------------------------------- brain

    @Override
    public void tick() {
        this.rollAngleO = this.rollAngle;
        super.tick();
        double moved = Math.hypot(this.getX() - this.xo, this.getZ() - this.zo);
        this.rollAngle += (float) (moved / 3.5);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if (this.bounceTicks > 0) {
            this.bounceTicks--;
            return;
        }

        if (this.getTarget() instanceof Player target && !target.isAlive()) {
            this.setTarget(null);
        }
        if (this.getTarget() == null) {
            this.state = State.CHASE;
            return;
        }

        LivingTargetInfo info = LivingTargetInfo.of(this, this.getTarget());
        this.faceDirection(info.direction);

        switch (this.state) {
            case CHASE -> tickChase(info);
            case ROLL -> tickRoll(info);
            case SLAM -> tickSlam(level, info);
        }
    }

    private void tickChase(LivingTargetInfo info) {
        this.accelerate(info.direction, 0.04, CHASE_MAX_SPEED);
        if (this.cooldown > 0) {
            this.cooldown--;
            return;
        }
        if (!this.onGround()) {
            return;
        }
        boolean far = info.distance > 28.0;
        if (far || this.random.nextFloat() < 0.45F) {
            this.startRoll(info);
        } else {
            this.startSlam();
        }
    }

    private void startRoll(LivingTargetInfo info) {
        this.state = State.ROLL;
        this.stateTicks = 0;
        this.rollDirection = info.direction;
        this.level().playSound(null, this.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 4.0F, 0.6F);
    }

    private void tickRoll(LivingTargetInfo info) {
        this.stateTicks++;
        // Mostly locked onto where the player was, with a little steering.
        Vec3 steered = this.rollDirection.scale(0.92).add(info.direction.scale(0.08));
        this.rollDirection = steered.lengthSqr() > 1.0E-6 ? steered.normalize() : info.direction;
        this.accelerate(this.rollDirection, 0.14, ROLL_MAX_SPEED);
        if (this.stateTicks >= 50) {
            this.endAttack(50);
        }
    }

    private void startSlam() {
        this.state = State.SLAM;
        this.stateTicks = 0;
        this.slamLaunched = false;
    }

    private void tickSlam(ServerLevel level, LivingTargetInfo info) {
        this.stateTicks++;
        if (!this.slamLaunched) {
            // The jump lasts about 30 ticks, and horizontal drag makes the drift cover ~24x the launch speed.
            double speed = Math.min(info.distance / 24.0, 1.2);
            this.setDeltaMovement(info.direction.x * speed, 1.35, info.direction.z * speed);
            this.hurtMarked = true;
            this.slamLaunched = true;
            this.level().playSound(null, this.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 4.0F, 0.4F);
            return;
        }
        if (this.stateTicks > 8 && (this.onGround() || this.stateTicks > 90)) {
            this.land(level);
            this.endAttack(70);
        }
    }

    private void land(ServerLevel level) {
        level.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 6.0F, 0.5F);
        level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.5, this.getZ(), 120, 6.0, 0.5, 6.0, 0.1);
        AABB footprint = this.getBoundingBox().inflate(2.0, 0.0, 2.0).setMinY(this.getY() - 1.0).setMaxY(this.getY() + 4.0);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, footprint)) {
            if (player.isSpectator()) {
                continue;
            }
            DamageSource source = level.damageSources().source(ModTags.HOESAC_SUFFOCATE, this);
            player.hurtServer(level, source, SLAM_DAMAGE);
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3));
        }
    }

    private void endAttack(int nextCooldown) {
        this.state = State.CHASE;
        this.cooldown = nextCooldown;
        this.stateTicks = 0;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel level && this.state == State.ROLL && this.bounceTicks == 0) {
            this.flattenPlayers(level);
        }
    }

    private void flattenPlayers(ServerLevel level) {
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(0.5))) {
            if (player.isSpectator() || player.isCreative()) {
                continue;
            }
            long now = level.getGameTime();
            if (now - player.getPersistentData().getLongOr(LAST_ROLL_HIT_KEY, Long.MIN_VALUE / 2) < PLAYER_HIT_COOLDOWN) {
                continue;
            }
            player.getPersistentData().putLong(LAST_ROLL_HIT_KEY, now);
            player.hurtServer(level, level.damageSources().source(ModTags.HOESAC_ROLL, this), ROLL_DAMAGE);
        }
    }

    /** A roll (or anything else) ran into a tack: bounce away and try again. */
    public void onTackHit(Vec3 tackCenter) {
        Vec3 away = new Vec3(this.getX() - tackCenter.x, 0.0, this.getZ() - tackCenter.z);
        away = away.lengthSqr() > 1.0E-6 ? away.normalize() : new Vec3(1.0, 0.0, 0.0);
        this.setDeltaMovement(away.x * 0.55, 0.25, away.z * 0.55);
        this.hurtMarked = true;
        this.bounceTicks = 25;
        this.state = State.CHASE;
        this.stateTicks = 0;
        this.cooldown = 40;
        if (this.level() instanceof ServerLevel level) {
            level.playSound(null, this.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 2.0F, 0.6F);
        }
    }

    private void accelerate(Vec3 direction, double push, double maxSpeed) {
        Vec3 velocity = this.getDeltaMovement().add(direction.x * push, 0.0, direction.z * push);
        double horizontal = Math.hypot(velocity.x, velocity.z);
        if (horizontal > maxSpeed) {
            velocity = new Vec3(velocity.x / horizontal * maxSpeed, velocity.y, velocity.z / horizontal * maxSpeed);
        }
        this.setDeltaMovement(velocity);
    }

    private void faceDirection(Vec3 direction) {
        float yaw = (float) (Math.atan2(-direction.x, direction.z) * (180.0 / Math.PI));
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
    }

    /** Flat (ignoring height) direction and distance from the boss to a target. */
    private record LivingTargetInfo(Vec3 direction, double distance) {
        static LivingTargetInfo of(HoesacBossEntity boss, net.minecraft.world.entity.LivingEntity target) {
            Vec3 flat = new Vec3(target.getX() - boss.getX(), 0.0, target.getZ() - boss.getZ());
            double distance = flat.length();
            Vec3 direction = distance > 1.0E-6 ? flat.scale(1.0 / distance) : new Vec3(0.0, 0.0, 1.0);
            return new LivingTargetInfo(direction, distance);
        }
    }

    // ------------------------------------------------------------ boss bar

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }
}

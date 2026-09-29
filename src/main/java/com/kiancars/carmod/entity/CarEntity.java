package com.kiancars.carmod.entity;

import com.kiancars.carmod.registry.ModItems;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * A single entity class shared by all 10 car variants; the variant is stored
 * as synced entity data. Movement comes from {@link AbstractBoat} (with land/water speed swapped, see {@link #tick()})
 * ("boat-style movement" per project decision) — forward/back accelerate,
 * left/right turn, no bespoke physics.
 * <p>
 * Confirmed against the real 1.21.11 NeoForge decompiled/patched sources
 * (net.minecraft.world.entity.vehicle.boat.AbstractBoat): the package moved
 * under {@code .boat}, and the drop item is now supplied once via the
 * constructor as a {@code Supplier<Item>} rather than an overridable method —
 * {@code AbstractBoat.getDropItem()} is {@code final}.
 * <p>
 * That supplier is fixed at construction time and genuinely can't read
 * {@code this.getCarType()} — javac rejects any reference to {@code this}
 * before the superclass constructor returns, including from inside a
 * lambda body (an earlier attempt at this assumed lambdas were exempt;
 * they're not, at least not for capturing the enclosing instance here).
 * So for now the drop/pick-block item is a fixed placeholder (SEDAN)
 * regardless of the car's actual variant — a known limitation, tracked in
 * tasks.md, separate from placement (which is already variant-correct via
 * {@link com.kiancars.carmod.item.CarItem}, one item per variant).
 */
public class CarEntity extends AbstractBoat {

    private static final EntityDataAccessor<Integer> DATA_CAR_TYPE =
            SynchedEntityData.defineId(CarEntity.class, EntityDataSerializers.INT);

    public CarEntity(EntityType<? extends CarEntity> entityType, Level level) {
        super(entityType, level, () -> ModItems.byCarType(CarType.SEDAN).get());
    }

    public CarEntity(Level level, CarType type) {
        this(com.kiancars.carmod.registry.ModEntities.CAR.get(), level);
        setCarType(type);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CAR_TYPE, CarType.SEDAN.getNetworkId());
    }

    public CarType getCarType() {
        return CarType.byNetworkId(this.entityData.get(DATA_CAR_TYPE));
    }

    public void setCarType(CarType type) {
        this.entityData.set(DATA_CAR_TYPE, type.getNetworkId());
    }

    /**
     * The boat code this is built on is fast on water and slow on land; a car
     * should be the other way around. Each tick the boat code multiplies the
     * speed by a "friction" number (about 0.9 on water, about 0.6 on most
     * ground) before adding the driver's push. Scaling the speed first makes
     * the effective friction whatever we want: high on land (fast), low on
     * water (sluggish).
     */
    private static final float LAND_FRICTION = 0.9F;
    private static final float WATER_FRICTION = 0.5F;
    private static final float BOAT_WATER_FRICTION = 0.9F;

    @Override
    public void tick() {
        if (this.getControllingPassenger() instanceof Player && this.isLocalInstanceAuthoritative()) {
            double scale = 1.0;
            if (this.isInWater()) {
                scale = WATER_FRICTION / BOAT_WATER_FRICTION;
            } else {
                float ground = this.getGroundFriction();
                if (!Float.isNaN(ground) && ground > 0.0F && ground < LAND_FRICTION) {
                    scale = LAND_FRICTION / ground;
                }
            }
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x * scale, motion.y, motion.z * scale);
        }
        super.tick();
    }

    /** Lets cars drive up one-block bumps instead of getting stuck on them. */
    @Override
    public float maxUpStep() {
        return 1.0F;
    }

    @Override
    protected double rideHeight(EntityDimensions dimensions) {
        return dimensions.height() / 3.0F;
    }
}

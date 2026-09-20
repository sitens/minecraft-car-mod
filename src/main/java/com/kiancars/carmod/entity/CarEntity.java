package com.kiancars.carmod.entity;

import com.kiancars.carmod.registry.ModItems;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * A single entity class shared by all 10 car variants; the variant is stored
 * as synced entity data. Movement is inherited as-is from {@link AbstractBoat}
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

    @Override
    protected double rideHeight(EntityDimensions dimensions) {
        return dimensions.height() / 3.0F;
    }
}

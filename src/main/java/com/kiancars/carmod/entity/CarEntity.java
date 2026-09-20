package com.kiancars.carmod.entity;

import com.kiancars.carmod.registry.ModEntities;
import com.kiancars.carmod.registry.ModItems;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * A single entity class shared by all 10 car variants; the variant is stored
 * as synced entity data (mirrors how vanilla {@code Boat} handles boat types).
 * <p>
 * Movement is inherited as-is from {@link AbstractBoat} ("boat-style
 * movement" per project decision) — forward/back accelerate, left/right turn,
 * no bespoke physics. Only the variant field and item/model plumbing are
 * added here.
 */
public class CarEntity extends AbstractBoat {

    private static final EntityDataAccessor<Integer> DATA_CAR_TYPE =
            SynchedEntityData.defineId(CarEntity.class, EntityDataSerializers.INT);

    public CarEntity(EntityType<? extends CarEntity> entityType, Level level) {
        super(entityType, level);
    }

    public CarEntity(Level level, CarType type) {
        this(ModEntities.CAR.get(), level);
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

    public Item getCarItem() {
        return ModItems.byCarType(getCarType()).get();
    }

    // NOTE — verify against the actual 1.21.11 mappings when Gradle first
    // syncs: AbstractBoat declares an abstract drop-item accessor (its exact
    // name/signature has moved between "getDropItem()" and a variant-aware
    // overload across MC versions). Wire whichever one 1.21.11 requires to
    // call getCarItem() above. Left unimplemented on purpose rather than
    // guessing a signature and having it silently do the wrong thing.
}

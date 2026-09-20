package com.kiancars.carmod.entity;

/**
 * The 10 car variants, ordered roughly by crafting difficulty (easiest first).
 * Each entry's {@link #id} is used for the item registry name ({@code car_<id>})
 * and the recipe file name ({@code data/carmod/recipe/car_<id>.json}).
 */
public enum CarType {
    SEDAN(0, "sedan"),
    PICKUP(1, "pickup"),
    OFFROADER(2, "offroader"),
    SPORTS_CAR(3, "sports_car"),
    LIMOUSINE(4, "limousine"),
    RACER(5, "racer"),
    MONSTER_TRUCK(6, "monster_truck"),
    ARMORED_CAR(7, "armored_car"),
    HOVER_PROTOTYPE(8, "hover_prototype"),
    GOLDEN_LUXURY(9, "golden_luxury");

    private final int networkId;
    private final String id;

    CarType(int networkId, String id) {
        this.networkId = networkId;
        this.id = id;
    }

    public int getNetworkId() {
        return networkId;
    }

    public String getId() {
        return id;
    }

    public static CarType byNetworkId(int networkId) {
        for (CarType type : values()) {
            if (type.networkId == networkId) {
                return type;
            }
        }
        return SEDAN;
    }
}

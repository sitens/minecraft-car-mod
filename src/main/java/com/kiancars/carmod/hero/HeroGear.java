package com.kiancars.carmod.hero;

/** The five pieces of hero gear the AI designs for every tier. */
public enum HeroGear {
    SUIT("Suit"),
    BLADE("Blade"),
    AXE("Axe"),
    PICKAXE("Pickaxe"),
    SHOVEL("Shovel");

    public static final HeroGear[] ALL = values();

    private final String label;

    HeroGear(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean isTool() {
        return this == AXE || this == PICKAXE || this == SHOVEL;
    }

    public static HeroGear byIndex(int index) {
        return index >= 0 && index < ALL.length ? ALL[index] : null;
    }
}

package com.kiancars.carmod.hero;

/**
 * Active superpowers: things you do on purpose. The suit's abilities are
 * triggered with the ability key (default G); a hero tool or blade uses its
 * ability when you right-click with it.
 */
public enum HeroAbility {
    TELEPORT("Teleport", 60),
    PHASE("Atom Dissipation", 100),
    FLIGHT("Flight", 0),
    WEB("Web Sling", 20),
    DASH("Dash", 40),
    SUPER_JUMP("Super Jump", 30),
    SLAM("Ground Slam", 80),
    LIGHTNING_STRIKE("Thunder Call", 100),
    FIREBALL("Fireball", 20),
    FREEZE_BLAST("Freeze Blast", 120),
    FORCE_PUSH("Force Push", 60),
    GRAVITY_PULL("Gravity Pull", 80),
    CLOAK("Cloak", 400),
    TIME_FREEZE("Time Freeze", 400),
    HEAL_BURST("Heal Burst", 300),
    DRAIN("Life Drain", 140),
    EXPLODE("Smash Blast", 120),
    SHIELD("Energy Shield", 400),
    SENSE("Super Sense", 300);

    private final String label;
    private final int cooldown;

    HeroAbility(String label, int cooldown) {
        this.label = label;
        this.cooldown = cooldown;
    }

    public String label() {
        return label;
    }

    /** Flight is always on while the suit is worn; every other ability is used on purpose. */
    public boolean isActive() {
        return this != FLIGHT;
    }

    /** Only a suit can fly. */
    public boolean suitOnly() {
        return this == FLIGHT;
    }

    public int cooldownTicks(int level) {
        return (int) Math.max(10, cooldown * (1.0 - 0.06 * Math.min(level - 1, 8)));
    }

    public static int range(int level) {
        return 20 + level * 8;
    }

    public String describe(int level) {
        String name = label + " " + HeroPower.roman(level);
        return switch (this) {
            case TELEPORT -> name + " (look somewhere and blink up to " + range(level) + " blocks)";
            case PHASE -> name + " (dissolve your atoms and slip through walls, up to " + (4 + level * 2) + " blocks)";
            case FLIGHT -> name + " (fly like a bird while worn)";
            case WEB -> name + " (shoot a web and swing to where you look, " + range(level) + " blocks)";
            case DASH -> name + " (burst forward very fast)";
            case SUPER_JUMP -> name + " (jump super high)";
            case SLAM -> name + " (smash the ground, hurting everything within " + (3 + level) + " blocks)";
            case LIGHTNING_STRIKE -> name + " (call " + (1 + level / 3) + " lightning bolt(s) where you look)";
            case FIREBALL -> name + " (shoot " + Math.min(1 + level / 2, 5) + " fireball(s))";
            case FREEZE_BLAST -> name + " (freeze everything within " + (5 + level) + " blocks)";
            case FORCE_PUSH -> name + " (blast everything ahead of you away)";
            case GRAVITY_PULL -> name + " (pull everything within " + (8 + level * 2) + " blocks to you)";
            case CLOAK -> name + " (turn invisible for " + (10 + 2 * level) + "s)";
            case TIME_FREEZE -> name + " (freeze time for creatures within " + (8 + level) + " blocks for " + (3 + level) + "s)";
            case HEAL_BURST -> name + " (heal " + (3 + level) + " hearts)";
            case DRAIN -> name + " (steal life from everything within " + (4 + level) + " blocks)";
            case EXPLODE -> name + " (blow up where you look, without breaking blocks)";
            case SHIELD -> name + " (take much less damage for " + (5 + level) + "s)";
            case SENSE -> name + " (see every creature within " + (20 + level * 4) + " blocks glow)";
        };
    }
}

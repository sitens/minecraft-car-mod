package com.kiancars.carmod.hero;

import java.util.List;

/**
 * Every superpower the AI can hand out. Suit powers and tool "held" powers
 * are potion effects kept topped up while the gear is worn or held; weapon
 * powers fire when you hit something.
 */
public enum HeroPower {

    // ---- passive powers: a potion effect while worn (suit) or held (tools)
    SWIFT("Swift", Kind.PASSIVE, "speed", 5),
    LEAP("Leap", Kind.PASSIVE, "jump_boost", 5),
    IRON_SKIN("Iron Skin", Kind.PASSIVE, "resistance", 3),
    REGROWTH("Regrowth", Kind.PASSIVE, "regeneration", 3),
    TITAN_STRENGTH("Titan Strength", Kind.PASSIVE, "strength", 5),
    QUICK_HANDS("Quick Hands", Kind.PASSIVE, "haste", 5),
    GUARDIAN_SHIELD("Guardian Shield", Kind.PASSIVE, "absorption", 5),
    VITALITY("Vitality", Kind.PASSIVE, "health_boost", 5),
    DOLPHIN_GRACE("Dolphin Grace", Kind.PASSIVE, "dolphins_grace", 3),
    LUCKY("Lucky", Kind.PASSIVE, "luck", 5),
    NIGHT_EYES("Night Eyes", Kind.PASSIVE, "night_vision", 1),
    FIREPROOF("Fireproof", Kind.PASSIVE, "fire_resistance", 1),
    GILLS("Gills", Kind.PASSIVE, "water_breathing", 1),
    FEATHER_FALL("Feather Fall", Kind.PASSIVE, "slow_falling", 1),

    // ---- weapon powers: fire when you hit something
    IGNITE("Ignite", Kind.ON_HIT, null, 5),
    LIGHTNING("Lightning Strike", Kind.ON_HIT, null, 5),
    LIFESTEAL("Lifesteal", Kind.ON_HIT, null, 5),
    FROSTBITE("Frostbite", Kind.ON_HIT, null, 5),
    VENOM("Venom", Kind.ON_HIT, null, 5),
    DECAY("Decay", Kind.ON_HIT, null, 3),
    WEAKEN("Weaken", Kind.ON_HIT, null, 3),
    SMASH("Smash", Kind.ON_HIT, null, 5),
    EXECUTE("Execute", Kind.ON_HIT, null, 5),
    SHOCKWAVE("Shockwave", Kind.ON_HIT, null, 5);

    public enum Kind { PASSIVE, ON_HIT }

    public static final List<HeroPower> PASSIVES = List.of(values()).stream().filter(p -> p.kind == Kind.PASSIVE).toList();
    public static final List<HeroPower> ON_HIT = List.of(values()).stream().filter(p -> p.kind == Kind.ON_HIT).toList();

    private final String label;
    private final Kind kind;
    private final String effectId;
    private final int maxLevel;

    HeroPower(String label, Kind kind, String effectId, int maxLevel) {
        this.label = label;
        this.kind = kind;
        this.effectId = effectId;
        this.maxLevel = maxLevel;
    }

    public String label() {
        return label;
    }

    public Kind kind() {
        return kind;
    }

    /** The vanilla potion effect behind a passive power (a name like "speed"), or null for weapon powers. */
    public String effectId() {
        return effectId;
    }

    public int maxLevel() {
        return maxLevel;
    }

    /** A short description with the strength spelled out, e.g. "Lightning Strike V (35% chance)". */
    public String describe(int level) {
        String name = maxLevel == 1 ? label : label + " " + roman(level);
        return switch (this) {
            case IGNITE -> name + " (sets targets on fire for " + (2 + level * 2) + "s)";
            case LIGHTNING -> name + " (" + (10 + level * 5) + "% chance)";
            case LIFESTEAL -> name + " (heals " + (level * 10) + "% of damage)";
            case FROSTBITE -> name + " (slows targets)";
            case VENOM -> name + " (poisons targets)";
            case DECAY -> name + " (withers targets)";
            case WEAKEN -> name + " (weakens targets)";
            case SMASH -> name + " (heavy knockback)";
            case EXECUTE -> name + " (+" + (level * 25) + "% damage to wounded targets)";
            case SHOCKWAVE -> name + " (hurts everything nearby)";
            default -> name;
        };
    }

    public static String roman(int n) {
        String[] numerals = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return n >= 0 && n < numerals.length ? numerals[n] : String.valueOf(n);
    }
}

package com.kiancars.carmod.hero;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The built-in "AI". Given a tier and a piece of gear it invents a name,
 * a color, a set of superpowers and a crafting cost. It's deterministic: the
 * same tier always gives the same design, on the server and on every client,
 * so nothing has to be sent over the network. There is no top tier: tier
 * 1,000 gets designed just like tier 2.
 */
public final class HeroGenerator {

    private static final String[] ADJECTIVES = {
            "Crimson", "Azure", "Golden", "Shadow", "Storm", "Nova", "Iron", "Solar", "Lunar", "Phantom",
            "Emerald", "Thunder", "Frost", "Blaze", "Cosmic", "Silent", "Mighty", "Radiant", "Savage", "Atomic",
            "Turbo", "Mystic", "Velvet", "Neon", "Rogue", "Royal", "Rapid", "Titan", "Ember", "Vortex"};

    private static final String[] SUIT_NOUNS = {
            "Guardian", "Vanguard", "Sentinel", "Defender", "Champion", "Warden", "Paladin", "Striker",
            "Ranger", "Avenger", "Protector", "Crusader", "Enforcer", "Hero", "Marshal", "Aegis"};

    private static final String[] WEAPON_NOUNS = {
            "Fang", "Thorn", "Cleaver", "Reaper", "Breaker", "Slicer", "Edge", "Wrath", "Fury", "Talon",
            "Smasher", "Piercer", "Ravager", "Whisper", "Comet", "Bane"};

    /** Crafting materials by difficulty: bracket 0 is everyday stuff, higher brackets are rarer. */
    private static final String[][] MATERIALS = {
            {"minecraft:iron_ingot", "minecraft:gold_ingot", "minecraft:redstone_block", "minecraft:lapis_block", "minecraft:copper_ingot"},
            {"minecraft:diamond", "minecraft:emerald", "minecraft:quartz_block", "minecraft:obsidian", "minecraft:ender_pearl"},
            {"minecraft:diamond_block", "minecraft:emerald_block", "minecraft:ender_eye", "minecraft:blaze_rod", "minecraft:ghast_tear", "minecraft:phantom_membrane"},
            {"minecraft:netherite_scrap", "minecraft:shulker_shell", "minecraft:echo_shard", "minecraft:breeze_rod", "minecraft:dragon_breath"},
            {"minecraft:netherite_ingot", "minecraft:nether_star", "minecraft:totem_of_undying", "minecraft:netherite_block"}};

    /** How many non-hostile mobs you must defeat to move up from this tier to the next one. */
    public static int killsNeeded(int tier) {
        return 100 + 20 * (Math.max(tier, 1) - 1);
    }

    public static HeroDesign design(int tier, HeroGear gear) {
        int t = Math.max(tier, 1);
        Random random = new Random(seed(t, gear));
        String name = name(random, t, gear);
        int color = color(random);
        return new HeroDesign(t, gear, name, color, powers(random, t, gear), upgrades(random, t, gear), cost(random, t, gear));
    }

    /** All five designs for one tier, suit first. */
    public static List<HeroDesign> designsForTier(int tier) {
        List<HeroDesign> designs = new ArrayList<>();
        for (HeroGear gear : HeroGear.ALL) {
            designs.add(design(tier, gear));
        }
        return designs;
    }

    // ------------------------------------------------------------------ parts

    private static long seed(int tier, HeroGear gear) {
        long x = tier * 1_000_003L + gear.ordinal() * 7_919L + 0x5DEECE66DL;
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        return x * 0x9E3779B97F4A7C15L;
    }

    private static String name(Random random, int tier, HeroGear gear) {
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String[] nouns = gear == HeroGear.SUIT ? SUIT_NOUNS : WEAPON_NOUNS;
        String noun = nouns[random.nextInt(nouns.length)];
        return adjective + " " + noun + " " + gear.label() + " Mk " + tier;
    }

    private static int color(Random random) {
        float hue = random.nextFloat();
        float saturation = 0.55F + random.nextFloat() * 0.35F;
        float brightness = 0.80F + random.nextFloat() * 0.20F;
        return Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
    }

    private static List<HeroDesign.PowerLevel> powers(Random random, int tier, HeroGear gear) {
        List<HeroDesign.PowerLevel> result = new ArrayList<>();
        List<HeroPower> passives = new ArrayList<>(HeroPower.PASSIVES);
        List<HeroPower> hits = new ArrayList<>(HeroPower.ON_HIT);
        Collections.shuffle(passives, random);
        Collections.shuffle(hits, random);

        int index = 0;
        if (gear == HeroGear.SUIT) {
            int count = Math.min(2 + (tier - 1) / 2, passives.size());
            for (int i = 0; i < count; i++) {
                result.add(level(passives.get(i), tier, index++));
            }
        } else {
            if (gear.isTool()) {
                // Tools always speed you up while you hold them.
                result.add(level(HeroPower.QUICK_HANDS, tier, index++));
            }
            int count = Math.min(1 + (tier - 1) / 3, hits.size());
            for (int i = 0; i < count; i++) {
                result.add(level(hits.get(i), tier, index++));
            }
        }
        return result;
    }

    /** Stronger with tier, and later powers on the list start a little weaker. */
    private static HeroDesign.PowerLevel level(HeroPower power, int tier, int index) {
        int level = 1 + Math.max(0, tier - 1 - index) / 3;
        return new HeroDesign.PowerLevel(power, Math.min(level, power.maxLevel()));
    }

    private static final String[] UPGRADE_PREFIXES = {
            "Quantum", "Plasma", "Hyper", "Ultra", "Omega", "Nano", "Photon", "Void", "Aether", "Chrono"};

    /**
     * Stat boosts that keep growing with the tier forever (some have a
     * sensible ceiling, the rest never stop). Applied as item attributes.
     */
    private static List<HeroDesign.Upgrade> upgrades(Random random, int tier, HeroGear gear) {
        List<HeroDesign.Upgrade> list = new ArrayList<>();
        String p1 = UPGRADE_PREFIXES[random.nextInt(UPGRADE_PREFIXES.length)];
        String p2 = UPGRADE_PREFIXES[random.nextInt(UPGRADE_PREFIXES.length)];
        if (gear == HeroGear.SUIT) {
            list.add(new HeroDesign.Upgrade(p1 + " Nano-plating", HeroDesign.Stat.ARMOR, Math.min(4 + tier, 20)));
            list.add(new HeroDesign.Upgrade(p2 + " Heart Core", HeroDesign.Stat.MAX_HEALTH, 2.0 * tier));
            list.add(new HeroDesign.Upgrade("Jet Servos", HeroDesign.Stat.SPEED, Math.min(0.01 * tier, 0.6)));
            list.add(new HeroDesign.Upgrade("Gravity Anchor", HeroDesign.Stat.KNOCKBACK_RESIST, Math.min(0.03 * tier, 1.0)));
            list.add(new HeroDesign.Upgrade("Titan Weave", HeroDesign.Stat.TOUGHNESS, Math.min(0.4 * tier, 10.0)));
        } else {
            list.add(new HeroDesign.Upgrade(p1 + " Overclock", HeroDesign.Stat.ATTACK_DAMAGE, 1.5 * tier));
            if (gear.isTool()) {
                list.add(new HeroDesign.Upgrade(p2 + " Drill Core", HeroDesign.Stat.MINING_SPEED, 0.5 * tier));
            } else {
                list.add(new HeroDesign.Upgrade(p2 + " Quick Edge", HeroDesign.Stat.ATTACK_SPEED, Math.min(0.05 * tier, 1.5)));
            }
        }
        return list;
    }

    private static List<HeroDesign.Cost> cost(Random random, int tier, HeroGear gear) {
        int bracket = (tier - 1) / 3;
        double scale = 1.0 + tier * 0.35;
        boolean suit = gear == HeroGear.SUIT;

        String core = pick(random, bracket, null);
        String support = pick(random, Math.max(bracket - 1, 0), core);
        String rare = pick(random, bracket + 1, core, support);

        int coreCount = (int) Math.round((suit ? 12 : 5) * scale);
        int supportCount = (int) Math.round((suit ? 8 : 3) * scale);
        int rareCount = suit ? 2 + tier / 3 : 1 + tier / 5;
        return List.of(
                new HeroDesign.Cost(core, Math.min(coreCount, 192)),
                new HeroDesign.Cost(support, Math.min(supportCount, 128)),
                new HeroDesign.Cost(rare, Math.min(rareCount, 16)));
    }

    private static String pick(Random random, int bracket, String... avoid) {
        String[] pool = MATERIALS[Math.min(bracket, MATERIALS.length - 1)];
        for (int attempt = 0; attempt < 20; attempt++) {
            String choice = pool[random.nextInt(pool.length)];
            boolean clash = false;
            if (avoid != null) {
                for (String other : avoid) {
                    clash |= choice.equals(other);
                }
            }
            if (!clash) {
                return choice;
            }
        }
        return pool[0];
    }

    private HeroGenerator() {
    }
}

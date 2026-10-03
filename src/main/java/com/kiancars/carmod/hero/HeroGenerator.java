package com.kiancars.carmod.hero;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/**
 * The built-in "AI". Given what the player searched for, a tier and a piece
 * of gear it invents a name, a color, superpowers that fit the search, and a
 * crafting cost. It is deterministic: the same search, tier and gear always
 * give the same design, on the server and on every client, so nothing has to
 * be saved. There is no top tier: tier 1,000 is designed just like tier 2.
 * <p>
 * It works offline by matching your words against a library of themes
 * (spider, thunder, teleport, ...) and mixing their powers. Searches it does
 * not recognise still work: it improvises from the letters you typed.
 */
public final class HeroGenerator {

    public static final int MAX_QUERY = 30;

    private static final String[] ADJECTIVES = {
            "Crimson", "Azure", "Golden", "Shadow", "Storm", "Nova", "Prime", "Solar", "Lunar", "Phantom",
            "Emerald", "Ultra", "Frost", "Blaze", "Cosmic", "Silent", "Mighty", "Radiant", "Savage", "Atomic",
            "Turbo", "Mystic", "Velvet", "Neon", "Rogue", "Royal", "Rapid", "Titan", "Ember", "Vortex"};

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

    /** Tidies a search: lowercase, single spaces, limited length. */
    public static String normalize(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        return q.length() > MAX_QUERY ? q.substring(0, MAX_QUERY).trim() : q;
    }

    /** True when the AI recognises at least one word of the search. */
    public static boolean understands(String query) {
        String q = normalize(query);
        for (HeroTheme theme : HeroTheme.ALL) {
            if (theme.score(q) > 0) {
                return true;
            }
        }
        return false;
    }

    public static HeroDesign design(String query, int tier, HeroGear gear) {
        String q = normalize(query);
        int t = Math.max(tier, 1);
        Random random = new Random(seed(q, t, gear));
        List<HeroTheme> themes = themesFor(q);
        String name = name(random, q, t, gear);
        int color = color(random, themes.get(0), t);
        return new HeroDesign(q, t, gear, name, color, powers(random, themes, t, gear),
                abilities(themes, t, gear), upgrades(random, t, gear), cost(random, t, gear));
    }

    // ------------------------------------------------------------------ parts

    private static long hash(String text) {
        long h = 1125899906842597L;
        for (int i = 0; i < text.length(); i++) {
            h = 31 * h + text.charAt(i);
        }
        return h;
    }

    private static long seed(String query, int tier, HeroGear gear) {
        long x = hash(query) + tier * 1_000_003L + gear.ordinal() * 7_919L + 0x5DEECE66DL;
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        return x * 0x9E3779B97F4A7C15L;
    }

    /** The one or two themes that best match the search (or lucky guesses if nothing matches). */
    static List<HeroTheme> themesFor(String query) {
        List<HeroTheme> matches = new ArrayList<>(HeroTheme.ALL);
        matches.removeIf(theme -> theme.score(query) <= 0);
        matches.sort(Comparator.comparingInt((HeroTheme theme) -> theme.score(query)).reversed());
        if (matches.size() > 2) {
            matches = new ArrayList<>(matches.subList(0, 2));
        }
        if (matches.isEmpty()) {
            Random random = new Random(hash(query));
            List<HeroTheme> all = new ArrayList<>(HeroTheme.ALL);
            Collections.shuffle(all, random);
            matches = new ArrayList<>(all.subList(0, 2));
        }
        return matches;
    }

    private static String title(String query) {
        StringBuilder out = new StringBuilder();
        boolean up = true;
        for (char c : query.toCharArray()) {
            out.append(up ? Character.toUpperCase(c) : c);
            up = c == ' ' || c == '-';
        }
        return out.length() == 0 ? "Hero" : out.toString();
    }

    private static String name(Random random, String query, int tier, HeroGear gear) {
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        return adjective + " " + title(query) + " " + gear.label() + " Mk " + tier;
    }

    private static int color(Random random, HeroTheme theme, int tier) {
        float hue = (theme.hue() + (random.nextFloat() - 0.5F) * 0.08F + 1.0F) % 1.0F;
        float saturation = 0.55F + random.nextFloat() * 0.35F;
        float brightness = 0.80F + random.nextFloat() * 0.20F;
        return Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
    }

    private static <T> List<T> interleave(List<HeroTheme> themes, java.util.function.Function<HeroTheme, List<T>> pick) {
        Set<T> result = new LinkedHashSet<>();
        int longest = 0;
        for (HeroTheme theme : themes) {
            longest = Math.max(longest, pick.apply(theme).size());
        }
        // The best-matching theme gets two picks per round, so it leads the list.
        for (int i = 0; i < longest; i++) {
            for (int k = 0; k < themes.size(); k++) {
                List<T> list = pick.apply(themes.get(k));
                int picks = k == 0 ? 2 : 1;
                for (int j = 0; j < picks; j++) {
                    int index = k == 0 ? 2 * i + j : i;
                    if (index < list.size()) {
                        result.add(list.get(index));
                    }
                }
            }
        }
        return new ArrayList<>(result);
    }

    /** The active abilities: the suit gets the most, each tool or blade gets its own pick. */
    private static List<HeroDesign.AbilityLevel> abilities(List<HeroTheme> themes, int tier, HeroGear gear) {
        List<HeroAbility> pool = interleave(themes, HeroTheme::abilities);
        List<HeroDesign.AbilityLevel> result = new ArrayList<>();
        if (gear == HeroGear.SUIT) {
            int count = Math.min(2 + (tier - 1) / 2, pool.size());
            for (int i = 0; i < count; i++) {
                result.add(abilityLevel(pool.get(i), tier, i));
            }
            return result;
        }
        pool.removeIf(HeroAbility::suitOnly);
        int count = Math.min(1 + (tier - 1) / 4, pool.size());
        int start = gear.ordinal() - 1;
        for (int i = 0; i < count; i++) {
            result.add(abilityLevel(pool.get((start + i) % pool.size()), tier, i));
        }
        return result;
    }

    private static HeroDesign.AbilityLevel abilityLevel(HeroAbility ability, int tier, int index) {
        return new HeroDesign.AbilityLevel(ability, Math.min(1 + Math.max(0, tier - 1 - index) / 3, 10));
    }

    private static List<HeroDesign.PowerLevel> powers(Random random, List<HeroTheme> themes, int tier, HeroGear gear) {
        List<HeroDesign.PowerLevel> result = new ArrayList<>();
        int index = 0;
        if (gear == HeroGear.SUIT) {
            List<HeroPower> pool = interleave(themes, HeroTheme::passives);
            List<HeroPower> extra = new ArrayList<>(HeroPower.PASSIVES);
            Collections.shuffle(extra, random);
            for (HeroPower p : extra) {
                if (!pool.contains(p)) {
                    pool.add(p);
                }
            }
            int count = Math.min(2 + (tier - 1) / 2, pool.size());
            for (int i = 0; i < count; i++) {
                result.add(level(pool.get(i), tier, index++));
            }
        } else {
            if (gear.isTool()) {
                result.add(level(HeroPower.QUICK_HANDS, tier, index++));
            }
            List<HeroPower> pool = interleave(themes, HeroTheme::hits);
            List<HeroPower> extra = new ArrayList<>(HeroPower.ON_HIT);
            Collections.shuffle(extra, random);
            for (HeroPower p : extra) {
                if (!pool.contains(p)) {
                    pool.add(p);
                }
            }
            int count = Math.min(1 + (tier - 1) / 3, pool.size());
            for (int i = 0; i < count; i++) {
                result.add(level(pool.get(i), tier, index++));
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

        String core = pick(random, bracket, (String[]) null);
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

package com.kiancars.carmod.hero;

import java.util.List;
import java.util.Locale;

/**
 * One piece of gear the AI invented: a name, a color, its superpowers, and
 * what it costs to craft. Pure data, no Minecraft classes, so any tier can
 * be generated (and tested) anywhere.
 */
public record HeroDesign(String query, int tier, HeroGear gear, String name, int color, List<PowerLevel> powers,
                         List<AbilityLevel> abilities, List<Upgrade> upgrades, List<Cost> cost) {

    /** What an upgrade improves. */
    public enum Stat {
        ARMOR("armor"),
        TOUGHNESS("armor toughness"),
        MAX_HEALTH("max health"),
        SPEED("movement speed"),
        KNOCKBACK_RESIST("knockback resistance"),
        ATTACK_DAMAGE("attack damage"),
        ATTACK_SPEED("attack speed"),
        MINING_SPEED("mining speed");

        private final String label;

        Stat(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    /** A stat boost the AI invented for this tier, e.g. "Quantum Nano-plating: +8 armor". */
    public record Upgrade(String name, Stat stat, double amount) {
        public String describe() {
            String value = switch (stat) {
                case SPEED, KNOCKBACK_RESIST, MINING_SPEED -> "+" + Math.round(amount * 100) + "%";
                case MAX_HEALTH -> {
                    double hearts = amount / 2;
                    String number = hearts == Math.rint(hearts) ? String.valueOf((int) hearts) : String.valueOf(hearts);
                    yield "+" + number + (hearts == 1 ? " heart" : " hearts");
                }
                default -> "+" + (amount == Math.rint(amount) ? String.valueOf((int) amount) : String.format("%.1f", amount));
            };
            return name + ": " + value + (stat == Stat.MAX_HEALTH ? "" : " " + stat.label());
        }
    }

    /** An active ability at a strength. */
    public record AbilityLevel(HeroAbility ability, int level) {
        public String describe() {
            return ability.describe(level);
        }
    }

    /** The abilities you trigger on purpose (everything except always-on flight). */
    public List<AbilityLevel> activeAbilities() {
        return abilities.stream().filter(a -> a.ability().isActive()).toList();
    }

    /** A superpower at a strength. */
    public record PowerLevel(HeroPower power, int level) {
        public String describe() {
            return power.describe(level);
        }
    }

    /** {@code count} of an item, named by its registry id like "minecraft:diamond". */
    public record Cost(String itemId, int count) {
    }

    /** A line of lowercase text covering everything searchable about this design. */
    public String searchText() {
        StringBuilder text = new StringBuilder(name).append(' ').append(gear.label());
        for (PowerLevel p : powers) {
            text.append(' ').append(p.describe());
        }
        for (Upgrade u : upgrades) {
            text.append(' ').append(u.describe());
        }
        for (Cost c : cost) {
            text.append(' ').append(c.itemId().substring(c.itemId().indexOf(':') + 1).replace('_', ' '));
        }
        return text.toString().toLowerCase(Locale.ROOT);
    }

    public boolean matches(String query) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            return true;
        }
        String text = searchText();
        for (String word : q.split("\\s+")) {
            if (!text.contains(word)) {
                return false;
            }
        }
        return true;
    }
}

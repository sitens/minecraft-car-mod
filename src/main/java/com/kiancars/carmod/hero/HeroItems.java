package com.kiancars.carmod.hero;

import com.kiancars.carmod.registry.ModComponents;
import com.kiancars.carmod.registry.ModItems;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Turns an AI {@link HeroDesign} into a real item stack, and reads hero gear back out of stacks. */
public final class HeroItems {

    private static final Identifier ARMOR_ID = id("hero_armor");
    private static final Identifier TOUGHNESS_ID = id("hero_toughness");
    private static final Identifier HEALTH_ID = id("hero_health");
    private static final Identifier SPEED_ID = id("hero_speed");
    private static final Identifier KNOCKBACK_ID = id("hero_knockback");

    public static Item itemFor(HeroGear gear) {
        return switch (gear) {
            case SUIT -> ModItems.HERO_SUIT.get();
            case BLADE -> ModItems.HERO_BLADE.get();
            case AXE -> ModItems.HERO_AXE.get();
            case PICKAXE -> ModItems.HERO_PICKAXE.get();
            case SHOVEL -> ModItems.HERO_SHOVEL.get();
        };
    }

    /** Which kind of hero gear this stack is, or null if it isn't hero gear. */
    public static @Nullable HeroGear gearOf(ItemStack stack) {
        if (stack.isEmpty() || !stack.has(ModComponents.HERO_TIER)) {
            return null;
        }
        for (HeroGear gear : HeroGear.ALL) {
            if (stack.is(itemFor(gear))) {
                return gear;
            }
        }
        return null;
    }

    public static int tierOf(ItemStack stack) {
        return stack.getOrDefault(ModComponents.HERO_TIER, 0);
    }

    /** The AI's design for a stack of hero gear, or null if it isn't hero gear. */
    public static @Nullable HeroDesign designOf(ItemStack stack) {
        HeroGear gear = gearOf(stack);
        int tier = tierOf(stack);
        return gear == null || tier < 1 ? null : HeroGenerator.design(tier, gear);
    }

    public static Item costItem(HeroDesign.Cost cost) {
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(cost.itemId()));
        return item == null ? Items.AIR : item;
    }

    public static ItemStack create(HeroDesign design) {
        ItemStack stack = new ItemStack(itemFor(design.gear()));
        stack.set(ModComponents.HERO_TIER, design.tier());
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(design.name())
                .withStyle(style -> style.withItalic(false).withColor(design.color())));
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(design.color()));
        stack.set(DataComponents.TOOLTIP_DISPLAY,
                new TooltipDisplay(false, new ReferenceLinkedOpenHashSet<>(List.of(DataComponents.DYED_COLOR))));
        stack.set(DataComponents.RARITY, Rarity.EPIC);
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        stack.set(DataComponents.LORE, new ItemLore(lore(design)));
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes(design));
        scaleMiningSpeed(stack, design);
        return stack;
    }

    private static List<Component> lore(HeroDesign design) {
        List<Component> lines = new ArrayList<>();
        lines.add(line("Tier " + design.tier() + " " + design.gear().label(), ChatFormatting.GOLD));
        lines.add(line("Superpowers:", ChatFormatting.AQUA));
        for (HeroDesign.PowerLevel power : design.powers()) {
            lines.add(line(" - " + power.describe(), ChatFormatting.GREEN));
        }
        lines.add(line("Upgrades:", ChatFormatting.AQUA));
        for (HeroDesign.Upgrade upgrade : design.upgrades()) {
            lines.add(line(" - " + upgrade.describe(), ChatFormatting.YELLOW));
        }
        lines.add(line("Designed by the AI Crafting Table", ChatFormatting.DARK_GRAY));
        return lines;
    }

    private static Component line(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(style -> style.withItalic(false).withColor(color));
    }

    private static ItemAttributeModifiers attributes(HeroDesign design) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        if (design.gear() == HeroGear.SUIT) {
            for (HeroDesign.Upgrade up : design.upgrades()) {
                switch (up.stat()) {
                    case ARMOR -> builder.add(Attributes.ARMOR, add(ARMOR_ID, up.amount()), EquipmentSlotGroup.CHEST);
                    case TOUGHNESS -> builder.add(Attributes.ARMOR_TOUGHNESS, add(TOUGHNESS_ID, up.amount()), EquipmentSlotGroup.CHEST);
                    case MAX_HEALTH -> builder.add(Attributes.MAX_HEALTH, add(HEALTH_ID, up.amount()), EquipmentSlotGroup.CHEST);
                    case SPEED -> builder.add(Attributes.MOVEMENT_SPEED,
                            new AttributeModifier(SPEED_ID, up.amount(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.CHEST);
                    case KNOCKBACK_RESIST -> builder.add(Attributes.KNOCKBACK_RESISTANCE, add(KNOCKBACK_ID, up.amount()), EquipmentSlotGroup.CHEST);
                    default -> {
                    }
                }
            }
            return builder.build();
        }

        double baseDamage = switch (design.gear()) {
            case BLADE -> 6.0;
            case AXE -> 9.0;
            case PICKAXE -> 4.0;
            default -> 4.5;
        };
        double baseSpeed = switch (design.gear()) {
            case BLADE -> 1.6;
            case AXE -> 1.0;
            case PICKAXE -> 1.2;
            default -> 1.0;
        };
        double damage = baseDamage;
        double speed = baseSpeed;
        for (HeroDesign.Upgrade up : design.upgrades()) {
            if (up.stat() == HeroDesign.Stat.ATTACK_DAMAGE) {
                damage += up.amount();
            } else if (up.stat() == HeroDesign.Stat.ATTACK_SPEED) {
                speed += up.amount();
            }
        }
        // Item modifiers add on top of the player's own base (damage 1.0, speed 4.0).
        builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage - 1.0,
                AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, speed - 4.0,
                AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        return builder.build();
    }

    private static AttributeModifier add(Identifier id, double amount) {
        return new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE);
    }

    private static void scaleMiningSpeed(ItemStack stack, HeroDesign design) {
        double bonus = 0.0;
        for (HeroDesign.Upgrade up : design.upgrades()) {
            if (up.stat() == HeroDesign.Stat.MINING_SPEED) {
                bonus += up.amount();
            }
        }
        Tool tool = stack.get(DataComponents.TOOL);
        if (bonus <= 0.0 || tool == null) {
            return;
        }
        float factor = (float) (1.0 + bonus);
        List<Tool.Rule> rules = new ArrayList<>();
        for (Tool.Rule rule : tool.rules()) {
            rules.add(new Tool.Rule(rule.blocks(), rule.speed().map(s -> s * factor), rule.correctForDrops()));
        }
        stack.set(DataComponents.TOOL, new Tool(rules, tool.defaultMiningSpeed(), tool.damagePerBlock(), tool.canDestroyBlocksInCreative()));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(com.kiancars.carmod.CarMod.MOD_ID, path);
    }

    private HeroItems() {
    }
}

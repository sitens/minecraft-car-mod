package com.kiancars.carmod.menu;

import com.kiancars.carmod.hero.HeroDesign;
import com.kiancars.carmod.hero.HeroGear;
import com.kiancars.carmod.hero.HeroGenerator;
import com.kiancars.carmod.hero.HeroItems;
import com.kiancars.carmod.hero.HeroProgress;
import com.kiancars.carmod.registry.ModBlocks;
import com.kiancars.carmod.registry.ModMenus;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The brain behind the AI Crafting Table screen. It has no item slots: gear
 * is "crafted" by a button press that checks the player's inventory for the
 * AI-generated ingredient list. That's what lets the recipes be endless (they
 * aren't recipes in the game's recipe book at all).
 * <p>
 * Button ids: {@code tier * 8 + gearIndex} crafts that piece of gear;
 * {@link #UPGRADE_BUTTON} spends kills to move up a tier.
 */
public class HeroWorkshopMenu extends AbstractContainerMenu {

    public static final int UPGRADE_BUTTON = Integer.MAX_VALUE;

    private final ContainerLevelAccess access;
    private final Player player;
    private final DataSlot tierSlot = DataSlot.standalone();
    private final DataSlot killsSlot = DataSlot.standalone();

    public HeroWorkshopMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenus.AI_WORKSHOP.get(), containerId);
        this.access = access;
        this.player = inventory.player;
        this.addDataSlot(tierSlot);
        this.addDataSlot(killsSlot);
        refresh();
    }

    public static int craftButtonId(int tier, HeroGear gear) {
        return tier * 8 + gear.ordinal();
    }

    /** Highest tier the player has unlocked (kept in sync with the server). */
    public int tier() {
        return Math.max(tierSlot.get(), 1);
    }

    public int kills() {
        return killsSlot.get();
    }

    public int killsNeeded() {
        return HeroGenerator.killsNeeded(tier());
    }

    private void refresh() {
        if (!player.level().isClientSide()) {
            HeroProgress progress = HeroProgress.of(player);
            tierSlot.set(progress.tier());
            killsSlot.set(Math.min(progress.kills(), Short.MAX_VALUE));
        }
    }

    @Override
    public void broadcastChanges() {
        refresh();
        super.broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player clicker, int id) {
        if (clicker.level().isClientSide()) {
            return true;
        }
        HeroProgress progress = HeroProgress.of(clicker);
        if (id == UPGRADE_BUTTON) {
            if (!progress.canUpgrade()) {
                return false;
            }
            HeroProgress next = progress.upgraded();
            HeroProgress.save(clicker, next);
            clicker.level().playSound(null, clicker.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            clicker.displayClientMessage(Component.literal("The AI designed Mk " + next.tier() + " gear for you!"), false);
            refresh();
            return true;
        }

        int tier = id / 8;
        HeroGear gear = HeroGear.byIndex(id % 8);
        if (id < 0 || gear == null || tier < 1 || tier > progress.tier()) {
            return false;
        }
        HeroDesign design = HeroGenerator.design(tier, gear);
        if (!hasIngredients(clicker, design)) {
            clicker.displayClientMessage(Component.literal("You don't have all the ingredients yet."), true);
            return false;
        }
        if (!clicker.hasInfiniteMaterials()) {
            for (HeroDesign.Cost cost : design.cost()) {
                Item item = HeroItems.costItem(cost);
                clicker.getInventory().clearOrCountMatchingItems(stack -> stack.is(item), cost.count(),
                        clicker.inventoryMenu.getCraftSlots());
            }
        }
        ItemStack result = HeroItems.create(design);
        if (!clicker.getInventory().add(result)) {
            clicker.drop(result, false);
        }
        clicker.level().playSound(null, clicker.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.8F, 1.4F);
        return true;
    }

    private static boolean hasIngredients(Player player, HeroDesign design) {
        if (player.hasInfiniteMaterials()) {
            return true;
        }
        for (HeroDesign.Cost cost : design.cost()) {
            Item item = HeroItems.costItem(cost);
            if (item == Items.AIR || player.getInventory().countItem(item) < cost.count()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.AI_CRAFTING_TABLE.get());
    }
}

package com.kiancars.carmod.menu;

import com.kiancars.carmod.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * A 4x4 (16-slot) analogue of vanilla's {@code CraftingMenu} (which is 3x3 /
 * 9 slots). Vanilla shaped recipes already support arbitrary grid sizes —
 * this menu doesn't need a custom recipe type, just a bigger grid — so any
 * recipe wider or taller than 3 can *only* be crafted here.
 * <p>
 * NOTE — written against the general shape of vanilla's crafting menu code
 * as of the 1.21.x line (which factors shared crafting-grid logic into an
 * {@code AbstractCraftingMenu} base). Confirm this still matches once
 * Gradle resolves the real 1.21.11 mappings; the constructor signatures
 * for {@link TransientCraftingContainer} / the abstract base in particular
 * are the most likely things to have shifted.
 */
public class CraftingTable4x4Menu extends AbstractContainerMenu {

    public static final int GRID_WIDTH = 4;
    public static final int GRID_HEIGHT = 4;
    private static final int GRID_SLOT_COUNT = GRID_WIDTH * GRID_HEIGHT; // 16

    // Slot layout (pixels). Vanilla's 3x3 table starts its grid at (30, 17)
    // with the result slot at (124, 35) on a 176x166 background. We add one
    // extra column to the left of the grid and one extra row above it, and
    // shift the whole grid + result slot down/right slightly so nothing
    // overlaps the (also slightly regrown) player inventory panel below.
    private static final int GRID_ORIGIN_X = 12;
    private static final int GRID_ORIGIN_Y = 17;
    private static final int SLOT_SIZE = 18;
    private static final int RESULT_SLOT_X = GRID_ORIGIN_X + GRID_WIDTH * SLOT_SIZE + 20;
    private static final int RESULT_SLOT_Y = GRID_ORIGIN_Y + (GRID_HEIGHT * SLOT_SIZE) / 2 - 9;

    private final CraftingContainer craftSlots;
    private final ResultContainer resultSlots = new ResultContainer();
    private final ContainerLevelAccess access;
    private final Player player;

    public CraftingTable4x4Menu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, playerInventory.player.level(), null);
    }

    public CraftingTable4x4Menu(int containerId, Inventory playerInventory, Level level, BlockPos pos) {
        super(ModMenus.CRAFTING_TABLE_4X4.get(), containerId);
        this.access = pos != null ? ContainerLevelAccess.create(level, pos) : ContainerLevelAccess.NULL;
        this.player = playerInventory.player;
        this.craftSlots = new TransientCraftingContainer(this, GRID_WIDTH, GRID_HEIGHT);

        this.addSlot(new net.minecraft.world.inventory.ResultSlot(
                playerInventory.player, this.craftSlots, this.resultSlots, 0, RESULT_SLOT_X, RESULT_SLOT_Y));

        for (int row = 0; row < GRID_HEIGHT; row++) {
            for (int col = 0; col < GRID_WIDTH; col++) {
                this.addSlot(new Slot(this.craftSlots, col + row * GRID_WIDTH,
                        GRID_ORIGIN_X + col * SLOT_SIZE, GRID_ORIGIN_Y + row * SLOT_SIZE));
            }
        }

        // Player inventory, shifted down to sit below the taller grid.
        int invY = GRID_ORIGIN_Y + GRID_HEIGHT * SLOT_SIZE + 14;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * SLOT_SIZE, invY + row * SLOT_SIZE));
            }
        }
        int hotbarY = invY + 3 * SLOT_SIZE + 4;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * SLOT_SIZE, hotbarY));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        access.execute((level, pos) -> slotChangedCraftingGrid(this, level, player, craftSlots, resultSlots));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> this.clearContainer(player, this.craftSlots));
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((level, pos) -> true, true);
        // NOTE: swap the line above for
        //   stillValid(access, player, com.kiancars.carmod.registry.ModBlocks.CRAFTING_TABLE_4X4.get())
        // once confirming `stillValid`'s signature against 1.21.11 — kept
        // permissive for now so the menu isn't silently unusable.
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // NOTE: shift-click routing between the 16 grid slots, the result
        // slot, and player inventory/hotbar — port vanilla CraftingMenu's
        // quickMoveStack logic here, adjusting the hardcoded slot index
        // ranges for GRID_SLOT_COUNT (16) instead of 9. Left as a stub so
        // the menu compiles and works via normal click-drag first.
        return ItemStack.EMPTY;
    }

    public int getGridSlotCount() {
        return GRID_SLOT_COUNT;
    }
}

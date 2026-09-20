package com.kiancars.carmod.menu;

import com.kiancars.carmod.registry.ModBlocks;
import com.kiancars.carmod.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/**
 * A 4x4 (16-slot) analogue of vanilla's {@code CraftingMenu} (which is 3x3 /
 * 9 slots), built on the same shared {@code AbstractCraftingMenu} base that
 * {@code CraftingMenu} uses. Vanilla shaped recipes already support
 * arbitrary grid sizes, so this menu needs no custom recipe type — any
 * recipe wider or taller than 3 simply can't be crafted anywhere else.
 * <p>
 * {@code slotChangedCraftingGrid} itself lives on {@code CraftingMenu}
 * (a sibling class, not the shared base), so it's reimplemented here
 * against the real 1.21.11 source rather than inherited.
 */
public class CraftingTable4x4Menu extends AbstractCraftingMenu {

    public static final int GRID_WIDTH = 4;
    public static final int GRID_HEIGHT = 4;
    private static final int GRID_SLOT_COUNT = GRID_WIDTH * GRID_HEIGHT; // 16
    private static final int RESULT_SLOT = 0;
    private static final int CRAFT_SLOT_START = 1;
    private static final int CRAFT_SLOT_END = CRAFT_SLOT_START + GRID_SLOT_COUNT; // 17
    private static final int INV_SLOT_START = CRAFT_SLOT_END; // 17
    private static final int INV_SLOT_END = INV_SLOT_START + 27; // 44
    private static final int USE_ROW_SLOT_START = INV_SLOT_END; // 44
    private static final int USE_ROW_SLOT_END = USE_ROW_SLOT_START + 9; // 53

    private final ContainerLevelAccess access;
    private final Player player;
    private boolean placingRecipe;

    public CraftingTable4x4Menu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, playerInventory.player.level(), null);
    }

    public CraftingTable4x4Menu(int containerId, Inventory playerInventory, Level level, BlockPos pos) {
        super(ModMenus.CRAFTING_TABLE_4X4.get(), containerId, GRID_WIDTH, GRID_HEIGHT);
        this.access = pos != null ? ContainerLevelAccess.create(level, pos) : ContainerLevelAccess.NULL;
        this.player = playerInventory.player;

        // Layout: grid start pushed left/up and the result slot pushed out
        // further right than vanilla's 30/17 + 124/35, to leave room for the
        // extra column/row; player inventory shifted down to sit below it.
        this.addResultSlot(this.player, 132, 26);
        this.addCraftingGridSlots(12, 8);
        this.addStandardInventorySlots(playerInventory, 8, 104);
    }

    private static void slotChangedCraftingGrid(
            AbstractContainerMenu menu,
            ServerLevel level,
            Player player,
            net.minecraft.world.inventory.CraftingContainer craftSlots,
            ResultContainer resultSlots
    ) {
        CraftingInput craftingInput = craftSlots.asCraftInput();
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ItemStack result = ItemStack.EMPTY;
        // 3-arg overload used deliberately: the 4-arg ones (recipe-holder-hint
        // variants used by CraftingMenu for recipe-book placement) are
        // ambiguous here since we always pass a literal null hint.
        Optional<RecipeHolder<CraftingRecipe>> match = level.getServer()
                .getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftingInput, level);
        if (match.isPresent()) {
            RecipeHolder<CraftingRecipe> recipeHolder = match.get();
            CraftingRecipe recipe = recipeHolder.value();
            if (resultSlots.setRecipeUsed(serverPlayer, recipeHolder)) {
                ItemStack assembled = recipe.assemble(craftingInput, level.registryAccess());
                if (assembled.isItemEnabled(level.enabledFeatures())) {
                    result = assembled;
                }
            }
        }

        resultSlots.setItem(0, result);
        menu.setRemoteSlot(0, result);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), 0, result));
    }

    @Override
    public void slotsChanged(Container container) {
        if (!this.placingRecipe) {
            this.access.execute((level, pos) -> {
                if (level instanceof ServerLevel serverLevel) {
                    slotChangedCraftingGrid(this, serverLevel, this.player, this.craftSlots, this.resultSlots);
                }
            });
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.craftSlots));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.CRAFTING_TABLE_4X4.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();
            if (index == RESULT_SLOT) {
                stack.getItem().onCraftedBy(stack, player);
                if (!this.moveItemStackTo(stack, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, copy);
            } else if (index >= INV_SLOT_START && index < USE_ROW_SLOT_END) {
                if (!this.moveItemStackTo(stack, CRAFT_SLOT_START, CRAFT_SLOT_END, false)) {
                    if (index < INV_SLOT_END) {
                        if (!this.moveItemStackTo(stack, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(stack, INV_SLOT_START, INV_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(stack, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == copy.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
            if (index == RESULT_SLOT) {
                player.drop(stack, false);
            }
        }

        return copy;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public Slot getResultSlot() {
        return this.slots.get(RESULT_SLOT);
    }

    @Override
    public List<Slot> getInputGridSlots() {
        return this.slots.subList(CRAFT_SLOT_START, CRAFT_SLOT_END);
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    @Override
    protected Player owner() {
        return this.player;
    }
}

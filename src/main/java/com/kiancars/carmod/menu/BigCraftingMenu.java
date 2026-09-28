package com.kiancars.carmod.menu;

import com.kiancars.carmod.registry.ModBlocks;
import com.kiancars.carmod.registry.ModMenus;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Optional;

/**
 * A square crafting grid bigger than vanilla's 3x3 (used for the 4x4 and
 * 5x5 tables). Vanilla shaped recipes already match inside any grid size,
 * so a recipe wider or taller than 3 can only be made in one of these.
 * <p>
 * {@code slotChangedCraftingGrid} lives on vanilla's {@code CraftingMenu}
 * (a sibling of {@link AbstractCraftingMenu}, not the shared base), so it's
 * reimplemented here.
 */
public class BigCraftingMenu extends AbstractCraftingMenu {

    public static final int SLOT_SIZE = 18;
    public static final int GRID_X = 8;
    public static final int GRID_Y = 18;
    private static final int RESULT_SLOT = 0;
    private static final int CRAFT_SLOT_START = 1;

    private final int size;
    private final ContainerLevelAccess access;
    private final Player player;
    private final Block tableBlock;
    private boolean placingRecipe;

    public static BigCraftingMenu fourByFour(int containerId, Inventory inventory, ContainerLevelAccess access) {
        return new BigCraftingMenu(ModMenus.CRAFTING_TABLE_4X4.get(), containerId, inventory, access, 4,
                ModBlocks.CRAFTING_TABLE_4X4.get());
    }

    public static BigCraftingMenu fiveByFive(int containerId, Inventory inventory, ContainerLevelAccess access) {
        return new BigCraftingMenu(ModMenus.CRAFTING_TABLE_5X5.get(), containerId, inventory, access, 5,
                ModBlocks.CRAFTING_TABLE_5X5.get());
    }

    public BigCraftingMenu(MenuType<?> type, int containerId, Inventory inventory, ContainerLevelAccess access,
                           int size, Block tableBlock) {
        super(type, containerId, size, size);
        this.size = size;
        this.access = access;
        this.player = inventory.player;
        this.tableBlock = tableBlock;

        this.addResultSlot(this.player, resultX(size), resultY(size));
        this.addCraftingGridSlots(GRID_X, GRID_Y);
        this.addStandardInventorySlots(inventory, 8, inventoryY(size));
    }

    public static int resultX(int size) {
        return GRID_X + size * SLOT_SIZE + 30;
    }

    public static int resultY(int size) {
        return GRID_Y + (size * SLOT_SIZE) / 2 - 9;
    }

    public static int inventoryY(int size) {
        return GRID_Y + size * SLOT_SIZE + 14;
    }

    public int getSize() {
        return size;
    }

    private int craftSlotEnd() {
        return CRAFT_SLOT_START + size * size;
    }

    private int invSlotStart() {
        return craftSlotEnd();
    }

    private int invSlotEnd() {
        return invSlotStart() + 27;
    }

    private int hotbarEnd() {
        return invSlotEnd() + 9;
    }

    private static void slotChangedCraftingGrid(AbstractContainerMenu menu, ServerLevel level, Player player,
                                                CraftingContainer craftSlots, ResultContainer resultSlots) {
        CraftingInput input = craftSlots.asCraftInput();
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ItemStack result = ItemStack.EMPTY;
        // 3-arg overload on purpose: the 4-arg recipe-hint overloads are
        // ambiguous when passed a plain null.
        Optional<RecipeHolder<CraftingRecipe>> match = level.getServer()
                .getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, level);
        if (match.isPresent()) {
            RecipeHolder<CraftingRecipe> holder = match.get();
            if (resultSlots.setRecipeUsed(serverPlayer, holder)) {
                ItemStack assembled = holder.value().assemble(input, level.registryAccess());
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
    protected void beginPlacingRecipe() {
        this.placingRecipe = true;
    }

    @Override
    protected void finishPlacingRecipe(ServerLevel level, RecipeHolder<CraftingRecipe> recipe) {
        this.placingRecipe = false;
        slotChangedCraftingGrid(this, level, this.player, this.craftSlots, this.resultSlots);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.craftSlots));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, this.tableBlock);
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
                if (!this.moveItemStackTo(stack, invSlotStart(), hotbarEnd(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, copy);
            } else if (index >= invSlotStart() && index < hotbarEnd()) {
                if (!this.moveItemStackTo(stack, CRAFT_SLOT_START, craftSlotEnd(), false)) {
                    if (index < invSlotEnd()) {
                        if (!this.moveItemStackTo(stack, invSlotEnd(), hotbarEnd(), false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(stack, invSlotStart(), invSlotEnd(), false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(stack, invSlotStart(), hotbarEnd(), false)) {
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
        return this.slots.subList(CRAFT_SLOT_START, craftSlotEnd());
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

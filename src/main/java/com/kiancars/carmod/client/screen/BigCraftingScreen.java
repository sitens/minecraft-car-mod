package com.kiancars.carmod.client.screen;

import com.kiancars.carmod.menu.BigCraftingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Placeholder screen for the big crafting tables: no GUI texture exists yet,
 * so the panel, slots and the arrow are drawn as flat shapes.
 */
public class BigCraftingScreen extends AbstractContainerScreen<BigCraftingMenu> {

    private static final int PANEL_COLOR = 0xFFC6C6C6;
    private static final int SLOT_COLOR = 0xFF8B8B8B;
    private static final int ARROW_COLOR = 0xFF555555;

    public BigCraftingScreen(BigCraftingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        int size = menu.getSize();
        this.imageWidth = 176;
        this.imageHeight = BigCraftingMenu.inventoryY(size) + 58 + 24;
        this.inventoryLabelY = BigCraftingMenu.inventoryY(size) - 11;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, PANEL_COLOR);
        for (Slot slot : menu.slots) {
            graphics.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, SLOT_COLOR);
        }

        int size = menu.getSize();
        int arrowY = y + BigCraftingMenu.resultY(size) + 6;
        int arrowStart = x + BigCraftingMenu.GRID_X + size * BigCraftingMenu.SLOT_SIZE + 6;
        int arrowEnd = x + BigCraftingMenu.resultX(size) - 6;
        graphics.fill(arrowStart, arrowY, arrowEnd - 4, arrowY + 4, ARROW_COLOR);
        for (int i = 0; i < 5; i++) {
            graphics.fill(arrowEnd - 5 + i, arrowY - 3 + i, arrowEnd - 4 + i, arrowY + 7 - i, ARROW_COLOR);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}

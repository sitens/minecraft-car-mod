package com.kiancars.carmod.client.screen;

import com.kiancars.carmod.menu.CraftingTable4x4Menu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Placeholder screen: no custom GUI texture exists yet, so the background is
 * drawn procedurally (flat panel + per-slot outlines) instead of blitting a
 * PNG. Swap {@link #renderBg} for a real {@code blit(...)} call once art for
 * a widened crafting-table panel exists — everything else (slot positions,
 * title) is already final.
 */
public class CraftingTable4x4Screen extends AbstractContainerScreen<CraftingTable4x4Menu> {

    private static final int PANEL_COLOR = 0xC0C6C6C6;
    private static final int SLOT_COLOR = 0xFF8B8B8B;

    public CraftingTable4x4Screen(CraftingTable4x4Menu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 196;
        this.imageHeight = 220;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, PANEL_COLOR);
        for (net.minecraft.world.inventory.Slot slot : menu.slots) {
            graphics.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, SLOT_COLOR);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}

package com.kiancars.carmod.client.screen;

import com.kiancars.carmod.hero.HeroDesign;
import com.kiancars.carmod.hero.HeroGear;
import com.kiancars.carmod.hero.HeroGenerator;
import com.kiancars.carmod.hero.HeroItems;
import com.kiancars.carmod.menu.HeroWorkshopMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The AI Crafting Table screen: a search box and a list of the AI gear on
 * the left, the chosen design's powers and ingredients on the right, and the
 * kill progress bar along the bottom.
 */
public class HeroWorkshopScreen extends AbstractContainerScreen<HeroWorkshopMenu> {

    private static final int ROWS = 6;
    private static final int ROW_H = 24;
    private static final int LIST_X = 8;
    private static final int LIST_Y = 44;
    private static final int LIST_W = 132;
    private static final int DETAIL_X = 168;
    private static final int DETAIL_Y = 24;
    private static final int DETAIL_W = 172;
    private static final int DETAIL_H = 156;

    private EditBox search;
    private final Button[] rowButtons = new Button[ROWS];
    private Button craftButton;
    private Button upgradeButton;

    private final List<HeroDesign> designs = new ArrayList<>();
    private final List<ItemStack> icons = new ArrayList<>();
    private int builtForTier = -1;
    private String builtForQuery = null;
    private int listScroll;
    private int detailScroll;
    private HeroDesign selected;
    private List<FormattedCharSequence> detailLines = List.of();
    private List<Integer> detailColors = List.of();

    public HeroWorkshopScreen(HeroWorkshopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 348;
        this.imageHeight = 236;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();
        this.search = new EditBox(this.font, leftPos + LIST_X, topPos + 20, LIST_W, 16, Component.literal("Search"));
        this.search.setHint(Component.literal("Search the AI gear..."));
        this.search.setMaxLength(40);
        this.addRenderableWidget(this.search);
        this.setInitialFocus(this.search);

        for (int i = 0; i < ROWS; i++) {
            final int row = i;
            rowButtons[i] = this.addRenderableWidget(Button.builder(Component.empty(), b -> select(listScroll + row))
                    .bounds(leftPos + LIST_X + 22, topPos + LIST_Y + i * ROW_H, LIST_W - 22, ROW_H - 2).build());
        }
        this.addRenderableWidget(Button.builder(Component.literal("^"), b -> scrollList(-ROWS))
                .bounds(leftPos + LIST_X + LIST_W + 2, topPos + LIST_Y, 18, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("v"), b -> scrollList(ROWS))
                .bounds(leftPos + LIST_X + LIST_W + 2, topPos + LIST_Y + ROWS * ROW_H - 22, 18, 20).build());

        this.craftButton = this.addRenderableWidget(Button.builder(Component.literal("Craft this"), b -> {
            if (selected != null) {
                press(HeroWorkshopMenu.craftButtonId(selected.tier(), selected.gear()));
            }
        }).bounds(leftPos + DETAIL_X, topPos + DETAIL_Y + DETAIL_H + 6, DETAIL_W, 20).build());

        this.upgradeButton = this.addRenderableWidget(Button.builder(Component.literal("Upgrade!"), b ->
                press(HeroWorkshopMenu.UPGRADE_BUTTON)).bounds(leftPos + 262, topPos + 208, 78, 20).build());

        this.builtForTier = -1;
        this.builtForQuery = null;
        refreshList();
    }

    private void press(int id) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private void scrollList(int delta) {
        int max = Math.max(0, designs.size() - ROWS);
        listScroll = Math.max(0, Math.min(max, listScroll + delta));
    }

    /** Rebuilds the gear list when the player tier or the search text changed. */
    private void refreshList() {
        String query = search == null ? "" : search.getValue();
        int tier = menu.tier();
        if (tier == builtForTier && query.equals(builtForQuery)) {
            return;
        }
        boolean tierChanged = tier != builtForTier;
        builtForTier = tier;
        builtForQuery = query;
        designs.clear();
        icons.clear();
        for (int t = tier; t >= 1; t--) {
            for (HeroGear gear : HeroGear.values()) {
                HeroDesign design = HeroGenerator.design(t, gear);
                if (design.matches(query)) {
                    designs.add(design);
                    icons.add(HeroItems.create(design));
                }
            }
        }
        listScroll = 0;
        if (tierChanged || selected == null || !designs.contains(selected)) {
            select(designs.isEmpty() ? -1 : 0);
        }
    }

    private void select(int index) {
        selected = index < 0 || index >= designs.size() ? null : designs.get(index);
        detailScroll = 0;
        buildDetail();
    }

    private void buildDetail() {
        List<FormattedCharSequence> lines = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        if (selected == null) {
            add(lines, colors, "No gear matches your search.", 0xFFFFFFFF);
            detailLines = lines;
            detailColors = colors;
            return;
        }
        add(lines, colors, selected.name(), 0xFF000000 | selected.color());
        add(lines, colors, "Tier " + selected.tier() + " " + selected.gear().label(), 0xFFFFAA00);
        add(lines, colors, " ", 0xFFFFFFFF);
        add(lines, colors, "Superpowers", 0xFF55FFFF);
        for (HeroDesign.PowerLevel power : selected.powers()) {
            add(lines, colors, "- " + power.describe(), 0xFF55FF55);
        }
        add(lines, colors, " ", 0xFFFFFFFF);
        add(lines, colors, "Upgrades", 0xFF55FFFF);
        for (HeroDesign.Upgrade upgrade : selected.upgrades()) {
            add(lines, colors, "- " + upgrade.describe(), 0xFFFFFF55);
        }
        add(lines, colors, " ", 0xFFFFFFFF);
        add(lines, colors, "Ingredients (have / need)", 0xFF55FFFF);
        boolean player = minecraft != null && minecraft.player != null;
        boolean creative = player && minecraft.player.hasInfiniteMaterials();
        for (HeroDesign.Cost cost : selected.cost()) {
            Item item = HeroItems.costItem(cost);
            int have = player ? minecraft.player.getInventory().countItem(item) : 0;
            boolean ok = creative || have >= cost.count();
            add(lines, colors, "- " + new ItemStack(item).getHoverName().getString() + ": " + have + " / " + cost.count(),
                    ok ? 0xFF55FF55 : 0xFFFF5555);
        }
        detailLines = lines;
        detailColors = colors;
    }

    private void add(List<FormattedCharSequence> lines, List<Integer> colors, String text, int color) {
        for (FormattedCharSequence seq : this.font.split(Component.literal(text), DETAIL_W - 8)) {
            lines.add(seq);
            colors.add(color);
        }
    }

    private boolean canCraft() {
        if (selected == null || minecraft == null || minecraft.player == null) {
            return false;
        }
        if (minecraft.player.hasInfiniteMaterials()) {
            return true;
        }
        for (HeroDesign.Cost cost : selected.cost()) {
            Item item = HeroItems.costItem(cost);
            if (minecraft.player.getInventory().countItem(item) < cost.count()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshList();
        buildDetail();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            this.minecraft.player.closeContainer();
            return true;
        }
        return this.search.keyPressed(event) || this.search.canConsumeInput() || super.keyPressed(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int dir = scrollY > 0 ? -1 : 1;
        if (mouseX >= leftPos + DETAIL_X) {
            int max = Math.max(0, detailLines.size() - DETAIL_H / 10);
            detailScroll = Math.max(0, Math.min(max, detailScroll + dir * 2));
        } else {
            scrollList(dir);
        }
        return true;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF2B2F3A);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF1B1E26);
        g.fill(x + DETAIL_X - 2, y + DETAIL_Y - 2, x + DETAIL_X + DETAIL_W + 2, y + DETAIL_Y + DETAIL_H + 2, 0xFF0E1015);
        int barX = x + 8;
        int barY = y + 212;
        int barW = 240;
        int needed = Math.max(1, menu.killsNeeded());
        int kills = Math.min(menu.kills(), needed);
        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + 13, 0xFF000000);
        g.fill(barX, barY, barX + barW, barY + 12, 0xFF3A3F4D);
        g.fill(barX, barY, barX + barW * kills / needed, barY + 12, kills >= needed ? 0xFF3FD16A : 0xFF3FA7D1);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title, titleLabelX, titleLabelY, 0xFFFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        for (int i = 0; i < ROWS; i++) {
            int index = listScroll + i;
            Button b = rowButtons[i];
            b.visible = index < designs.size();
            b.active = b.visible && designs.get(index) != selected;
            if (b.visible) {
                b.setMessage(Component.literal(this.font.plainSubstrByWidth(designs.get(index).name(), LIST_W - 30)));
            }
        }
        craftButton.active = canCraft();
        upgradeButton.active = menu.kills() >= menu.killsNeeded();
        super.render(g, mouseX, mouseY, partialTick);

        ItemStack hovered = ItemStack.EMPTY;
        for (int i = 0; i < ROWS; i++) {
            int index = listScroll + i;
            if (index >= designs.size()) {
                break;
            }
            int ix = leftPos + LIST_X;
            int iy = topPos + LIST_Y + i * ROW_H + 3;
            g.renderItem(icons.get(index), ix + 2, iy);
            Button b = rowButtons[i];
            if (mouseX >= b.getX() - 22 && mouseX < b.getX() + b.getWidth()
                    && mouseY >= b.getY() && mouseY < b.getY() + b.getHeight()) {
                hovered = icons.get(index);
            }
        }

        int dx = leftPos + DETAIL_X + 4;
        int dy = topPos + DETAIL_Y + 2;
        g.enableScissor(leftPos + DETAIL_X, topPos + DETAIL_Y, leftPos + DETAIL_X + DETAIL_W, topPos + DETAIL_Y + DETAIL_H);
        for (int i = detailScroll; i < detailLines.size(); i++) {
            int ly = dy + (i - detailScroll) * 10;
            if (ly > topPos + DETAIL_Y + DETAIL_H) {
                break;
            }
            g.drawString(this.font, detailLines.get(i), dx, ly, detailColors.get(i), false);
        }
        g.disableScissor();

        int needed = menu.killsNeeded();
        String label = "Mk " + (menu.tier() + 1) + " upgrade: " + Math.min(menu.kills(), needed) + " / " + needed
                + " peaceful mobs";
        g.drawString(this.font, label, leftPos + 8 + (240 - this.font.width(label)) / 2, topPos + 214, 0xFFFFFFFF, true);
        g.drawString(this.font, "Tier " + menu.tier() + " unlocked", leftPos + 8, topPos + 197, 0xFFAAAAAA, false);

        if (!hovered.isEmpty()) {
            g.setTooltipForNextFrame(this.font, hovered, mouseX, mouseY);
        }
        this.renderTooltip(g, mouseX, mouseY);
    }
}

package com.universalrandomizer.client.gui;

import com.universalrandomizer.config.RandomizerConfig;
import com.universalrandomizer.config.RandomizerMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.*;
import java.util.function.Consumer;

/**
 * First-launch setup screen shown when a new world is created (or via /randomizer setup).
 *
 * <p>Features:
 * <ul>
 *   <li>Two-column mode checkbox grid, grouped by category</li>
 *   <li>Search bar filtering modes by name</li>
 *   <li>Category tabs (Drops / Crafting / World / Other / All)</li>
 *   <li>Per-mode randomization type dropdown</li>
 *   <li>Scrollable content area</li>
 *   <li>Profile preset buttons</li>
 *   <li>[Create World] / [Cancel] buttons</li>
 * </ul>
 *
 * <p>When confirmed, the settings are sent to the server via
 * {@link com.universalrandomizer.network.NetworkHandler}.
 */
public class RandomizerSetupScreen extends Screen {

    // ── Layout constants ───────────────────────────────────────────────────────
    private static final int PANEL_WIDTH   = 500;
    private static final int PANEL_HEIGHT  = 360;
    private static final int ROW_HEIGHT    = 24;
    private static final int COL_WIDTH     = 230;
    private static final int HEADER_HEIGHT = 60;
    private static final int FOOTER_HEIGHT = 50;

    // ── State ─────────────────────────────────────────────────────────────────
    private final RandomizerConfig editConfig;
    private final Consumer<RandomizerConfig> onConfirm;
    private final Screen parent;

    private EditBox searchBox;
    private String searchQuery = "";
    private RandomizerMode.Category activeCategory = null; // null = All
    private int scrollOffset = 0;

    // ──────────────────────────────────────────────────────────────────────────

    public RandomizerSetupScreen(Screen parent, RandomizerConfig initialConfig, Consumer<RandomizerConfig> onConfirm) {
        super(Component.literal("Universal Randomizer — Setup"));
        this.parent = parent;
        this.editConfig = copyConfig(initialConfig);
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;

        // ── Search bar ─────────────────────────────────────────────────────────
        searchBox = new EditBox(font, panelX + 10, panelY + 30, 240, 18,
            Component.literal("Search modes..."));
        searchBox.setHint(Component.literal("Search...").withStyle(Style.EMPTY.withColor(0x888888)));
        searchBox.setResponder(text -> {
            searchQuery = text.toLowerCase();
            scrollOffset = 0;
        });
        addWidget(searchBox);
        setInitialFocus(searchBox);

        // ── Category tab buttons ──────────────────────────────────────────────
        String[] tabLabels = {"All", "Drops", "Crafting", "World", "Other"};
        RandomizerMode.Category[] tabCats = {null,
            RandomizerMode.Category.DROPS,
            RandomizerMode.Category.CRAFTING,
            RandomizerMode.Category.WORLD,
            RandomizerMode.Category.OTHER};
        for (int i = 0; i < tabLabels.length; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal(tabLabels[i]), btn -> {
                activeCategory = tabCats[idx];
                scrollOffset = 0;
            }).bounds(panelX + 260 + i * 48, panelY + 30, 46, 18).build());
        }

        // ── Select All / None ─────────────────────────────────────────────────
        addRenderableWidget(Button.builder(Component.literal("All"), btn -> {
            for (RandomizerMode mode : RandomizerMode.values()) editConfig.setEnabled(mode, true);
        }).bounds(panelX + 10, panelY + PANEL_HEIGHT - FOOTER_HEIGHT + 2, 60, 18).build());

        addRenderableWidget(Button.builder(Component.literal("None"), btn -> {
            for (RandomizerMode mode : RandomizerMode.values()) editConfig.setEnabled(mode, false);
        }).bounds(panelX + 74, panelY + PANEL_HEIGHT - FOOTER_HEIGHT + 2, 60, 18).build());

        // ── Scroll buttons ────────────────────────────────────────────────────
        addRenderableWidget(Button.builder(Component.literal("▲"), btn -> {
            scrollOffset = Math.max(0, scrollOffset - ROW_HEIGHT);
        }).bounds(panelX + PANEL_WIDTH - 20, panelY + HEADER_HEIGHT, 18, 18).build());

        addRenderableWidget(Button.builder(Component.literal("▼"), btn -> {
            scrollOffset += ROW_HEIGHT;
        }).bounds(panelX + PANEL_WIDTH - 20, panelY + PANEL_HEIGHT - FOOTER_HEIGHT - 20, 18, 18).build());

        // ── Confirm / Cancel ──────────────────────────────────────────────────
        addRenderableWidget(Button.builder(Component.literal("✔ Create World"), btn -> {
            onConfirm.accept(editConfig);
            onClose();
        }).bounds(panelX + PANEL_WIDTH - 160, panelY + PANEL_HEIGHT - 28, 155, 20).build());

        addRenderableWidget(Button.builder(Component.literal("✖ Cancel"), btn -> onClose())
            .bounds(panelX + PANEL_WIDTH - 320, panelY + PANEL_HEIGHT - 28, 155, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Background
        renderBackground(graphics);

        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;

        // Panel background (dark translucent)
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xCC0A0A0A);
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 4, 0xFF4A90D9); // top accent

        // Title
        graphics.drawCenteredString(font,
            Component.literal("✦ Universal Randomizer").withStyle(s -> s.withColor(0x4A90D9).withBold(true)),
            panelX + PANEL_WIDTH / 2, panelY + 10, 0xFFFFFF);

        // Subtitle
        graphics.drawCenteredString(font,
            Component.literal("Select the gameplay systems to randomize"),
            panelX + PANEL_WIDTH / 2, panelY + 22, 0xAAAAAA);

        // Content area clipping (manual row rendering)
        int contentY = panelY + HEADER_HEIGHT;
        int contentMaxY = panelY + PANEL_HEIGHT - FOOTER_HEIGHT;

        List<RandomizerMode> filtered = getFilteredModes();
        int startY = contentY - scrollOffset;
        int col = 0;

        for (RandomizerMode mode : filtered) {
            int rowX = panelX + 10 + col * (COL_WIDTH + 10);
            int rowY = startY;

            if (rowY >= contentY && rowY + ROW_HEIGHT <= contentMaxY) {
                // Checkbox (manual since we can't use Minecraft's Checkbox widget cleanly in a scroll view)
                boolean enabled = editConfig.isEnabled(mode);
                graphics.fill(rowX, rowY + 4, rowX + 14, rowY + 18, enabled ? 0xFF4A90D9 : 0xFF333333);
                if (enabled) {
                    graphics.drawString(font, "✓", rowX + 3, rowY + 5, 0xFFFFFF);
                }

                // Mode name
                graphics.drawString(font, mode.getDisplayName(), rowX + 18, rowY + 5,
                    enabled ? 0xFFFFFF : 0x888888);

                // Category badge
                String cat = mode.getCategory().getDisplayName();
                int badgeColor = getCategoryColor(mode.getCategory());
                graphics.drawString(font, "[" + cat + "]", rowX + COL_WIDTH - 60, rowY + 5, badgeColor);

                // Tooltip on hover
                if (mouseX >= rowX && mouseX <= rowX + COL_WIDTH
                 && mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT) {
                    graphics.renderTooltip(font,
                        Component.literal(mode.getDescription()).withStyle(s -> s.withColor(0xAAAAAA)),
                        mouseX, mouseY);
                }
            }

            // Advance col/row
            col++;
            if (col >= 2) {
                col = 0;
                startY += ROW_HEIGHT;
            }
        }

        // Divider lines
        graphics.fill(panelX, panelY + HEADER_HEIGHT - 2, panelX + PANEL_WIDTH, panelY + HEADER_HEIGHT, 0xFF222222);
        graphics.fill(panelX, panelY + PANEL_HEIGHT - FOOTER_HEIGHT, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT - FOOTER_HEIGHT + 1, 0xFF222222);

        // Enabled count
        long enabledCount = Arrays.stream(RandomizerMode.values())
            .filter(m -> editConfig.isEnabled(m)).count();
        graphics.drawString(font, enabledCount + " / " + RandomizerMode.values().length + " modes enabled",
            panelX + 145, panelY + PANEL_HEIGHT - FOOTER_HEIGHT + 7, 0xAAAAAA);

        // Search box + widgets
        searchBox.render(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle checkbox clicks
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;
        int contentY = panelY + HEADER_HEIGHT;
        int contentMaxY = panelY + PANEL_HEIGHT - FOOTER_HEIGHT;

        List<RandomizerMode> filtered = getFilteredModes();
        int startY = contentY - scrollOffset;
        int col = 0;

        for (RandomizerMode mode : filtered) {
            int rowX = panelX + 10 + col * (COL_WIDTH + 10);
            int rowY = startY;

            if (rowY >= contentY && rowY + ROW_HEIGHT <= contentMaxY) {
                if (mouseX >= rowX && mouseX <= rowX + COL_WIDTH
                 && mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT) {
                    editConfig.setEnabled(mode, !editConfig.isEnabled(mode));
                    return true;
                }
            }
            col++;
            if (col >= 2) { col = 0; startY += ROW_HEIGHT; }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollOffset = Math.max(0, scrollOffset - (int)(delta * ROW_HEIGHT));
        return true;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // ──────────────────────────────────────────────────────────────────────────

    private List<RandomizerMode> getFilteredModes() {
        List<RandomizerMode> result = new ArrayList<>();
        for (RandomizerMode mode : RandomizerMode.values()) {
            if (activeCategory != null && mode.getCategory() != activeCategory) continue;
            if (!searchQuery.isEmpty()
             && !mode.getDisplayName().toLowerCase().contains(searchQuery)
             && !mode.getDescription().toLowerCase().contains(searchQuery)) continue;
            result.add(mode);
        }
        return result;
    }

    private static int getCategoryColor(RandomizerMode.Category cat) {
        return switch (cat) {
            case DROPS    -> 0xFF6BA3;
            case CRAFTING -> 0xFFB347;
            case WORLD    -> 0x7EC8E3;
            case OTHER    -> 0xB8A9C9;
        };
    }

    private static RandomizerConfig copyConfig(RandomizerConfig src) {
        RandomizerConfig copy = new RandomizerConfig();
        for (RandomizerMode mode : RandomizerMode.values()) {
            copy.getMode(mode).setEnabled(src.isEnabled(mode));
            copy.getMode(mode).setRandomType(src.getMode(mode).getRandomType());
            copy.getMode(mode).setSeed(src.getMode(mode).getSeed());
        }
        return copy;
    }
}

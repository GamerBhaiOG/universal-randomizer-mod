package com.universalrandomizer.client.gui;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.network.ClientConfigCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.EnumMap;
import java.util.Map;

/**
 * Main Inventory-style GUI dashboard for Universal Randomizer.
 * Clicking the main mode card toggles it ON/OFF directly.
 * Clicking the square settings button [S] opens the detailed settings screen.
 */
public class RandomizerHubScreen extends Screen {

    private final Screen parent;
    private static final int CARD_WIDTH = 165;
    private static final int GEAR_WIDTH = 22;
    private static final int CARD_HEIGHT = 24;

    private static final Map<RandomizerMode, ItemStack> MODE_ICONS = new EnumMap<>(RandomizerMode.class);

    static {
        MODE_ICONS.put(RandomizerMode.MINING_DROPS,     new ItemStack(Items.DIAMOND_PICKAXE));
        MODE_ICONS.put(RandomizerMode.MOB_DROPS,        new ItemStack(Items.DIAMOND_SWORD));
        MODE_ICONS.put(RandomizerMode.CROP_DROPS,       new ItemStack(Items.WHEAT));
        MODE_ICONS.put(RandomizerMode.FISHING_LOOT,    new ItemStack(Items.FISHING_ROD));
        MODE_ICONS.put(RandomizerMode.CHEST_LOOT,      new ItemStack(Items.CHEST));
        MODE_ICONS.put(RandomizerMode.CRAFTING,         new ItemStack(Items.CRAFTING_TABLE));
        MODE_ICONS.put(RandomizerMode.SMELTING,         new ItemStack(Items.FURNACE));
        MODE_ICONS.put(RandomizerMode.BLOCK_PLACEMENT,  new ItemStack(Items.GRASS_BLOCK));
        MODE_ICONS.put(RandomizerMode.ENTITY_SPAWNS,    new ItemStack(Items.PIG_SPAWN_EGG));
        MODE_ICONS.put(RandomizerMode.STRUCTURE_SPAWNS, new ItemStack(Items.BELL));
        MODE_ICONS.put(RandomizerMode.WORLD_GEN,        new ItemStack(Items.OAK_SAPLING));
        MODE_ICONS.put(RandomizerMode.POTION_BREWING,   new ItemStack(Items.BREWING_STAND));
    }

    public RandomizerHubScreen(Screen parent) {
        super(Component.literal("Universal Randomizer Dashboard"));
        this.parent = parent;
    }

    public RandomizerHubScreen() {
        this(null);
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = 38;

        RandomizerMode[] modes = RandomizerMode.values();
        for (int i = 0; i < modes.length; i++) {
            final RandomizerMode mode = modes[i];
            int col = i % 2;
            int row = i / 2;

            int colWidth = CARD_WIDTH + GEAR_WIDTH + 4;
            int startX = (col == 0) ? centerX - colWidth - 4 : centerX + 4;
            int y = startY + row * (CARD_HEIGHT + 3);

            boolean isEnabled = isModeEnabled(mode);
            String statusText = isEnabled ? " §a[ON]" : " §c[OFF]";
            Component buttonText = Component.literal("     " + mode.getDisplayName() + statusText);

            // 1. Main Mode Card Button (Directly toggles ON/OFF)
            this.addRenderableWidget(Button.builder(buttonText, b -> {
                boolean newState = !isModeEnabled(mode);
                setModeEnabledLocal(mode, newState);
                sendCommand("randomizer " + mode.getId() + " " + (newState ? "enable" : "disable"));
                b.setMessage(Component.literal("     " + mode.getDisplayName() + (newState ? " §a[ON]" : " §c[OFF]")));
            }).bounds(startX, y, CARD_WIDTH, CARD_HEIGHT).build());

            // 2. Square Settings Button [S] (Opens ModeSettingsScreen)
            this.addRenderableWidget(Button.builder(Component.literal("[S]"), b -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new ModeSettingsScreen(this, mode));
                }
            }).bounds(startX + CARD_WIDTH + 2, y, GEAR_WIDTH, CARD_HEIGHT).build());
        }

        // Bottom Dashboard Action Buttons
        int actionY = this.height - 28;

        // 1. Profiles Button
        this.addRenderableWidget(Button.builder(Component.literal("Profiles"), b -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new ProfileManagerScreen(this));
            }
        }).bounds(centerX - 195, actionY, 90, 20).build());

        // 2. Weights Button
        this.addRenderableWidget(Button.builder(Component.literal("Weights"), b -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new WeightConfigScreen(this));
            }
        }).bounds(centerX - 100, actionY, 90, 20).build());

        // 3. Debug Toggle Button
        boolean isDebug = isDebugEnabled();
        Component debugLabel = Component.literal(isDebug ? "Debug: §aON" : "Debug: §cOFF");
        this.addRenderableWidget(Button.builder(debugLabel, b -> {
            boolean nextState = !isDebugEnabled();
            com.universalrandomizer.util.RandomizerLogger.setDebugEnabled(nextState);
            sendCommand("randomizer debug " + (nextState ? "on" : "off"));
            b.setMessage(Component.literal(nextState ? "Debug: §aON" : "Debug: §cOFF"));
        }).bounds(centerX - 5, actionY, 100, 20).build());

        // 4. Done / Close Button
        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
            .bounds(centerX + 100, actionY, 95, 20)
            .build());
    }

    private void sendCommand(String cmd) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.connection != null) {
            Minecraft.getInstance().player.connection.sendCommand(cmd);
        }
    }

    private boolean isModeEnabled(RandomizerMode mode) {
        if (ClientConfigCache.isSynced()) {
            return ClientConfigCache.isEnabled(mode);
        }
        try {
            RandomizerManager mgr = RandomizerManager.getInstance();
            return mgr != null && mgr.isInitialized() && mgr.isEnabled(mode);
        } catch (Exception e) {
            return false;
        }
    }

    private void setModeEnabledLocal(RandomizerMode mode, boolean enabled) {
        if (ClientConfigCache.isSynced()) {
            ClientConfigCache.setEnabled(mode, enabled);
        }
        try {
            RandomizerManager mgr = RandomizerManager.getInstance();
            if (mgr != null && mgr.getConfig() != null) {
                mgr.getConfig().setEnabled(mode, enabled);
            }
        } catch (Exception ignored) {}
    }

    private boolean isDebugEnabled() {
        return com.universalrandomizer.util.RandomizerLogger.isDebugEnabled();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        // Header Title
        graphics.drawCenteredString(this.font, "§b§lUniversal Randomizer Dashboard", this.width / 2, 10, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "§7Click card to toggle ON/OFF directly. Click [S] for advanced mode settings.", this.width / 2, 22, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, partialTick);

        // Render 3D Item Icons on Mode Buttons
        int centerX = this.width / 2;
        int startY = 38;
        RandomizerMode[] modes = RandomizerMode.values();
        for (int i = 0; i < modes.length; i++) {
            RandomizerMode mode = modes[i];
            int col = i % 2;
            int row = i / 2;

            int colWidth = CARD_WIDTH + GEAR_WIDTH + 4;
            int startX = (col == 0) ? centerX - colWidth - 4 : centerX + 4;
            int y = startY + row * (CARD_HEIGHT + 3);

            ItemStack icon = MODE_ICONS.getOrDefault(mode, new ItemStack(Items.PAPER));
            graphics.renderFakeItem(icon, startX + 4, y + 4);
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

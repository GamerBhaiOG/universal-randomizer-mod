package com.universalrandomizer.client.gui;

import com.universalrandomizer.config.ModeConfig;
import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Detailed settings Sub-GUI for a single {@link RandomizerMode}.
 * Allows toggling enable/disable, changing randomization type, setting custom seeds,
 * and regenerating mappings.
 */
public class ModeSettingsScreen extends Screen {

    private final Screen parent;
    private final RandomizerMode mode;
    private Button toggleButton;
    private Button typeButton;
    private EditBox seedBox;

    public ModeSettingsScreen(Screen parent, RandomizerMode mode) {
        super(Component.literal("Configure " + mode.getDisplayName()));
        this.parent = parent;
        this.mode = mode;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = 55;

        // 1. Enable / Disable Toggle Button
        boolean isEnabled = isModeEnabled();
        Component toggleLabel = Component.literal("Mode Status: " + (isEnabled ? "§aENABLED" : "§cDISABLED"));
        toggleButton = Button.builder(toggleLabel, b -> {
            boolean newState = !isModeEnabled();
            sendCommand("randomizer " + mode.getId() + " " + (newState ? "enable" : "disable"));
            b.setMessage(Component.literal("Mode Status: " + (newState ? "§aENABLED" : "§cDISABLED")));
        }).bounds(centerX - 100, startY, 200, 20).build();
        this.addRenderableWidget(toggleButton);

        // 2. Randomization Type Cycle Button
        String currentType = getModeType();
        typeButton = Button.builder(Component.literal("Type: §f" + currentType), b -> {
            String nextType = cycleType(getModeType());
            sendCommand("randomizer " + mode.getId() + " type " + nextType);
            b.setMessage(Component.literal("Type: §f" + nextType));
        }).bounds(centerX - 100, startY + 28, 200, 20).build();
        this.addRenderableWidget(typeButton);

        // 3. Seed Edit Box & Set Seed Button
        seedBox = new EditBox(this.font, centerX - 100, startY + 56, 120, 20, Component.literal("Seed"));
        seedBox.setMaxLength(20);
        seedBox.setValue(String.valueOf(getModeSeed()));
        this.addRenderableWidget(seedBox);

        this.addRenderableWidget(Button.builder(Component.literal("Set Seed"), b -> {
            try {
                long seed = Long.parseLong(seedBox.getValue().trim());
                sendCommand("randomizer " + mode.getId() + " seed " + seed);
            } catch (NumberFormatException ignored) {}
        }).bounds(centerX + 25, startY + 56, 75, 20).build());

        // 4. Regenerate Mappings Button
        this.addRenderableWidget(Button.builder(Component.literal("§eRegenerate Mappings"), b -> {
            sendCommand("randomizer reset");
        }).bounds(centerX - 100, startY + 84, 200, 20).build());

        // 5. Back Button
        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> this.onClose())
            .bounds(centerX - 60, this.height - 32, 120, 20)
            .build());
    }

    private void sendCommand(String cmd) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.connection != null) {
            Minecraft.getInstance().player.connection.sendCommand(cmd);
        }
    }

    private boolean isModeEnabled() {
        if (com.universalrandomizer.network.ClientConfigCache.hasConfig()) {
            return com.universalrandomizer.network.ClientConfigCache.getConfig().isEnabled(mode);
        }
        RandomizerManager mgr = RandomizerManager.getInstance();
        return mgr != null && mgr.isInitialized() && mgr.isEnabled(mode);
    }

    private String getModeType() {
        if (com.universalrandomizer.network.ClientConfigCache.hasConfig()) {
            return com.universalrandomizer.network.ClientConfigCache.getConfig().getMode(mode).getRandomType().name();
        }
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (mgr != null && mgr.isInitialized()) {
            return mgr.getConfig().getMode(mode).getRandomType().name();
        }
        return "SEED_BASED";
    }

    private long getModeSeed() {
        if (com.universalrandomizer.network.ClientConfigCache.hasConfig()) {
            return com.universalrandomizer.network.ClientConfigCache.getConfig().getMode(mode).getSeed();
        }
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (mgr != null && mgr.isInitialized()) {
            return mgr.getConfig().getMode(mode).getSeed();
        }
        return 0L;
    }

    private String cycleType(String current) {
        ModeConfig.RandomType[] types = ModeConfig.RandomType.values();
        for (int i = 0; i < types.length; i++) {
            if (types[i].name().equalsIgnoreCase(current)) {
                return types[(i + 1) % types.length].name();
            }
        }
        return ModeConfig.RandomType.SEED_BASED.name();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, "§b§lConfigure: " + mode.getDisplayName(), this.width / 2, 15, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "§7" + mode.getDescription(), this.width / 2, 30, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, partialTick);
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

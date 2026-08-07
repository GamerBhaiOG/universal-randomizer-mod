package com.universalrandomizer.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Profile management GUI screen.
 * Allows loading built-in working preset profiles or saving/loading custom user profiles.
 */
public class ProfileManagerScreen extends Screen {

    private final Screen parent;
    private EditBox customProfileBox;

    public ProfileManagerScreen(Screen parent) {
        super(Component.literal("Randomizer Profiles"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = 45;

        // Built-in Working Preset Buttons
        List<String> builtins = List.of(
            "Default", "Chaos Mode", "Speedrun", "Lucky Block", "Survival Friendly", "World Craze"
        );
        for (int i = 0; i < builtins.size(); i++) {
            final String profileName = builtins.get(i);
            int col = i % 2;
            int row = i / 2;
            int x = (col == 0) ? centerX - 195 : centerX + 5;
            int y = startY + row * 26;

            this.addRenderableWidget(Button.builder(Component.literal("Load: §e" + profileName), b -> {
                sendCommand("randomizer profile load " + profileName);
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new RandomizerHubScreen(parent));
                }
            }).bounds(x, y, 190, 22).build());
        }

        // Custom Profile Section
        int customY = startY + 3 * 26 + 10;
        customProfileBox = new EditBox(this.font, centerX - 195, customY, 190, 20, Component.literal("Profile Name"));
        customProfileBox.setMaxLength(25);
        customProfileBox.setValue("MyCustomProfile");
        this.addRenderableWidget(customProfileBox);

        this.addRenderableWidget(Button.builder(Component.literal("Save Profile"), b -> {
            String name = customProfileBox.getValue().trim();
            if (!name.isEmpty()) {
                sendCommand("randomizer profile save " + name);
            }
        }).bounds(centerX + 5, customY, 92, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Load Custom"), b -> {
            String name = customProfileBox.getValue().trim();
            if (!name.isEmpty()) {
                sendCommand("randomizer profile load " + name);
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new RandomizerHubScreen(parent));
                }
            }
        }).bounds(centerX + 103, customY, 92, 20).build());

        // Back Button
        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> this.onClose())
            .bounds(centerX - 60, this.height - 30, 120, 20)
            .build());
    }

    private void sendCommand(String cmd) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.connection != null) {
            Minecraft.getInstance().player.connection.sendCommand(cmd);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, "§b§lPreset & Custom Profiles", this.width / 2, 15, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "§7Click a working preset to instantly update GUI mode options!", this.width / 2, 28, 0xAAAAAA);

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

package com.universalrandomizer.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Interactive GUI screen for configuring custom weighted drop probabilities.
 * Features JEI/REI style searchable item pickers for source and target entries.
 */
public class WeightConfigScreen extends Screen {

    private final Screen parent;
    private EditBox sourceBox;
    private EditBox targetBox;
    private EditBox weightBox;
    private int currentWeight = 10;

    public WeightConfigScreen(Screen parent) {
        super(Component.literal("Weighted Drops Configurator"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;

        // ── 1. Source Block / Item ID ──────────────────────────────────────────
        sourceBox = new EditBox(this.font, centerX - 130, 54, 180, 20, Component.literal("Source Item ID"));
        sourceBox.setMaxLength(64);
        sourceBox.setValue("minecraft:stone");
        this.addRenderableWidget(sourceBox);

        this.addRenderableWidget(Button.builder(Component.literal("Browse..."), b -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new ItemPickerScreen(this, "Select Source Block / Item", id -> sourceBox.setValue(id)));
            }
        }).bounds(centerX + 75, 54, 60, 20).build());

        // ── 2. Target Drop Item ID ─────────────────────────────────────────────
        targetBox = new EditBox(this.font, centerX - 130, 94, 180, 20, Component.literal("Target Item ID"));
        targetBox.setMaxLength(64);
        targetBox.setValue("minecraft:diamond");
        this.addRenderableWidget(targetBox);

        this.addRenderableWidget(Button.builder(Component.literal("Browse..."), b -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new ItemPickerScreen(this, "Select Target Drop Item", id -> targetBox.setValue(id)));
            }
        }).bounds(centerX + 75, 94, 60, 20).build());

        // ── 3. Weight Value Controls ───────────────────────────────────────────
        weightBox = new EditBox(this.font, centerX - 130, 134, 75, 20, Component.literal("Weight"));
        weightBox.setMaxLength(10);
        weightBox.setValue(String.valueOf(currentWeight));
        this.addRenderableWidget(weightBox);

        this.addRenderableWidget(Button.builder(Component.literal("+1"), b -> adjustWeight(1))
            .bounds(centerX - 50, 134, 30, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+5"), b -> adjustWeight(5))
            .bounds(centerX - 16, 134, 30, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+10"), b -> adjustWeight(10))
            .bounds(centerX + 18, 134, 35, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("-1"), b -> adjustWeight(-1))
            .bounds(centerX + 57, 134, 30, 20).build());

        // ── 4. Main Action Buttons ─────────────────────────────────────────────
        this.addRenderableWidget(Button.builder(Component.literal("§aSet Weight"), b -> {
            String src = sourceBox.getValue().trim();
            String tgt = targetBox.getValue().trim();
            String wStr = weightBox.getValue().trim();
            if (!src.isEmpty() && !tgt.isEmpty() && !wStr.isEmpty()) {
                sendCommand("randomizer weight " + src + " " + tgt + " " + wStr);
            }
        }).bounds(centerX - 130, 170, 125, 22).build());

        this.addRenderableWidget(Button.builder(Component.literal("§cClear Weights"), b -> {
            String src = sourceBox.getValue().trim();
            if (!src.isEmpty()) {
                sendCommand("randomizer weightclear " + src);
            }
        }).bounds(centerX + 5, 170, 125, 22).build());

        // ── 5. Back Button ─────────────────────────────────────────────────────
        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> this.onClose())
            .bounds(centerX - 60, this.height - 28, 120, 20)
            .build());
    }

    private void adjustWeight(int delta) {
        try {
            int val = Integer.parseInt(weightBox.getValue().trim());
            val = Math.max(1, val + delta);
            currentWeight = val;
            weightBox.setValue(String.valueOf(val));
        } catch (NumberFormatException ignored) {
            weightBox.setValue("10");
        }
    }

    private void sendCommand(String cmd) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.connection != null) {
            Minecraft.getInstance().player.connection.sendCommand(cmd);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;

        graphics.drawCenteredString(this.font, "§b§lWeighted Drop Configurator", centerX, 14, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "§7Configure drop probabilities for any block or item", centerX, 26, 0xAAAAAA);

        // Section Labels
        graphics.drawString(this.font, "Source Block / Item ID:", centerX - 130, 42, 0xDDDDDD);
        graphics.drawString(this.font, "Target Drop Item ID:", centerX - 130, 82, 0xDDDDDD);
        graphics.drawString(this.font, "Relative Drop Weight:", centerX - 130, 122, 0xDDDDDD);

        super.render(graphics, mouseX, mouseY, partialTick);

        // Render 3D Item Stack Previews
        ItemStack srcStack = getItemStackFromId(sourceBox.getValue().trim());
        ItemStack tgtStack = getItemStackFromId(targetBox.getValue().trim());
        if (!srcStack.isEmpty()) graphics.renderFakeItem(srcStack, centerX + 54, 56);
        if (!tgtStack.isEmpty()) graphics.renderFakeItem(tgtStack, centerX + 54, 96);
    }

    private ItemStack getItemStackFromId(String idStr) {
        try {
            ResourceLocation loc = new ResourceLocation(idStr);
            Item item = BuiltInRegistries.ITEM.get(loc);
            return item != null ? new ItemStack(item) : ItemStack.EMPTY;
        } catch (Exception e) {
            return ItemStack.EMPTY;
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

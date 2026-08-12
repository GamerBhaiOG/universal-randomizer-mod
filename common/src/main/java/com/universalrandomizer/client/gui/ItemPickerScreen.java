package com.universalrandomizer.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * JEI / REI style searchable item picker screen.
 * Allows searching and selecting any item or block registered in Minecraft.
 */
public class ItemPickerScreen extends Screen {

    private final Screen parent;
    private final Consumer<String> itemConsumer;
    private final String titleText;

    private EditBox searchBox;
    private final List<Item> allItems = new ArrayList<>();
    private final List<Item> filteredItems = new ArrayList<>();

    private static final int COLS = 10;
    private static final int ROWS = 6;
    private static final int PAGE_SIZE = COLS * ROWS;

    private int currentPage = 0;

    public ItemPickerScreen(Screen parent, String titleText, Consumer<String> itemConsumer) {
        super(Component.literal(titleText));
        this.parent = parent;
        this.titleText = titleText;
        this.itemConsumer = itemConsumer;

        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR) {
                allItems.add(item);
            }
        }
        filteredItems.addAll(allItems);
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;

        // Search EditBox
        searchBox = new EditBox(this.font, centerX - 100, 30, 200, 18, Component.literal("Search..."));
        searchBox.setMaxLength(64);
        searchBox.setResponder(this::updateSearchFilter);
        this.addRenderableWidget(searchBox);

        // Previous Page Button
        this.addRenderableWidget(Button.builder(Component.literal("< Prev"), b -> {
            if (currentPage > 0) currentPage--;
        }).bounds(centerX - 110, 190, 60, 20).build());

        // Cancel Button
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> this.onClose())
            .bounds(centerX - 30, 190, 60, 20)
            .build());

        // Next Page Button
        this.addRenderableWidget(Button.builder(Component.literal("Next >"), b -> {
            int maxPage = Math.max(0, (filteredItems.size() - 1) / PAGE_SIZE);
            if (currentPage < maxPage) currentPage++;
        }).bounds(centerX + 50, 190, 60, 20).build());
    }

    private void updateSearchFilter(String query) {
        filteredItems.clear();
        String q = query.trim().toLowerCase();
        for (Item item : allItems) {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
            if (key != null) {
                String idStr = key.toString().toLowerCase();
                String nameStr = item.getDescription().getString().toLowerCase();
                if (idStr.contains(q) || nameStr.contains(q)) {
                    filteredItems.add(item);
                }
            }
        }
        currentPage = 0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        graphics.drawCenteredString(this.font, "§b§l" + titleText, centerX, 12, 0xFFFFFF);

        int maxPage = Math.max(1, (filteredItems.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        graphics.drawCenteredString(this.font, "Page " + (currentPage + 1) + " / " + maxPage + " (" + filteredItems.size() + " items)", centerX, 176, 0xDDDDDD);

        super.render(graphics, mouseX, mouseY, partialTick);

        // Render Item Grid
        int startX = centerX - (COLS * 20) / 2;
        int startY = 52;

        int startIndex = currentPage * PAGE_SIZE;
        ItemStack hoveredStack = ItemStack.EMPTY;

        for (int i = 0; i < PAGE_SIZE; i++) {
            int index = startIndex + i;
            if (index >= filteredItems.size()) break;

            Item item = filteredItems.get(index);
            int col = i % COLS;
            int row = i / COLS;

            int x = startX + col * 20;
            int y = startY + row * 20;

            // Slot Background Highlight
            boolean isHovered = mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18;
            int bgColor = isHovered ? 0x80FFFFFF : 0x40000000;
            graphics.fill(x, y, x + 18, y + 18, bgColor);

            ItemStack stack = new ItemStack(item);
            graphics.renderFakeItem(stack, x + 1, y + 1);

            if (isHovered) {
                hoveredStack = stack;
            }
        }

        // Render Hover Tooltip
        if (!hoveredStack.isEmpty()) {
            graphics.renderTooltip(this.font, hoveredStack, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            int centerX = this.width / 2;
            int startX = centerX - (COLS * 20) / 2;
            int startY = 52;

            int startIndex = currentPage * PAGE_SIZE;

            for (int i = 0; i < PAGE_SIZE; i++) {
                int index = startIndex + i;
                if (index >= filteredItems.size()) break;

                int col = i % COLS;
                int row = i / COLS;

                int x = startX + col * 20;
                int y = startY + row * 20;

                if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
                    Item selected = filteredItems.get(index);
                    ResourceLocation key = BuiltInRegistries.ITEM.getKey(selected);
                    if (key != null) {
                        itemConsumer.accept(key.toString());
                        this.onClose();
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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

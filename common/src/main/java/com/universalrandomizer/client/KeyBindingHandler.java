package com.universalrandomizer.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.universalrandomizer.client.gui.RandomizerHubScreen;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Keybinding handler for Universal Randomizer GUI.
 * Registers default keybind 'R' under "Universal Randomizer" category.
 */
public final class KeyBindingHandler {

    public static final KeyMapping OPEN_GUI_KEY = new KeyMapping(
        "key.universalrandomizer.open_gui",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "key.categories.universalrandomizer"
    );

    private KeyBindingHandler() {}

    public static void register() {
        KeyMappingRegistry.register(OPEN_GUI_KEY);

        ClientTickEvent.CLIENT_POST.register(client -> {
            while (OPEN_GUI_KEY.consumeClick()) {
                if (client.player != null && client.screen == null) {
                    client.setScreen(new RandomizerHubScreen());
                }
            }
        });
    }
}

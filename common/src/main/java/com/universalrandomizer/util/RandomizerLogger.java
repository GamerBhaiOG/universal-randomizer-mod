package com.universalrandomizer.util;

import com.universalrandomizer.UniversalRandomizerCommon;
import org.apache.logging.log4j.Logger;

/**
 * Logging utility wrapping the mod's Log4J logger and in-game debug chat notifications.
 */
public final class RandomizerLogger {

    private RandomizerLogger() {}

    private static final Logger LOGGER = UniversalRandomizerCommon.LOGGER;
    private static boolean debugEnabled = false;

    public static void setDebugEnabled(boolean enabled) { debugEnabled = enabled; }
    public static boolean isDebugEnabled()              { return debugEnabled; }

    public static void info(String msg, Object... args)  { LOGGER.info(msg, args); }
    public static void warn(String msg, Object... args)  { LOGGER.warn(msg, args); }
    public static void error(String msg, Object... args) { LOGGER.error(msg, args); }

    public static void debug(String msg, Object... args) {
        if (!debugEnabled) return;
        String formatted = format(msg, args);
        LOGGER.info("[DEBUG] {}", formatted);

        // Send live debug message to in-game chat when debug mode is enabled on client
        try {
            ClientDebugHelper.sendChatDebug(formatted);
        } catch (Throwable ignored) {
            // Environment may be server-only
        }
    }

    private static class ClientDebugHelper {
        private static void sendChatDebug(String formatted) {
            net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
            if (client != null && client.player != null) {
                client.player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[Debug] " + formatted));
            }
        }
    }

    public static void always(String msg, Object... args) { LOGGER.info(msg, args); }

    private static String format(String pattern, Object... args) {
        if (args == null || args.length == 0) return pattern;
        String result = pattern;
        for (Object arg : args) {
            result = result.replaceFirst("\\{\\}", arg != null ? arg.toString() : "null");
        }
        return result;
    }
}

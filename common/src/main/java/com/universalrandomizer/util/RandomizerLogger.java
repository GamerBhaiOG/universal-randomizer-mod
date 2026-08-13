package com.universalrandomizer.util;

import com.universalrandomizer.UniversalRandomizerCommon;
import org.apache.logging.log4j.Logger;

/**
 * Logging utility wrapping the mod's Log4J logger.
 * Completely server-safe with zero client dependencies.
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

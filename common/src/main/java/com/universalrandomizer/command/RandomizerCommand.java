package com.universalrandomizer.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.universalrandomizer.config.ModeConfig;
import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.persist.PersistenceManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

import static net.minecraft.commands.Commands.*;

/**
 * Registers the full {@code /randomizer} Brigadier command tree.
 *
 * <p>Permission model:
 * <ul>
 *   <li>{@code /randomizer status} and {@code /randomizer help} — level 0 (any player)</li>
 *   <li>All other subcommands — level 2 (OP)</li>
 * </ul>
 *
 * <pre>
 * /randomizer
 *   status              — list enabled modes (all players)
 *   help                — show command reference (all players)
 *   &lt;mode&gt; enable|disable|seed &lt;long&gt;|type &lt;RandomType&gt;   (OP)
 *   reset               — re-generate all mappings (OP)
 *   setup               — re-opens first-launch GUI (OP)
 *   debug on|off        — toggles debug logging (OP)
 *   export              — dumps mapping.json to file (OP)
 * </pre>
 */
public final class RandomizerCommand {

    private RandomizerCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Root command: open GUI directly for player
        var root = literal("randomizer").executes(ctx -> {
            com.universalrandomizer.network.NetworkHandler.sendSetupScreen(
                ctx.getSource().getPlayerOrException());
            return 1;
        });

        // ── Public: status (any player) ───────────────────────────────────────
        root.then(literal("status").executes(ctx -> {
            RandomizerManager mgr = RandomizerManager.getInstance();
            StringBuilder sb = new StringBuilder();
            sb.append("§b§l[Universal Randomizer] §r§bActive Modes:\n");
            boolean any = false;
            for (RandomizerMode mode : RandomizerMode.values()) {
                if (mgr.isEnabled(mode)) {
                    sb.append("  §a✔ ").append(mode.getDisplayName()).append("\n");
                    any = true;
                }
            }
            if (!any) {
                sb.append("  §cNo modes enabled.\n");
                sb.append("  §7Ask an admin: /randomizer <mode> enable");
            }
            sb.append(mgr.isInitialized()
                ? "\n§7(Mappings loaded ✔)"
                : "\n§c(Not initialized — load a world first)");
            String status = sb.toString();
            ctx.getSource().sendSuccess(() -> Component.literal(status), false);
            return 1;
        }));

        // ── Public: gui (open in-game dashboard) ──────────────────────────────
        root.then(literal("gui").executes(ctx -> {
            com.universalrandomizer.network.NetworkHandler.sendSetupScreen(
                ctx.getSource().getPlayerOrException());
            return 1;
        }));

        root.then(literal("enableall").requires(src -> src.hasPermission(2)).executes(ctx -> {
            RandomizerManager mgr = RandomizerManager.getInstance();
            for (RandomizerMode m : RandomizerMode.values()) {
                mgr.getConfig().setEnabled(m, true);
            }
            PersistenceManager.saveConfig(ctx.getSource().getServer(), mgr.getConfig());
            ctx.getSource().sendSuccess(() -> Component.literal("§b§l[Universal Randomizer] §aAll randomizer modes ENABLED!"), true);
            return 1;
        }));

        root.then(literal("disableall").requires(src -> src.hasPermission(2)).executes(ctx -> {
            RandomizerManager mgr = RandomizerManager.getInstance();
            for (RandomizerMode m : RandomizerMode.values()) {
                mgr.getConfig().setEnabled(m, false);
            }
            PersistenceManager.saveConfig(ctx.getSource().getServer(), mgr.getConfig());
            ctx.getSource().sendSuccess(() -> Component.literal("§b§l[Universal Randomizer] §cAll randomizer modes DISABLED!"), true);
            return 1;
        }));

        // ── Public: help (any player) ─────────────────────────────────────────
        root.then(literal("help").executes(ctx -> {
            String modes = Arrays.stream(RandomizerMode.values())
                .map(RandomizerMode::getId)
                .collect(Collectors.joining(", "));
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§b§l[Universal Randomizer] §r§7Commands:\n" +
                "§f/randomizer status §7— Show active modes (any player)\n" +
                "§f/randomizer help §7— This message (any player)\n" +
                "§c--- Admin (OP) ---\n" +
                "§f/randomizer <mode> enable|disable §7— Toggle a mode\n" +
                "§f/randomizer <mode> seed <n> §7— Set seed for a mode\n" +
                "§f/randomizer <mode> type <type> §7— Set randomization type\n" +
                "§f/randomizer reset §7— Regenerate all mappings\n" +
                "§f/randomizer debug on|off §7— Toggle debug logging\n" +
                "§f/randomizer export §7— Dump mapping to logs/\n" +
                "§7Available modes: §f" + modes
            ), false);
            return 1;
        }));

        // ── Per-mode subcommands (accessible via GUI and commands) ─────────────────
        for (RandomizerMode mode : RandomizerMode.values()) {
            final RandomizerMode capturedMode = mode;
            root.then(literal(mode.getId())
                .then(literal("enable").executes(ctx -> {
                    setEnabled(ctx.getSource(), capturedMode, true);
                    return 1;
                }))
                .then(literal("disable").executes(ctx -> {
                    setEnabled(ctx.getSource(), capturedMode, false);
                    return 1;
                }))
                .then(literal("seed")
                    .then(argument("seed", LongArgumentType.longArg()).executes(ctx -> {
                        long seed = LongArgumentType.getLong(ctx, "seed");
                        setSeed(ctx.getSource(), capturedMode, seed);
                        return 1;
                    })))
                .then(literal("type")
                    .then(argument("randomType", StringArgumentType.word()).executes(ctx -> {
                        setType(ctx.getSource(), capturedMode,
                            StringArgumentType.getString(ctx, "randomType"));
                        return 1;
                    })))
            );
        }

        // ── Admin: global commands ────────────────────────────────────────────
        root.then(literal("reset").requires(src -> src.hasPermission(2)).executes(ctx -> {
            RandomizerManager.getInstance().reset();
            PersistenceManager.save(ctx.getSource().getServer(),
                RandomizerManager.getInstance().getConfig(),
                RandomizerManager.getInstance().getTable());
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a[Universal Randomizer] All mappings reset and regenerated."), true);
            return 1;
        }));

        root.then(literal("setup").requires(src -> src.hasPermission(2)).executes(ctx -> {
            com.universalrandomizer.network.NetworkHandler.sendSetupScreen(
                ctx.getSource().getPlayerOrException());
            return 1;
        }));

        root.then(literal("debug").requires(src -> src.hasPermission(2))
            .then(literal("on").executes(ctx -> {
                RandomizerManager.getInstance().getConfig().setDebugMode(true);
                PersistenceManager.saveConfig(ctx.getSource().getServer(),
                    RandomizerManager.getInstance().getConfig());
                ctx.getSource().sendSuccess(() ->
                    Component.literal("§e[Universal Randomizer] Debug mode ON."), false);
                return 1;
            }))
            .then(literal("off").executes(ctx -> {
                RandomizerManager.getInstance().getConfig().setDebugMode(false);
                PersistenceManager.saveConfig(ctx.getSource().getServer(),
                    RandomizerManager.getInstance().getConfig());
                ctx.getSource().sendSuccess(() ->
                    Component.literal("§e[Universal Randomizer] Debug mode OFF."), false);
                return 1;
            }))
        );

        root.then(literal("export").requires(src -> src.hasPermission(2)).executes(ctx -> {
            String json = com.universalrandomizer.persist.MappingSerializer.serializeMapping(
                RandomizerManager.getInstance().getTable());
            try {
                java.nio.file.Files.writeString(
                    java.nio.file.Paths.get("logs/randomizer_export.json"), json);
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a[Universal Randomizer] Mapping exported to logs/randomizer_export.json"), false);
            } catch (java.io.IOException e) {
                ctx.getSource().sendFailure(Component.literal(
                    "§c[Universal Randomizer] Export failed: " + e.getMessage()));
            }
            return 1;
        }));

        // ── Admin: sub-command trees ──────────────────────────────────────────
        MapCommand.register(root);
        WeightCommand.register(root);
        ProfileCommand.register(root);

        dispatcher.register(root);
    }

    // ──────────────────────────────────────────────────────────────────────────

    private static void setEnabled(CommandSourceStack src, RandomizerMode mode, boolean enabled) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        mgr.getConfig().setEnabled(mode, enabled);
        PersistenceManager.saveConfig(src.getServer(), mgr.getConfig());
        if (enabled) {
            mgr.reset();
        }
        src.sendSuccess(() -> Component.literal(
            "§a[Universal Randomizer] " + mode.getDisplayName()
            + " " + (enabled ? "§aENABLED" : "§cDISABLED") + "§r."), true);
    }

    private static void setSeed(CommandSourceStack src, RandomizerMode mode, long seed) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        mgr.getConfig().getMode(mode).setSeed(seed);
        mgr.getConfig().getMode(mode).setRandomType(ModeConfig.RandomType.SEED_BASED);
        PersistenceManager.saveConfig(src.getServer(), mgr.getConfig());
        mgr.reset();
        src.sendSuccess(() -> Component.literal(
            "§a[Universal Randomizer] " + mode.getDisplayName()
            + " seed set to §f" + seed + "§a."), true);
    }

    private static void setType(CommandSourceStack src, RandomizerMode mode, String typeName) {
        try {
            ModeConfig.RandomType type = ModeConfig.RandomType.valueOf(typeName.toUpperCase());
            RandomizerManager.getInstance().getConfig().getMode(mode).setRandomType(type);
            PersistenceManager.saveConfig(src.getServer(),
                RandomizerManager.getInstance().getConfig());
            src.sendSuccess(() -> Component.literal(
                "§a[Universal Randomizer] " + mode.getDisplayName()
                + " type set to §f" + type + "§a."), true);
        } catch (IllegalArgumentException e) {
            src.sendFailure(Component.literal(
                "§c[Universal Randomizer] Unknown type: §f" + typeName
                + "§c. Valid: PURE_RANDOM, SEED_BASED, PER_PLAYER, PER_WORLD, SHARED_MULTIPLAYER"));
        }
    }
}

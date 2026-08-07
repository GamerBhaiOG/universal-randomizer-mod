package com.universalrandomizer.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.universalrandomizer.config.RandomizerConfig;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.persist.PersistenceManager;
import com.universalrandomizer.persist.ProfileManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Brigadier subcommands for profile management.
 *
 * <pre>
 * /randomizer profile save   &lt;name&gt;
 * /randomizer profile load   &lt;name&gt;
 * /randomizer profile list
 * /randomizer profile delete &lt;name&gt;
 * /randomizer profile export &lt;name&gt;
 * </pre>
 */
public final class ProfileCommand {

    private ProfileCommand() {}

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {

        var profileNode = Commands.literal("profile");

        // save
        profileNode.then(Commands.literal("save")
            .then(Commands.argument("name", StringArgumentType.string())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "name");
                    ProfileManager.save(ctx.getSource().getServer(), name,
                        RandomizerManager.getInstance().getConfig());
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "[Universal Randomizer] Profile saved: " + name), true);
                    return 1;
                }))
        );

        // load
        profileNode.then(Commands.literal("load")
            .then(Commands.argument("name", StringArgumentType.string())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "name");
                    RandomizerConfig loaded = ProfileManager.load(ctx.getSource().getServer(), name);
                    if (loaded == null) {
                        ctx.getSource().sendFailure(Component.literal(
                            "[Universal Randomizer] Profile not found: " + name));
                        return 0;
                    }
                    // Apply loaded config and regenerate
                    RandomizerManager mgr = RandomizerManager.getInstance();
                    loaded.applyWorldSeed(mgr.getConfig().getWorldSeed());
                    // Swap config reference — reset re-reads from mgr.config
                    mgr.getConfig().getModes().putAll(loaded.getModes());
                    mgr.reset();
                    PersistenceManager.save(ctx.getSource().getServer(), mgr.getConfig(), mgr.getTable());
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "[Universal Randomizer] Profile loaded: " + name), true);
                    return 1;
                }))
        );

        // list
        profileNode.then(Commands.literal("list").executes(ctx -> {
            var profiles = ProfileManager.list(ctx.getSource().getServer());
            ctx.getSource().sendSuccess(() -> Component.literal(
                "[Universal Randomizer] Profiles: " + String.join(", ", profiles)), false);
            return 1;
        }));

        // delete
        profileNode.then(Commands.literal("delete")
            .then(Commands.argument("name", StringArgumentType.string())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "name");
                    boolean deleted = ProfileManager.delete(ctx.getSource().getServer(), name);
                    if (deleted) {
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "[Universal Randomizer] Profile deleted: " + name), true);
                    } else {
                        ctx.getSource().sendFailure(Component.literal(
                            "[Universal Randomizer] Cannot delete profile: " + name
                            + " (not found or built-in)."));
                    }
                    return deleted ? 1 : 0;
                }))
        );

        // export
        profileNode.then(Commands.literal("export")
            .then(Commands.argument("name", StringArgumentType.string())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "name");
                    String json = ProfileManager.exportJson(ctx.getSource().getServer(), name);
                    if (json == null) {
                        ctx.getSource().sendFailure(Component.literal(
                            "[Universal Randomizer] Profile not found: " + name));
                        return 0;
                    }
                    // Output first 1000 chars to chat (Minecraft chat limit)
                    String preview = json.length() > 1000 ? json.substring(0, 997) + "..." : json;
                    ctx.getSource().sendSuccess(() -> Component.literal(preview), false);
                    return 1;
                }))
        );

        root.then(profileNode);
    }
}

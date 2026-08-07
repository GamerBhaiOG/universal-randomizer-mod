package com.universalrandomizer.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.persist.PersistenceManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Brigadier subcommands for fixed item/block mappings with native tab-completion.
 *
 * <pre>
 * /randomizer map block &lt;source&gt; &lt;target&gt;
 * /randomizer map item  &lt;source&gt; &lt;target&gt;
 * /randomizer unmap     &lt;source&gt;
 * </pre>
 */
public final class MapCommand {

    private MapCommand() {}

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {

        // /randomizer map block|item <source> <target>
        root.then(Commands.literal("map")
            .then(Commands.literal("block")
                .then(Commands.argument("source", ResourceLocationArgument.id())
                    .then(Commands.argument("target", ResourceLocationArgument.id())
                        .executes(ctx -> {
                            ResourceLocation source = ResourceLocationArgument.getId(ctx, "source");
                            ResourceLocation target = ResourceLocationArgument.getId(ctx, "target");
                            return doMap(ctx.getSource(), source, target, "block");
                        }))))
            .then(Commands.literal("item")
                .then(Commands.argument("source", ResourceLocationArgument.id())
                    .then(Commands.argument("target", ResourceLocationArgument.id())
                        .executes(ctx -> {
                            ResourceLocation source = ResourceLocationArgument.getId(ctx, "source");
                            ResourceLocation target = ResourceLocationArgument.getId(ctx, "target");
                            return doMap(ctx.getSource(), source, target, "item");
                        }))))
        );

        // /randomizer unmap <source>
        root.then(Commands.literal("unmap")
            .then(Commands.argument("source", ResourceLocationArgument.id())
                .executes(ctx -> {
                    ResourceLocation source = ResourceLocationArgument.getId(ctx, "source");
                    RandomizerManager.getInstance().getTable().removeCustom(source);
                    PersistenceManager.save(ctx.getSource().getServer(),
                        RandomizerManager.getInstance().getConfig(),
                        RandomizerManager.getInstance().getTable());

                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "§a[Universal Randomizer] Custom mapping removed for §f" + source), true);
                    return 1;
                }))
        );
    }

    private static int doMap(CommandSourceStack src, ResourceLocation srcRL, ResourceLocation tgtRL, String type) {
        RandomizerManager.getInstance().getTable().putCustom(srcRL, tgtRL);
        PersistenceManager.save(src.getServer(),
            RandomizerManager.getInstance().getConfig(),
            RandomizerManager.getInstance().getTable());

        src.sendSuccess(() -> Component.literal(
            "§a[Universal Randomizer] Custom " + type + " mapping added: §f" + srcRL + " §a-> §f" + tgtRL), true);
        return 1;
    }
}

package com.universalrandomizer.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.universalrandomizer.core.MappingTable;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.core.WeightedEntry;
import com.universalrandomizer.persist.PersistenceManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Brigadier subcommands for weighted drop mappings with full native tab-completion for modded items.
 *
 * <pre>
 * /randomizer weight &lt;source&gt; &lt;target&gt; &lt;weight&gt;
 * /randomizer weightclear &lt;source&gt;
 * </pre>
 */
public final class WeightCommand {

    private WeightCommand() {}

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {

        // /randomizer weight <source> <target> <weight>
        root.then(Commands.literal("weight")
            .then(Commands.argument("source", ResourceLocationArgument.id())
                .then(Commands.argument("target", ResourceLocationArgument.id())
                    .then(Commands.argument("weight", IntegerArgumentType.integer(1, 10000))
                        .executes(ctx -> {
                            ResourceLocation srcRL = ResourceLocationArgument.getId(ctx, "source");
                            ResourceLocation tgtRL = ResourceLocationArgument.getId(ctx, "target");
                            int weight = IntegerArgumentType.getInteger(ctx, "weight");

                            MappingTable table = RandomizerManager.getInstance().getTable();
                            Map<ResourceLocation, List<WeightedEntry<ResourceLocation>>> weighted = table.getWeightedMappings();
                            List<WeightedEntry<ResourceLocation>> entries = new ArrayList<>(weighted.getOrDefault(srcRL, List.of()));
                            entries.removeIf(e -> e.key().equals(tgtRL));
                            entries.add(new WeightedEntry<>(tgtRL, weight));
                            table.putWeighted(srcRL, entries);

                            PersistenceManager.save(ctx.getSource().getServer(),
                                RandomizerManager.getInstance().getConfig(), table);

                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "§a[Universal Randomizer] Weight set: §f" + srcRL + " §a-> §f" + tgtRL + " §a(weight=" + weight + ")"), true);
                            return 1;
                        }))
                )
            )
        );

        // /randomizer weightclear <source>
        root.then(Commands.literal("weightclear")
            .then(Commands.argument("source", ResourceLocationArgument.id())
                .executes(ctx -> {
                    ResourceLocation srcRL = ResourceLocationArgument.getId(ctx, "source");
                    RandomizerManager.getInstance().getTable().removeWeighted(srcRL);
                    PersistenceManager.save(ctx.getSource().getServer(),
                        RandomizerManager.getInstance().getConfig(),
                        RandomizerManager.getInstance().getTable());

                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "§a[Universal Randomizer] Weights cleared for §f" + srcRL), true);
                    return 1;
                }))
        );
    }
}

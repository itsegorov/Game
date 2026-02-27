package me.egorov.plugin.game.module.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.egorov.plugin.library.command.commander.AbstractCommand;
import me.egorov.plugin.library.rank.model.Rank;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RankCommand extends AbstractCommand {

    public RankCommand() {
        super("rank", "Тестовая команда рангов", "group");
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return withStandardRequirements(LiteralArgumentBuilder.<CommandSourceStack>literal(getName())

                // /rank
                .executes(context -> {
                    if (getOptionalPlayer(context).isEmpty()) {
                        return 0;
                    }

                    Player player = getOptionalPlayer(context).orElseThrow();
                    Optional<Rank> playerRank = getPlayerRank(player);

                    if (playerRank.isEmpty()) {
                        error(context, "❌ У вас нет ранга!");
                        return 0;
                    }

                    Rank rank = playerRank.get();

                    success(context, rank.name() + " - " + rank.prefix());
                    return 1;
                })

                // /rank set
                .then(literal("set")
                        // /rank set <rank>
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("ранг", StringArgumentType.word())
                                .suggests(this::suggestRanks)
                                .executes(context -> {
                                    if (getOptionalPlayer(context).isEmpty()) {
                                        return 0;
                                    }

                                    Player player = getOptionalPlayer(context).orElseThrow();
                                    String rankID = getString(context, "ранг");

                                    Optional<Rank> rank = getRankById(rankID);
                                    if (rank.isEmpty()) {
                                        error(context, "Ранг '" + rankID + "' не найден");
                                        return 0;
                                    }

                                    Rank targetRank = rank.get();

                                    rankManager.getRankService().updateRank(player, targetRank, null);
                                    success(context, "New rank: " + targetRank.name() + " - " + targetRank.prefix());
                                    return 1;
                                })
                        )
                )
                // /rank remove
                .then(literal("remove")
                        // /rank remove <rank>
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("ранг", StringArgumentType.word())
                                .suggests(this::suggestRanks)
                                .executes(context -> {
                                    if (getOptionalPlayer(context).isEmpty()) {
                                        return 0;
                                    }

                                    Player player = getOptionalPlayer(context).orElseThrow();
                                    String rankID = getString(context, "ранг");

                                    Optional<Rank> rank = getRankById(rankID);
                                    if (rank.isEmpty()) {
                                        error(context, "Ранг '" + rankID + "' не найден");
                                        return 0;
                                    }

                                    Rank targetRank = rank.get();

                                    rankManager.getRankService().removeRank(player, targetRank);
                                    success(context, "New rank: " + targetRank.name() + " - " + targetRank.prefix());
                                    return 1;
                                })
                        )
                )
        );
    }

    private CompletableFuture<Suggestions> suggestRanks(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder) {

        String partial = builder.getRemaining().toLowerCase();

        java.util.Arrays.stream(Rank.values())
                .map(Rank::id)
                .filter(id -> id.startsWith(partial))
                .forEach(builder::suggest);

        return builder.buildFuture();
    }

    private Optional<Rank> getRankById(String id) {
        return java.util.Arrays.stream(Rank.values())
                .filter(r -> r.id().equalsIgnoreCase(id))
                .findFirst();
    }
}

package me.egorov.plugin.library.command.commander;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.egorov.plugin.library.command.cooldown.CooldownManager;
import me.egorov.plugin.library.command.decorator.CommandDecorator;
import me.egorov.plugin.library.economy.model.Currency;
import me.egorov.plugin.library.rank.model.Rank;
import me.egorov.plugin.library.rank.wrapper.RankManagerWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public abstract class AbstractCommand implements CommanderCommand {

    protected final String name;
    protected final String description;
    protected final Set<String> aliases;
    protected final List<CommandDecorator> decorators = new CopyOnWriteArrayList<>();
    protected JavaPlugin plugin;
    protected CooldownManager cooldownManager;
    protected RankManagerWrapper rankManager;
    protected long cooldownSeconds = 0;
    protected String permission;
    protected boolean playerOnly = false;

    public static final List<Currency> currencies = List.of(
            new Currency("money", "$", "Обычная валюта"),
            new Currency("donate", "^", "Донатная валюта")
    );

    protected static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    public AbstractCommand(String name, String description, String... aliases) {
        this.name = name;
        this.description = description;
        this.aliases = ConcurrentHashMap.newKeySet();
        this.aliases.addAll(Arrays.asList(aliases));
    }

    public void init(JavaPlugin plugin, CooldownManager cooldownManager, RankManagerWrapper rankManager) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
        this.rankManager = rankManager;
    }

    public AbstractCommand withPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public AbstractCommand playerOnly() {
        this.playerOnly = true;
        return this;
    }

    public AbstractCommand withCooldown(long seconds) {
        this.cooldownSeconds = seconds;
        return this;
    }

    public synchronized AbstractCommand withDecorator(CommandDecorator decorator) {
        this.decorators.add(decorator);
        return this;
    }

    public synchronized AbstractCommand withAlias(String alias) {
        this.aliases.add(alias);
        return this;
    }

    protected RequiredArgumentBuilder<CommandSourceStack, String> argument(String name, StringArgumentType type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected RequiredArgumentBuilder<CommandSourceStack, Integer> argument(String name, IntegerArgumentType type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected RequiredArgumentBuilder<CommandSourceStack, Boolean> argument(String name, BoolArgumentType type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected RequiredArgumentBuilder<CommandSourceStack, Float> argument(String name, FloatArgumentType type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected LiteralArgumentBuilder<CommandSourceStack> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    protected Predicate<CommandSourceStack> permission(String perm) {
        return source -> source.getSender().hasPermission(perm);
    }

    protected Predicate<CommandSourceStack> anyPermission(String... perms) {
        return source -> {
            CommandSender sender = source.getSender();
            for (String perm : perms) {
                if (sender.hasPermission(perm)) {
                    return true;
                }
            }
            return false;
        };
    }

    protected Predicate<CommandSourceStack> allPermissions(String... perms) {
        return source -> {
            CommandSender sender = source.getSender();
            for (String perm : perms) {
                if (!sender.hasPermission(perm)) {
                    return false;
                }
            }
            return true;
        };
    }

    protected Predicate<CommandSourceStack> playerOnlyPredicate() {
        return source -> {
            if (source.getSender() instanceof Player) return true;
            return false;
        };
    }

    protected Predicate<CommandSourceStack> consoleOnlyPredicate() {
        return source -> {
            if (!(source.getSender() instanceof Player)) return true;
            return false;
        };
    }

    protected Predicate<CommandSourceStack> cooldownPredicate() {
        return source -> {
            if (!(source.getSender() instanceof Player player) || cooldownSeconds <= 0) {
                return true;
            }

            if (permission != null && !permission.isEmpty() && !player.hasPermission(permission)) {
                return false;
            }

            CooldownManager.CooldownResult result = cooldownManager.checkCooldown(
                    player, name, cooldownSeconds * 1000, permission
            );

            return result.isAllowed();
        };
    }

    protected boolean checkCooldown(Player player, CommandContext<CommandSourceStack> context) {
        if (cooldownSeconds <= 0) return true;

        CooldownManager.CooldownResult result = cooldownManager.checkCooldown(
                player,
                name,
                cooldownSeconds * 1000,
                permission
        );

        if (!result.isAllowed()) {
            player.sendMessage(result.getErrorMessage());
            return false;
        }
        return true;
    }

    protected Predicate<CommandSourceStack> isOp() {
        return source -> source.getSender().isOp();
    }


    protected Predicate<CommandSourceStack> hasPermission(String permission) {
        return source -> {
            if (!(source.getSender() instanceof Player player)) {
                return source.getSender().hasPermission(permission);
            }

            if (player.hasPermission(permission)) {
                return true;
            }

            player.sendMessage(Component.text()
                    .append(Component.text("❌ Требуется право: ", NamedTextColor.RED))
                    .append(Component.text(permission, NamedTextColor.YELLOW))
                    .build()
            );
            return false;
        };
    }

    protected Predicate<CommandSourceStack> hasRank(String rankId) {
        return source -> {
            if (!rankManager.isEnabled()) {
                return false;
            }

            if (!(source.getSender() instanceof Player player)) {
                return false;
            }

            return rankManager.isInGroup(player, rankId);
        };
    }

    protected Predicate<CommandSourceStack> hasAnyRank(String... rankIds) {
        return source -> {
            if (!rankManager.isEnabled()) {
                return false;
            }

            if (!(source.getSender() instanceof Player player)) {
                return false;
            }

            return rankManager.isInAnyGroup(player, rankIds);
        };
    }

    protected Predicate<CommandSourceStack> hasRankAtLeast(String rankId) {
        return source -> {
            if (!rankManager.isEnabled()) {
                return false;
            }

            if (!(source.getSender() instanceof Player player)) {
                return false;
            }

            Optional<Rank> playerRank = rankManager.getPlayerRank(player);
            Optional<Rank> requiredRank = getRankById(rankId);

            if (playerRank.isEmpty() || requiredRank.isEmpty()) {
                return false;
            }

            return playerRank.get().isAtLeast(requiredRank.get());
        };
    }

    protected Predicate<CommandSourceStack> hasRankAbove(String rankId) {
        return source -> {
            if (!rankManager.isEnabled()) {
                return false;
            }

            if (!(source.getSender() instanceof Player player)) {
                return false;
            }

            Optional<Rank> playerRank = rankManager.getPlayerRank(player);
            Optional<Rank> requiredRank = getRankById(rankId);

            if (playerRank.isEmpty() || requiredRank.isEmpty()) {
                return false;
            }

            return playerRank.get().isAbove(requiredRank.get());
        };
    }

    protected Predicate<CommandSourceStack> allExcept(String... excludedGroups) {
        return source -> {
            if (!(source.getSender() instanceof Player player)) {
                return false;
            }

            if (hasAnyRank(player, excludedGroups)) {
                return false;
            }
            return true;
        };
    }

    protected Predicate<CommandSourceStack> allExceptAtLeast(String minRank, String... excludedGroups) {
        return source -> {
            if (!(source.getSender() instanceof Player player)) {
                return false;
            }

            if (hasAnyRank(player, excludedGroups)) {
                return false;
            }

            return hasRankAtLeast(player, minRank);
        };
    }

    private Optional<Rank> getRankById(String rankId) {
        return Arrays.stream(Rank.values())
                .filter(r -> r.id().equalsIgnoreCase(rankId))
                .findFirst();
    }

    protected Player getPlayer(CommandContext<CommandSourceStack> context, String argName) {
        return context.getArgument(argName, Player.class);
    }

    protected World getWorld(CommandContext<CommandSourceStack> context, String argName) {
        return context.getArgument(argName, World.class);
    }

    protected Location getLocation(CommandContext<CommandSourceStack> context, String argName) {
        return context.getArgument(argName, Location.class);
    }

    protected String getString(CommandContext<CommandSourceStack> context, String argName) {
        return context.getArgument(argName, String.class);
    }

    protected int getInteger(CommandContext<CommandSourceStack> context, String argName) {
        return context.getArgument(argName, Integer.class);
    }

    protected boolean getBoolean(CommandContext<CommandSourceStack> context, String argName) {
        return context.getArgument(argName, Boolean.class);
    }

    protected float getFloat(CommandContext<CommandSourceStack> context, String argName) {
        return context.getArgument(argName, Float.class);
    }

    protected Optional<Player> getOptionalPlayer(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        return sender instanceof Player ? Optional.of((Player) sender) : Optional.empty();
    }

    protected SuggestionProvider<CommandSourceStack> suggestPlayers() {
        return (ctx, builder) -> {
            Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
                    .forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

    protected SuggestionProvider<CommandSourceStack> suggestCurrencies() {
        return (ctx, builder) -> {
            String partial = builder.getRemaining().toLowerCase();
            currencies.stream()
                    .map(Currency::getId)
                    .filter(id -> id.startsWith(partial))
                    .forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

    protected SuggestionProvider<CommandSourceStack> suggestRanks() {
        return (ctx, builder) -> {
            String partial = builder.getRemaining().toLowerCase();
            Arrays.stream(Rank.values())
                    .map(Rank::id)
                    .filter(id -> id.toLowerCase().startsWith(partial))
                    .forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

    private CompletableFuture<Suggestions> fancyPlayerSuggestions(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder) {

        Bukkit.getOnlinePlayers().forEach(player -> {
            String name = player.getName();
            if (name.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                String prefix = player.isOp() ? "⚡ " : "👤 ";
                Component tooltip = Component.text()
                        .append(Component.text("Здоровье: ", NamedTextColor.RED))
                        .append(Component.text((int)player.getHealth() + "/" + (int)player.getMaxHealth()))
                        .append(Component.text("\nМир: ", NamedTextColor.GREEN))
                        .append(Component.text(player.getWorld().getName()))
                        .build();

                builder.suggest(prefix + name, (Message) tooltip);
            }
        });

        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> sortedPlayerSuggestions(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder) {

        String partial = builder.getRemaining().toLowerCase();

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getName().toLowerCase().startsWith(partial))
                .sorted((p1, p2) -> {
                    if (p1.isOp() && !p2.isOp()) return -1;
                    if (!p1.isOp() && p2.isOp()) return 1;
                    return p1.getName().compareTo(p2.getName());
                })
                .map(Player::getName)
                .forEach(builder::suggest);

        return builder.buildFuture();
    }

    protected SuggestionProvider<CommandSourceStack> suggestWorlds() {
        return (ctx, builder) -> {
            Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .filter(name -> name.toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
                    .forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

    protected SuggestionProvider<CommandSourceStack> suggestFromList(String... values) {
        return (ctx, builder) -> {
            Arrays.stream(values)
                    .filter(v -> v.toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
                    .forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

    protected void sendMessage(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().getSender().sendMessage(Component.text(message));
    }

    protected void sendMessage(CommandContext<CommandSourceStack> context, Component message) {
        context.getSource().getSender().sendMessage(message);
    }

    protected void sendMessage(Player player, String message) {
        player.sendMessage(Component.text(message));
    }

    protected void sendMessage(Player player, Component message) {
        player.sendMessage(message);
    }

    protected void sendLines(CommandContext<CommandSourceStack> context, String... lines) {
        CommandSender sender = context.getSource().getSender();
        for (String line : lines) {
            sender.sendMessage(Component.text(line));
        }
    }

    protected void sendLines(CommandContext<CommandSourceStack> context, Component... lines) {
        CommandSender sender = context.getSource().getSender();
        for (Component line : lines) {
            sender.sendMessage(line);
        }
    }

    protected void sendLines(Player player, String... lines) {
        for (String line : lines) {
            player.sendMessage(Component.text(line));
        }
    }

    protected void sendLines(Player player, Component... lines) {
        for (Component line : lines) {
            player.sendMessage(line);
        }
    }

    protected void sendLines(CommandContext<CommandSourceStack> context, List<Component> lines) {
        CommandSender sender = context.getSource().getSender();
        for (Component line : lines) {
            sender.sendMessage(line);
        }
    }

    protected void success(CommandContext<CommandSourceStack> context, String message) {
        sendMessage(context, Component.text("✅ " + message, NamedTextColor.GREEN));
    }

    protected void error(CommandContext<CommandSourceStack> context, String message) {
        sendMessage(context, Component.text("❌ " + message, NamedTextColor.RED));
    }

    protected void warn(CommandContext<CommandSourceStack> context, String message) {
        sendMessage(context, Component.text("⚠ " + message, NamedTextColor.YELLOW));
    }

    protected void info(CommandContext<CommandSourceStack> context, String message) {
        sendMessage(context, Component.text("ℹ " + message, NamedTextColor.GREEN));
    }

    protected Optional<Rank> getPlayerRank(Player player) {
        return rankManager.getPlayerRank(player);
    }

    protected String getPlayerPrefix(Player player) {
        return rankManager.getPlayerPrefix(player).orElse("");
    }

    protected boolean hasRank(Player player, String rankId) {
        return rankManager.isInGroup(player, rankId);
    }

    protected boolean hasAnyRank(Player player, String... rankIds) {
        return rankManager.isInAnyGroup(player, rankIds);
    }

    protected boolean hasRankAtLeast(Player player, String rankId) {
        Optional<Rank> playerRank = rankManager.getPlayerRank(player);
        Optional<Rank> requiredRank = getRankById(rankId);

        return playerRank.isPresent() && requiredRank.isPresent() &&
                playerRank.get().isAtLeast(requiredRank.get());
    }

    private String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String[] getAliases() {
        return aliases.toArray(new String[0]);
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public boolean isPlayerOnly() {
        return playerOnly;
    }

    @Override
    public long getCooldown() {
        return cooldownSeconds;
    }

    protected LiteralArgumentBuilder<CommandSourceStack> applyDecorators(
            LiteralArgumentBuilder<CommandSourceStack> builder) {

        LiteralArgumentBuilder<CommandSourceStack> result = builder;
        for (CommandDecorator decorator : decorators) {
            result = decorator.decorate(result);
        }
        return result;
    }

    protected LiteralArgumentBuilder<CommandSourceStack> withStandardRequirements(
            LiteralArgumentBuilder<CommandSourceStack> builder) {

        LiteralArgumentBuilder<CommandSourceStack> result = builder;

        if (playerOnly) {
            result = result.requires(playerOnlyPredicate());
        }

        if (permission != null && !permission.isEmpty()) {
            result = result.requires(permission(permission));
        }

        if (cooldownSeconds > 0) {
            result = result.requires(cooldownPredicate());
        }

        return applyDecorators(result);
    }

    public void setRankManager(RankManagerWrapper rankManager) {
        this.rankManager = rankManager;
    }
}

package me.egorov.plugin.library.command.registrar;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.egorov.plugin.library.command.commander.AbstractCommand;
import me.egorov.plugin.library.command.cooldown.CooldownManager;
import me.egorov.plugin.library.command.decorator.ConfirmationDecorator;
import me.egorov.plugin.library.database.HikariConnectionPool;
import me.egorov.plugin.library.database.type.SQLiteConnectionPool;
import me.egorov.plugin.library.rank.service.PlayerRankService;
import me.egorov.plugin.library.rank.wrapper.RankManagerWrapper;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class CommandRegistrar {

    private final JavaPlugin plugin;
    private final CooldownManager cooldownManager;
    private final ConfirmationDecorator confirmationDecorator;

    private final Map<String, AbstractCommand> commands = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> redirects = new ConcurrentHashMap<>();

    private RankManagerWrapper rankManager;

    private CommandDispatcher<CommandSourceStack> dispatcher;
    private boolean registered = false;

    public CommandRegistrar(JavaPlugin plugin, PlayerRankService rankService) {
        this.plugin = plugin;
        this.cooldownManager = new CooldownManager(plugin);
        this.confirmationDecorator = new ConfirmationDecorator(plugin);

        if (rankService != null) {
            this.rankManager = new RankManagerWrapper(rankService);
        } else {
            this.rankManager = null;
        }
    }

    public CommandRegistrar register(AbstractCommand command) {
        if (registered) {
            plugin.getLogger().warning("⚠ Команда " + command.getName() + " регистрируется после регистрации! Это может вызвать проблемы.");
        }

        command.init(plugin, cooldownManager, rankManager);
        commands.put(command.getName().toLowerCase(), command);

        if (command.getCooldown() > 0) {
            cooldownManager.setDefaultCooldown(command.getName(), command.getCooldown());
        }
        return this;
    }

    public CommandRegistrar register(AbstractCommand... commands) {
        for (AbstractCommand cmd : commands) {
            register(cmd);
        }
        return this;
    }

    public CommandRegistrar addRedirect(String from, String to) {
        if (registered) {
            plugin.getLogger().warning("⚠ Редирект " + from + " -> " + to + " добавляется после регистрации!");
        }

        redirects.computeIfAbsent(from.toLowerCase(), k ->
                ConcurrentHashMap.newKeySet()
        ).add(to.toLowerCase());

        return this;
    }

    public void registerAll() {
        if (registered) {
            plugin.getLogger().warning("⚠ Команды уже зарегистрированы! Повторная регистрация отменена.");
            return;
        }

        LifecycleEventManager<?> manager = plugin.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands registrar = event.registrar();
            this.dispatcher = registrar.getDispatcher();

            registerAllCommands(registrar);
            registerAllRedirects(registrar);
        });

        plugin.getServer().getScheduler().runTaskTimer(plugin,
                ConfirmationDecorator::cleanExpired, 1200L, 1200L);

        registered = true;
    }

    private void registerAllCommands(Commands registrar) {
        List<AbstractCommand> commandsCopy = new ArrayList<>(commands.values());

        for (AbstractCommand command : commandsCopy) {
            try {
                LiteralArgumentBuilder<CommandSourceStack> builder = command.build();
                LiteralCommandNode<CommandSourceStack> node = builder.build();

                registrar.register(
                        node,
                        command.getDescription(),
                        Arrays.asList(command.getAliases())
                );

                plugin.getLogger().info("✅ Зарегистрирована команда: /" + command.getName() + " -> " + command.getDescription());
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE,
                        "❌ Ошибка регистрации " + command.getName(), e);
            }
        }
    }

    private void registerAllRedirects(Commands registrar) {
        Map<String, Set<String>> redirectsCopy = new HashMap<>(redirects);

        for (Map.Entry<String, Set<String>> entry : redirectsCopy.entrySet()) {
            String from = entry.getKey();
            Set<String> toSet = entry.getValue();

            for (String to : new ArrayList<>(toSet)) {
                if (commands.containsKey(to)) {
                    try {
                        LiteralCommandNode<CommandSourceStack> redirectNode =
                                Commands.literal(from)
                                        .executes(ctx -> {
                                            String input = ctx.getInput();
                                            String remaining = input.substring(from.length()).trim();
                                            String newCommand = to + (remaining.isEmpty() ? "" : " " + remaining);
                                            return dispatcher.execute(newCommand, ctx.getSource());
                                        })
                                        .build();

                        registrar.register(
                                redirectNode,
                                "Алиас для /" + to,
                                Collections.emptyList()
                        );

                        plugin.getLogger().info("🔄 Редирект: /" + from + " -> /" + to);
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING,
                                "❌ Ошибка создания редиректа /" + from + " -> /" + to, e);
                    }
                }
            }
        }
    }

    public Collection<AbstractCommand> getCommands() {
        return new ArrayList<>(commands.values());
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public ConfirmationDecorator getConfirmationDecorator() {
        return confirmationDecorator;
    }

    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return dispatcher;
    }

    public RankManagerWrapper getRankManager() {return rankManager;}
}

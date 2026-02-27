package me.egorov.plugin.library.command.decorator;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ConfirmationDecorator implements CommandDecorator {

    private static final Map<UUID, PendingCommand> PENDING_COMMANDS = new HashMap<>();
    private static final long CONFIRMATION_TIMEOUT = TimeUnit.SECONDS.toMillis(30);

    private final JavaPlugin plugin;
    private final String confirmCommand;

    public ConfirmationDecorator(JavaPlugin plugin) {
        this(plugin, "confirm");
    }

    public ConfirmationDecorator(JavaPlugin plugin, String confirmCommand) {
        this.plugin = plugin;
        this.confirmCommand = confirmCommand;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> decorate(LiteralArgumentBuilder<CommandSourceStack> builder) {
        var originalCommand = builder.getCommand();

        if (originalCommand == null) {
            return builder;
        }

        builder.executes(context -> {
            if (!(context.getSource().getSender() instanceof Player player)) {
                context.getSource().getSender().sendMessage(
                        Component.text("❌ Подтверждение доступно только игрокам!", NamedTextColor.RED)
                );
                return 0;
            }

            PendingCommand pending = new PendingCommand(
                    player.getUniqueId(),
                    context.getInput(),
                    originalCommand,
                    System.currentTimeMillis() + CONFIRMATION_TIMEOUT
            );

            PENDING_COMMANDS.put(player.getUniqueId(), pending);
            sendConfirmationMessage(player, context.getInput());

            return 1;
        });

        return builder;
    }

    public int handleConfirm(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!(context.getSource().getSender() instanceof Player player)) {
            context.getSource().getSender().sendMessage(
                    Component.text("❌ Только игроки могут подтверждать команды!", NamedTextColor.RED)
            );
            return 0;
        }

        PendingCommand pending = PENDING_COMMANDS.remove(player.getUniqueId());

        if (pending == null) {
            player.sendMessage(Component.text("❌ Нет команды, ожидающей подтверждения!", NamedTextColor.RED));
            return 0;
        }

        if (System.currentTimeMillis() > pending.expiryTime) {
            player.sendMessage(Component.text("❌ Время подтверждения истекло!", NamedTextColor.RED));
            return 0;
        }

        try {
            int result = pending.command.run(context);
            player.sendMessage(Component.text("✅ Команда подтверждена и выполнена!", NamedTextColor.GREEN));
            plugin.getLogger().info(String.format(
                    "[CONFIRM] %s подтвердил: %s",
                    player.getName(), pending.originalInput
            ));
            return result;
        } catch (Exception e) {
            player.sendMessage(Component.text("❌ Ошибка при выполнении команды!", NamedTextColor.RED));
            return 0;
        }
    }

    private void sendConfirmationMessage(Player player, String command) {
        player.sendMessage(Component.empty());
        player.sendMessage(
                Component.text("⚠ ВНИМАНИЕ! ", NamedTextColor.RED, TextDecoration.BOLD)
                        .append(Component.text("Команда требует подтверждения", NamedTextColor.YELLOW))
        );
        player.sendMessage(
                Component.text("└ Команда: ", NamedTextColor.GRAY)
                        .append(Component.text(command, NamedTextColor.WHITE))
        );
        player.sendMessage(
                Component.text("└ Подтвердите: ", NamedTextColor.GRAY)
                        .append(Component.text("/" + confirmCommand, NamedTextColor.GREEN, TextDecoration.BOLD))
        );
        player.sendMessage(
                Component.text("└ Время: ", NamedTextColor.GRAY)
                        .append(Component.text("30 секунд", NamedTextColor.AQUA))
        );
        player.sendMessage(Component.empty());
    }

    public static void cleanExpired() {
        long now = System.currentTimeMillis();
        PENDING_COMMANDS.entrySet().removeIf(entry -> now > entry.getValue().expiryTime);
    }

    private static class PendingCommand {
        private final UUID playerId;
        private final String originalInput;
        private final com.mojang.brigadier.Command<CommandSourceStack> command;
        private final long expiryTime;

        public PendingCommand(UUID playerId, String originalInput,
                              com.mojang.brigadier.Command<CommandSourceStack> command,
                              long expiryTime) {
            this.playerId = playerId;
            this.originalInput = originalInput;
            this.command = command;
            this.expiryTime = expiryTime;
        }
    }
}

package me.egorov.plugin.game.module.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.egorov.plugin.library.command.argument.ArgumentTypes;
import me.egorov.plugin.library.command.commander.AbstractCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class GameModeCommand extends AbstractCommand {

    private static final Map<String, GameMode> MODES = new HashMap<>();

    static {
        MODES.put("0", GameMode.SURVIVAL);
        MODES.put("1", GameMode.CREATIVE);
        MODES.put("2", GameMode.ADVENTURE);
        MODES.put("3", GameMode.SPECTATOR);
        MODES.put("survival", GameMode.SURVIVAL);
        MODES.put("creative", GameMode.CREATIVE);
        MODES.put("adventure", GameMode.ADVENTURE);
        MODES.put("spectator", GameMode.SPECTATOR);
    }

    public GameModeCommand() {
        super("gm", "Меняет режим игры", "gamemode");
        withPermission("fourcube.command.gamemode");
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return withStandardRequirements(LiteralArgumentBuilder.<CommandSourceStack>literal(getName())
                .requires(hasPermission("fourcube.command.gamemode"))
                .executes(this::sendUsage)
                .then(argument("режим", StringArgumentType.word())
                        .suggests(suggestFromList("survival", "creative", "adventure", "spectator", "s", "c", "a", "sp"))
                        .executes(context -> {
                            Player player = getOptionalPlayer(context).orElseThrow();
                            String modeStr = getString(context, "режим");

                            GameMode mode = MODES.get(modeStr.toLowerCase());

                            if (mode == null) {
                                error(context, "Неизвестный режим игры.");
                                return 0;
                            }

                            player.setGameMode(mode);
                            success(context, "Режим изменён на " + mode.name().toLowerCase());
                            return 1;
                        })
                        .then(RequiredArgumentBuilder.<CommandSourceStack, Player> argument("игрок", ArgumentTypes.onlinePlayer())
                                .suggests(suggestPlayers())
                                .requires(anyPermission("fourcube.command.gamemode.other", "fourcube.command.gamemode.*"))
                                .executes(context -> {
                                    Player target = getPlayer(context, "игрок");
                                    String modeStr = getString(context, "режим");

                                    if (target == null) {
                                        error(context, "Игрок не найден");
                                        return 0;
                                    }
                                    GameMode mode = MODES.get(modeStr.toLowerCase());

                                    if (mode == null) {
                                        error(context, "Неизвестный режим");
                                        return 0;
                                    }

                                    target.setGameMode(mode);
                                    success(context, "Режим " + target.getName() + " изменён на " + mode.name().toLowerCase());
                                    target.sendMessage(Component.text("✏ Ваш режим игры изменён на " + mode.name().toLowerCase(), NamedTextColor.YELLOW));
                                    return 1;
                                })
                        )
                )
        );
    }

    private int sendUsage(CommandContext<CommandSourceStack> context) {
        Component usage = Component.text()
                .append(Component.text("Использование команды /gm:\n", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("  /gm <режим>", NamedTextColor.YELLOW))
                .append(Component.text(" - изменить свой режим\n", NamedTextColor.GRAY))
                .append(Component.text("  /gm <режим> <игрок>", NamedTextColor.YELLOW))
                .append(Component.text(" - изменить режим игрока\n", NamedTextColor.GRAY))
                .append(Component.text("\nДоступные режимы: ", NamedTextColor.GREEN))
                .append(Component.text("survival (s/0), creative (c/1), adventure (a/2), spectator (sp/3)", NamedTextColor.AQUA))
                .build();

        context.getSource().getSender().sendMessage(usage);
        return 1;
    }
}

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
    }

    public GameModeCommand() {
        super("gm", "Меняет режим игры", "gamemode");
        withPermission("command.gamemode");
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return withStandardRequirements(LiteralArgumentBuilder.<CommandSourceStack>literal(getName())
                .requires(hasPermission("command.gamemode"))
                .then(argument("режим", StringArgumentType.word())
                        .suggests(suggestFromList( "s", "c", "a", "sp"))
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
                                .requires(anyPermission("command.gamemode.other", "command.gamemode.*"))
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
}

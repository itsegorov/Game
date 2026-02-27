package me.egorov.plugin.library.command.commander;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public interface CommanderCommand {

    String getName();

    String getDescription();

    String[] getAliases();

    String getPermission();

    boolean isPlayerOnly();

    long getCooldown();

    LiteralArgumentBuilder<CommandSourceStack> build();
}

package me.egorov.plugin.library.command.decorator;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public interface CommandDecorator {

    LiteralArgumentBuilder<CommandSourceStack> decorate(LiteralArgumentBuilder<CommandSourceStack> builder);

}
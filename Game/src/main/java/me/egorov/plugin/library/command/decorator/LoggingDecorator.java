package me.egorov.plugin.library.command.decorator;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;

public class LoggingDecorator implements CommandDecorator {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> decorate(LiteralArgumentBuilder<CommandSourceStack> builder) {
        var originalExecutes = builder.getCommand();
        if (originalExecutes != null) {
            builder.executes(context -> {
                long start = System.currentTimeMillis();
                try {
                    int result = originalExecutes.run(context);
                    long time = System.currentTimeMillis() - start;
                    Bukkit.getLogger().info(String.format(
                            "[Logger/CMD] %s выполнил /%s за %dms",
                            context.getSource().getSender().getName(),
                            context.getInput(),
                            time
                    ));
                    return result;
                } catch (Exception e) {
                    Bukkit.getLogger().warning(String.format(
                            "[Logger/CMD] Ошибка у %s: %s",
                            context.getSource().getSender().getName(),
                            e.getMessage()
                    ));
                    throw e;
                }
            });
        }
        return builder;
    }

}

package me.egorov.plugin.library.command.decorator;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class AsyncDecorator implements CommandDecorator {

    private final JavaPlugin plugin;

    public AsyncDecorator(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> decorate(LiteralArgumentBuilder<CommandSourceStack> builder) {
        var originalExecutes = builder.getCommand();
        if (originalExecutes != null) {
            builder.executes(context -> {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        originalExecutes.run(context);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                return 1;
            });
        }
        return builder;
    }

}

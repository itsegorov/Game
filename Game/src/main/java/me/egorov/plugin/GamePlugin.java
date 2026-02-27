package me.egorov.plugin;

import me.egorov.plugin.game.module.chat.ChatListener;
import me.egorov.plugin.game.module.command.*;
import me.egorov.plugin.library.command.registrar.CommandRegistrar;
import me.egorov.plugin.library.configuration.Messages;
import me.egorov.plugin.library.economy.Economy;
import me.egorov.plugin.library.economy.listener.EconomyPlayerListener;
import me.egorov.plugin.library.inventory.listener.MenuListener;
import me.egorov.plugin.library.inventory.session.MenuSession;
import me.egorov.plugin.library.inventory.session.PaginatedSession;
import me.egorov.plugin.library.rank.Ranks;
import me.egorov.plugin.library.rank.listener.RanksPlayerListener;
import me.egorov.plugin.library.rank.service.PlayerRankService;
import me.egorov.plugin.library.rank.wrapper.RankManagerWrapper;
import org.bukkit.plugin.java.JavaPlugin;

public class GamePlugin extends JavaPlugin {

    private static GamePlugin instance;

    private CommandRegistrar commandRegistrar;
    private static Messages messages;

    private Ranks ranks;
    private RankManagerWrapper rankManager;

    private Economy economy;

    // sender.sendMessage("[Служебное сообщение] Такого игрока нет! (OfflinePlayer#" + targetName + ")");
    // sender.sendMessage("[Служебное сообщение] Успешно! (OfflinePlayer#" + targetName + ", IncrementBalance=" + type + ", Value=" + value + ")");
    // sender.sendMessage("[Служебное сообщение] Успешно! (OfflinePlayer#" + targetName + ", DecrementBalance=" + type + ", Value=" + value + ")");

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        economy = new Economy(this);

        initializeRanks();
        PlayerRankService rankService = ranks != null ? ranks.service() : null;
        this.rankManager = new RankManagerWrapper(rankService);

        messages = new Messages(getConfig().getConfigurationSection("Messages"));

        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(rankManager), this);
        getServer().getPluginManager().registerEvents(new RanksPlayerListener(ranks.service()), this);
        getServer().getPluginManager().registerEvents(new EconomyPlayerListener(economy.service()), this);

        commandRegistrar = new CommandRegistrar(this, rankService);
        commandRegistrar.register(
                new GameModeCommand(),
                new EconomyCommand(economy.service()),
                new RankCommand()
        );
        commandRegistrar.registerAll();
    }

    private void initializeRanks() {
        try {
            this.ranks = new Ranks(this);
        } catch (Exception e) {
            e.printStackTrace();
            this.ranks = null;
        }
    }

    @Override
    public void onDisable() {
        MenuSession.clearAll();
        PaginatedSession.clearAll();
        if (ranks != null) {
            ranks.shutdown();
        }
        if (economy != null) {
            economy.shutdown();
        }
    }

    public static GamePlugin instance() {return instance;}

    public static Messages messages() {return messages;}
}

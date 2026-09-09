package me.egorov.plugin.library.economy;

import me.egorov.plugin.library.configuration.SimpleConfiguration;
import me.egorov.plugin.library.database.HikariConnectionPool;
import me.egorov.plugin.library.database.type.SQLiteConnectionPool;
import me.egorov.plugin.library.economy.repository.EconomyPlayerRepository;
import me.egorov.plugin.library.economy.repository.PlayerBalanceRepository;
import me.egorov.plugin.library.economy.repository.impl.EconomyPlayerRepositoryImpl;
import me.egorov.plugin.library.economy.repository.impl.PlayerBalanceRepositoryImpl;
import me.egorov.plugin.library.economy.service.EconomyService;
import me.egorov.plugin.library.economy.service.impl.EconomyServiceImpl;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Economy {

    private final HikariConnectionPool connectionPool;
    private final @NotNull EconomyService economyService;

    public Economy(@NotNull JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "Plugin cannot be null");

        SimpleConfiguration economyConfig = new SimpleConfiguration(plugin, "economy.yml");

        this.connectionPool = createConnectionPool(plugin, economyConfig);
        this.economyService = createEconomyService();
    }

    public @NotNull EconomyService service() {
        return economyService;
    }

    private EconomyService createEconomyService() {
        EconomyPlayerRepository playerRepository = new EconomyPlayerRepositoryImpl(connectionPool);
        PlayerBalanceRepository balanceRepository = new PlayerBalanceRepositoryImpl(connectionPool);

        return new EconomyServiceImpl(playerRepository, balanceRepository);
    }

    private HikariConnectionPool createConnectionPool(JavaPlugin plugin, SimpleConfiguration economyConfig) {
        int poolSize = economyConfig.getInt("Database.Pool Size", 5);

        return new SQLiteConnectionPool.Factory()
                .setMaxPoolsSize(poolSize)
                .addProperty("foreign_keys", "true")
                .addProperty("synchronous", "NORMAL")
                .addProperty("journal_mode", "WAL")
                .build();
    }

    public void shutdown() {
        if (connectionPool != null) {
            connectionPool.close();
        }
    }

}
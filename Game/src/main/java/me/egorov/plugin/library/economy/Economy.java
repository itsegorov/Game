package me.egorov.plugin.library.economy;

import me.egorov.plugin.library.configuration.SimpleConfiguration;
import me.egorov.plugin.library.database.HikariConnectionPool;
import me.egorov.plugin.library.database.type.MySQLConnectionPool;
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

        this.connectionPool = createConnectionPool(economyConfig);
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

    private HikariConnectionPool createConnectionPool(SimpleConfiguration economyConfig) {
        String host = economyConfig.getString("Connection.Host", "localhost");
        String port = economyConfig.getString("Connection.Port", "3306");
        String database = economyConfig.getString("Connection.Database", "minecraft");
        String user = economyConfig.getString("Connection.Username", "root");
        String password = economyConfig.getString("Connection.Password", "");
        int poolSize = economyConfig.getInt("Connection.Pool Size", 5);

        return new MySQLConnectionPool.Factory()
                .hostname(host)
                .port(port)
                .database(database)
                .username(user)
                .password(password)
                .maxPoolsSize(poolSize)
                .build();
    }

    public void shutdown() {
        if (connectionPool != null) {
            connectionPool.close();
        }
    }

}
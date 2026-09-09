package me.egorov.plugin.library.rank;

import me.egorov.plugin.library.configuration.SimpleConfiguration;
import me.egorov.plugin.library.database.HikariConnectionPool;
import me.egorov.plugin.library.database.type.MySQLConnectionPool;
import me.egorov.plugin.library.database.type.SQLiteConnectionPool;
import me.egorov.plugin.library.rank.repository.PlayerRankRepository;
import me.egorov.plugin.library.rank.repository.impl.PlayerRankRepositoryImpl;
import me.egorov.plugin.library.rank.service.PlayerRankService;
import me.egorov.plugin.library.rank.service.impl.PlayerRankServiceImpl;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.*;

import java.util.Objects;

public class Ranks {

    private final JavaPlugin plugin;

    private final HikariConnectionPool connectionPool;
    private final PlayerRankService playerRankService;

    public Ranks(@NotNull JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "Plugin cannot be null");

        this.plugin = plugin;

        SimpleConfiguration rankConfig = new SimpleConfiguration(plugin, "ranks.yml");

        this.connectionPool = createConnectionPool(rankConfig);
        this.playerRankService = createPlayerRankService();
    }

    private HikariConnectionPool createConnectionPool(SimpleConfiguration rankConfig) {
        int poolSize = rankConfig.getInt("Database.Pool Size", 10);

        return new SQLiteConnectionPool.Factory()
                .setMaxPoolsSize(poolSize)
                .addProperty("foreign_keys", "true")
                .addProperty("synchronous", "NORMAL")
                .addProperty("journal_mode", "WAL")
                .addProperty("busy_timeout", "5000")
                .build();
    }

    private PlayerRankService createPlayerRankService() {
        PlayerRankRepository repository = new PlayerRankRepositoryImpl(plugin, connectionPool);
        return new PlayerRankServiceImpl(repository);
    }

    public @NotNull PlayerRankService service() {
        return playerRankService;
    }

    public void shutdown() {
        if (connectionPool != null) {
            connectionPool.close();
        }
    }
}

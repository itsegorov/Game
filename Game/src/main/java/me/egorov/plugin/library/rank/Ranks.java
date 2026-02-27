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
    private final DatabaseType databaseType;

    public enum DatabaseType {
        MYSQL,
        SQLITE
    }

    public Ranks(@NotNull JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "Plugin cannot be null");

        this.plugin = plugin;

        SimpleConfiguration rankConfig = new SimpleConfiguration(plugin, "ranks.yml");

        String dbType = rankConfig.getString("Database.Type", "mysql").toUpperCase();
        this.databaseType = DatabaseType.valueOf(dbType);

        this.connectionPool = createConnectionPool(rankConfig);
        this.playerRankService = createPlayerRankService();

    }

    private HikariConnectionPool createConnectionPool(SimpleConfiguration rankConfig) {
        int poolSize = rankConfig.getInt("Database.Pool Size", 10);

        switch (databaseType) {
            case MYSQL:
                String host = rankConfig.getString("Database.MySQL.Host", "localhost");
                String port = rankConfig.getString("Database.MySQL.Port", "3306");
                String database = rankConfig.getString("Database.MySQL.Database", "minecraft");
                String user = rankConfig.getString("Database.MySQL.Username", "root");
                String password = rankConfig.getString("Database.MySQL.Password", "");

                return new MySQLConnectionPool.Factory()
                        .hostname(host)
                        .port(port)
                        .database(database)
                        .username(user)
                        .password(password)
                        .setMaxPoolsSize(poolSize)
                        .addProperty("useUnicode", "true")
                        .addProperty("characterEncoding", "utf8")
                        .addProperty("useSSL", "false")
                        .addProperty("autoReconnect", "true")
                        .build();

            case SQLITE:
                String dbPath = rankConfig.getString("Database.SQLite.File", "ranks.db");

                return new SQLiteConnectionPool.Factory()
                        .database(plugin.getDataFolder().getAbsolutePath() + "/" + dbPath)
                        .setMaxPoolsSize(poolSize)
                        .addProperty("foreign_keys", "true")
                        .addProperty("synchronous", "NORMAL")
                        .addProperty("journal_mode", "WAL")
                        .build();

            default:
                throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
    }

    private PlayerRankService createPlayerRankService() {
        PlayerRankRepository repository = new PlayerRankRepositoryImpl(plugin, connectionPool);
        return new PlayerRankServiceImpl(repository);
    }

    public @NotNull PlayerRankService service() {
        return playerRankService;
    }

    public @NotNull DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void shutdown() {
        if (connectionPool != null) {
            connectionPool.close();
        }
    }
}

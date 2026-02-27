package me.egorov.plugin.library.rank.repository.impl;

import me.egorov.plugin.library.database.HikariConnectionPool;
import me.egorov.plugin.library.database.type.MySQLConnectionPool;
import me.egorov.plugin.library.rank.Ranks;
import me.egorov.plugin.library.rank.entity.PlayerRank;
import me.egorov.plugin.library.rank.model.Rank;
import me.egorov.plugin.library.rank.repository.PlayerRankRepository;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.*;

import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlayerRankRepositoryImpl implements PlayerRankRepository {

    private final JavaPlugin plugin;
    private final HikariConnectionPool connectionPool;
    private final Map<Rank, List<PlayerRank>> rankIndex = new ConcurrentHashMap<>();
    private final boolean isMySQL;

    public PlayerRankRepositoryImpl(@NotNull JavaPlugin plugin, @NotNull HikariConnectionPool connectionPool) {
        this.plugin = plugin;
        this.connectionPool = connectionPool;
        this.isMySQL = connectionPool instanceof MySQLConnectionPool;

        createTableIfNotExists();
        scheduleExpiredRanksCleanup();
        loadAllRanksIntoIndex();
    }

    private void createTableIfNotExists() {
        String sql;
        if (isMySQL) {
            sql = "CREATE TABLE IF NOT EXISTS player_ranks (" +
                    "uuid CHAR(36) PRIMARY KEY, " +
                    "`rank` VARCHAR(32) NOT NULL, " +
                    "expire_time BIGINT, " +
                    "username VARCHAR(16), " +
                    "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
        } else {
            sql = "CREATE TABLE IF NOT EXISTS player_ranks (" +
                    "uuid CHAR(36) PRIMARY KEY, " +
                    "`rank` VARCHAR(32) NOT NULL, " +
                    "expire_time BIGINT, " +
                    "username VARCHAR(16), " +
                    "last_updated DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";
        }

        try (Connection connection = connectionPool.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);

            statement.execute("CREATE INDEX IF NOT EXISTS idx_rank ON player_ranks(rank)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_expire ON player_ranks(expire_time)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_username ON player_ranks(username)");

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to create player_ranks table", exception);
        }
    }

    private void loadAllRanksIntoIndex() {
        rankIndex.clear();
        List<PlayerRank> allRanks = findAll();
        for (PlayerRank playerRank : allRanks) {
            rankIndex.computeIfAbsent(playerRank.rank(), k -> new ArrayList<>()).add(playerRank);
        }
    }

    private void scheduleExpiredRanksCleanup() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            int deleted = deleteAllExpired();
            if (deleted > 0) {
                Component component = Component.text("Cleaned up " + deleted + " expired ranks");
                plugin.getComponentLogger().info(component);

                loadAllRanksIntoIndex();
            }
        }, 20L * 60 * 60 * 2, 20L * 60 * 60 * 2);

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            deleteAllExpired();
        }, 20L * 60, 20L * 60);
    }

    @Override
    public @NotNull Optional<PlayerRank> find(@NotNull UUID uuid) {
        for (List<PlayerRank> ranks : rankIndex.values()) {
            for (PlayerRank rank : ranks) {
                if (rank.uniqueId().equals(uuid)) {
                    return Optional.of(rank);
                }
            }
        }

        String sql = "SELECT `rank`, expire_time, username FROM player_ranks WHERE uuid = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Rank rank = Rank.fromString(resultSet.getString("rank"));
                Long expireTime = resultSet.getLong("expire_time");

                if (resultSet.wasNull()) {
                    expireTime = null;
                }

                String username = resultSet.getString("username");

                PlayerRank playerRank = new PlayerRank(uuid, rank, expireTime, username);
                rankIndex.computeIfAbsent(rank, k -> new ArrayList<>()).add(playerRank);

                return Optional.of(playerRank);
            }

            return Optional.empty();
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to fetch player rank", exception);
        }
    }

    @Override
    public @NotNull Optional<PlayerRank> find(@NotNull String username) {
        for (List<PlayerRank> ranks : rankIndex.values()) {
            for (PlayerRank rank : ranks) {
                if (username.equalsIgnoreCase(rank.userName())) {
                    return Optional.of(rank);
                }
            }
        }

        String sql = "SELECT uuid, `rank`, expire_time FROM player_ranks WHERE username = ? " +
                "ORDER BY last_updated DESC LIMIT 1";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                Rank rank = Rank.fromString(resultSet.getString("rank"));
                Long expireTime = resultSet.getLong("expire_time");

                if (resultSet.wasNull()) {
                    expireTime = null;
                }

                PlayerRank playerRank = new PlayerRank(uuid, rank, expireTime, username);
                rankIndex.computeIfAbsent(rank, k -> new ArrayList<>()).add(playerRank);

                return Optional.of(playerRank);
            }

            return Optional.empty();
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to fetch player rank by username", exception);
        }
    }

    @Override
    public void save(@NotNull UUID uuid, @NotNull Rank rank, @Nullable Long expireTime, @Nullable String username) {
        String sql;
        if (isMySQL) {
            sql = "INSERT INTO player_ranks (uuid, `rank`, expire_time, username) VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE `rank` = VALUES(`rank`), expire_time = VALUES(expire_time), " +
                    "username = VALUES(username)";
        } else {
            sql = "INSERT INTO player_ranks (uuid, `rank`, expire_time, username) VALUES (?, ?, ?, ?) " +
                    "ON CONFLICT(uuid) DO UPDATE SET `rank` = excluded.`rank`, " +
                    "expire_time = excluded.expire_time, username = excluded.username";
        }

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, rank.id());

            if (expireTime != null) {
                statement.setLong(3, expireTime);
            } else {
                statement.setNull(3, Types.BIGINT);
            }

            statement.setString(4, username);
            statement.executeUpdate();

            PlayerRank playerRank = new PlayerRank(uuid, rank, expireTime, username);

            rankIndex.values().forEach(list ->
                    list.removeIf(r -> r.uniqueId().equals(uuid))
            );

            rankIndex.computeIfAbsent(rank, k -> new ArrayList<>()).add(playerRank);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save player rank", e);
        }
    }

    @Override
    public void delete(@NotNull UUID uuid) {
        String sql = "DELETE FROM player_ranks WHERE uuid = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();

            rankIndex.values().forEach(list ->
                    list.removeIf(rank -> rank.uniqueId().equals(uuid))
            );

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to delete player rank", exception);
        }
    }

    @Override
    public int updateAllByRank(@NotNull Rank fromRank, @NotNull Rank toRank, @Nullable Long newExpireTime) {
        String sql = "UPDATE player_ranks SET `rank` = ?, expire_time = ? WHERE `rank` = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, toRank.id());

            if (newExpireTime != null) {
                statement.setLong(2, newExpireTime);
            } else {
                statement.setNull(2, Types.BIGINT);
            }

            statement.setString(3, fromRank.id());

            int updated = statement.executeUpdate();

            if (updated > 0) {
                List<PlayerRank> fromList = rankIndex.remove(fromRank);
                if (fromList != null) {
                    List<PlayerRank> toList = rankIndex.computeIfAbsent(toRank, k -> new ArrayList<>());
                    for (PlayerRank rank : fromList) {
                        PlayerRank updatedRank = new PlayerRank(
                                rank.uniqueId(),
                                toRank,
                                newExpireTime != null ? newExpireTime : rank.expireTime(),
                                rank.userName()
                        );
                        toList.add(updatedRank);
                    }
                }
            }

            return updated;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update ranks by rank", e);
        }
    }

    @Override
    public int deleteAllExpired() {
        String sql = "DELETE FROM player_ranks WHERE expire_time IS NOT NULL AND expire_time < ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, System.currentTimeMillis());
            int deleted = statement.executeUpdate();

            if (deleted > 0) {
                loadAllRanksIntoIndex();
            }

            return deleted;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete expired ranks", e);
        }
    }

    @Override
    public int deleteAllTemporary() {
        String sql = "DELETE FROM player_ranks WHERE expire_time IS NOT NULL";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int deleted = statement.executeUpdate();

            if (deleted > 0) {
                loadAllRanksIntoIndex();
            }

            return deleted;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete temporary ranks", e);
        }
    }

    @Override
    public int resetAllTemporaryToDefault(@NotNull Rank defaultRank) {
        String sql = "UPDATE player_ranks SET `rank` = ?, expire_time = NULL WHERE expire_time IS NOT NULL";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, defaultRank.id());
            int updated = statement.executeUpdate();

            if (updated > 0) {
                loadAllRanksIntoIndex();
            }

            return updated;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to reset temporary ranks", e);
        }
    }

    @Override
    public @NotNull List<PlayerRank> findAllByRank(@NotNull Rank rank) {
        return rankIndex.getOrDefault(rank, Collections.emptyList());
    }

    @Override
    public @NotNull List<PlayerRank> findAllWithRankAtLeast(@NotNull Rank minRank) {
        return rankIndex.entrySet().stream()
                .filter(entry -> entry.getKey().isAtLeast(minRank))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull List<PlayerRank> findAllTemporary() {
        String sql = "SELECT uuid, `rank`, expire_time, username FROM player_ranks WHERE expire_time IS NOT NULL";
        List<PlayerRank> result = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                Rank rank = Rank.fromString(rs.getString("rank"));
                Long expireTime = rs.getLong("expire_time");
                String username = rs.getString("username");

                result.add(new PlayerRank(uuid, rank, expireTime, username));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch temporary ranks", e);
        }

        return result;
    }

    @Override
    public @NotNull List<PlayerRank> findAll() {
        List<PlayerRank> result = new ArrayList<>();
        String sql = "SELECT uuid, `rank`, expire_time, username FROM player_ranks";

        try (Connection connection = connectionPool.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                Rank rank = Rank.fromString(rs.getString("rank"));
                Long expireTime = rs.getLong("expire_time");

                if (rs.wasNull()) {
                    expireTime = null;
                }

                String username = rs.getString("username");

                result.add(new PlayerRank(uuid, rank, expireTime, username));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all ranks", e);
        }

        return result;
    }

    @Override
    public @NotNull java.util.Map<Rank, Integer> getRankStatistics() {
        Map<Rank, Integer> stats = new LinkedHashMap<>();

        for (Map.Entry<Rank, List<PlayerRank>> entry : rankIndex.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().size());
        }

        return stats;
    }

    @Override
    public @NotNull List<PlayerRank> findTopPlayers(int limit) {
        return rankIndex.entrySet().stream()
                .sorted(Map.Entry.<Rank, List<PlayerRank>>comparingByKey(
                        Comparator.comparingInt(Rank::priority).reversed()))
                .limit(limit)
                .flatMap(entry -> entry.getValue().stream())
                .limit(limit)
                .collect(Collectors.toList());
    }
}

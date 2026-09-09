package me.egorov.plugin.library.economy.repository.impl;

import me.egorov.plugin.library.database.HikariConnectionPool;
import me.egorov.plugin.library.economy.model.entity.EconomyPlayer;
import me.egorov.plugin.library.economy.repository.EconomyPlayerRepository;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public final class EconomyPlayerRepositoryImpl implements EconomyPlayerRepository {
    
    private final @NotNull HikariConnectionPool connectionPool;
    
    public EconomyPlayerRepositoryImpl(@NotNull HikariConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        
        createTablesIfNotExists();
    }

    private void createTablesIfNotExists() {
        try (Connection connection = connectionPool.getConnection()) {

            try (PreparedStatement playersTableStatement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS economy_players (" +
                            "player_uuid TEXT PRIMARY KEY, " +
                            "player_name TEXT NOT NULL" +
                            ")")) {
                playersTableStatement.execute();
            }

            try (PreparedStatement balancesTableStatement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS economy_balances (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "player_uuid TEXT NOT NULL, " +
                            "currency TEXT NOT NULL, " +
                            "balance REAL NOT NULL DEFAULT 0.0, " +
                            "FOREIGN KEY (player_uuid) REFERENCES economy_players(player_uuid) ON DELETE CASCADE, " +
                            "UNIQUE(player_uuid, currency)" +
                            ")")) {
                balancesTableStatement.execute();
            }

            try (PreparedStatement indexStatement = connection.prepareStatement(
                    "CREATE INDEX IF NOT EXISTS idx_balance_player ON economy_balances(player_uuid)")) {
                indexStatement.execute();
            }

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to initialize database tables", exception);
        }
    }

    @Override
    public void save(@NotNull EconomyPlayer player) {
        String sql = "INSERT INTO economy_players (player_uuid, player_name) VALUES (?, ?) " +
                "ON CONFLICT(player_uuid) DO UPDATE SET player_name = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, player.uuid().toString());
            preparedStatement.setString(2, player.name());
            preparedStatement.setString(3, player.name());
            preparedStatement.executeUpdate();

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to save player", exception);
        }
    }

    @Override
    public @NotNull Optional<EconomyPlayer> findByUuid(@NotNull UUID uuid) {
        String sql = "SELECT player_name FROM economy_players WHERE player_uuid = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String name = resultSet.getString("player_name");
                return Optional.of(new EconomyPlayer(uuid, name));
            }

            return Optional.empty();

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find player by uuid", exception);
        }
    }

    @Override
    public @NotNull Optional<EconomyPlayer> findByUsername(@NotNull String username) {
        String sql = "SELECT player_uuid, player_name FROM economy_players WHERE player_name = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("player_uuid"));
                String name = resultSet.getString("player_name");
                return Optional.of(new EconomyPlayer(uuid, name));
            }

            return Optional.empty();

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find player by username", exception);
        }
    }

    @Override
    public boolean checkExists(@NotNull UUID uuid) {
        String sql = "SELECT 1 FROM economy_players WHERE player_uuid = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, uuid.toString());
            return preparedStatement.executeQuery().next();

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to check player existence", exception);
        }
    }

    @Override
    public boolean checkExists(@NotNull String username) {
        String sql = "SELECT 1 FROM economy_players WHERE player_name = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, username);
            return preparedStatement.executeQuery().next();

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to check player existence", exception);
        }
    }
    
}
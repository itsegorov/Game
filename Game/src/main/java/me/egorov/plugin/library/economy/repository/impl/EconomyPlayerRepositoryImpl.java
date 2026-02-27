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
        try (Connection connection = connectionPool.connection()) {
            try (PreparedStatement playersTableStatement = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS economy_players (" +
                    "player_uuid VARCHAR(36) PRIMARY KEY, " +
                    "player_name VARCHAR(16) NOT NULL" +
                    ")")) {
                playersTableStatement.execute();
            }
            
            try (PreparedStatement balancesTableStatement = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS economy_balances (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "player_uuid VARCHAR(36) NOT NULL, " +
                    "currency VARCHAR(32) NOT NULL, " +
                    "balance DOUBLE NOT NULL DEFAULT 0.0, " +
                    "FOREIGN KEY (player_uuid) REFERENCES economy_players(player_uuid) ON DELETE CASCADE, " +
                    "UNIQUE KEY unique_player_currency (player_uuid, currency)" +
                    ")")) {
                balancesTableStatement.execute();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to initialize database tables. Please check your MySQL version and syntax.", exception);
        }
    }
    
    @Override
    public void save(@NotNull EconomyPlayer player) {
        try (Connection connection = connectionPool.connection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                 "INSERT INTO economy_players (player_uuid, player_name) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE player_name = ?")) {
            
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
        try (Connection connection = connectionPool.connection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                 "SELECT player_name FROM economy_players WHERE player_uuid = ?")) {
            
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
        try (Connection connection = connectionPool.connection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                 "SELECT player_name FROM economy_players WHERE player_name = ?")) {
            
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                String name = resultSet.getString("player_name");
                UUID uuid = UUID.fromString(resultSet.getString("player_uuid"));
                
                return Optional.of(new EconomyPlayer(uuid, name));
            }
            
            return Optional.empty();
            
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find player by uuid", exception);
        }
    }
    
    @Override
    public boolean checkExists(@NotNull UUID uuid) {
        try (Connection connection = connectionPool.connection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                 "SELECT 1 FROM economy_players WHERE player_uuid = ?")) {
            
            preparedStatement.setString(1, uuid.toString());
            return preparedStatement.executeQuery().next();
            
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to check player existence", exception);
        }
    }
    
    @Override
    public boolean checkExists(@NotNull String username) {
        try (Connection connection = connectionPool.connection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                 "SELECT 1 FROM economy_players WHERE player_name = ?")) {
            
            preparedStatement.setString(1, username);
            return preparedStatement.executeQuery().next();
            
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to check player existence", exception);
        }
    }
    
}
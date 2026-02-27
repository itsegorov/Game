package me.egorov.plugin.library.economy.repository.impl;

import me.egorov.plugin.library.database.HikariConnectionPool;
import me.egorov.plugin.library.economy.model.Currency;
import me.egorov.plugin.library.economy.model.entity.EconomyPlayer;
import me.egorov.plugin.library.economy.model.entity.PlayerBalance;
import me.egorov.plugin.library.economy.repository.PlayerBalanceRepository;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class PlayerBalanceRepositoryImpl implements PlayerBalanceRepository {
    
    private final HikariConnectionPool connectionPool;
    
    public PlayerBalanceRepositoryImpl(HikariConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public void save(@NotNull PlayerBalance balance) {
        String sql = "INSERT INTO economy_balances (player_uuid, currency, balance) VALUES (?, ?, ?) " +
                "ON CONFLICT(player_uuid, currency) DO UPDATE SET balance = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, balance.player().uuid().toString());
            statement.setString(2, balance.currency().name());
            statement.setDouble(3, balance.value());
            statement.setDouble(4, balance.value());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save player balance", e);
        }
    }

    @Override
    public @NotNull Optional<PlayerBalance> findByPlayerAndCurrency(@NotNull EconomyPlayer player,
                                                                    @NotNull Currency currency) {
        String sql = "SELECT balance FROM economy_balances WHERE player_uuid = ? AND currency = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, player.uuid().toString());
            statement.setString(2, currency.name());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                double amount = resultSet.getDouble("balance");
                return Optional.of(new PlayerBalance(player, currency, amount));
            }

            PlayerBalance newBalance = new PlayerBalance(player, currency, 0.0);
            save(newBalance);
            return Optional.of(newBalance);

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find player balance", exception);
        }
    }
    
}
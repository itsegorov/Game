package me.egorov.plugin.library.economy.service.impl;

import me.egorov.plugin.library.economy.model.Currency;
import me.egorov.plugin.library.economy.model.entity.EconomyPlayer;
import me.egorov.plugin.library.economy.model.entity.PlayerBalance;
import me.egorov.plugin.library.economy.repository.EconomyPlayerRepository;
import me.egorov.plugin.library.economy.repository.PlayerBalanceRepository;
import me.egorov.plugin.library.economy.service.EconomyService;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public final class EconomyServiceImpl implements EconomyService {

    private final @NotNull EconomyPlayerRepository playerRepository;
    private final @NotNull PlayerBalanceRepository balanceRepository;

    public EconomyServiceImpl(@NotNull EconomyPlayerRepository playerRepository,
                              @NotNull PlayerBalanceRepository balanceRepository) {
        this.playerRepository = playerRepository;
        this.balanceRepository = balanceRepository;
    }

    @Override
    public double getBalance(@NotNull EconomyPlayer player, @NotNull Currency currency) {
        return balanceRepository.findByPlayerAndCurrency(player, currency)
                .map(PlayerBalance::value)
                .orElse(0.0);
    }

    @Override
    public void setBalance(@NotNull EconomyPlayer player, @NotNull Currency currency, double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }

        PlayerBalance balance = new PlayerBalance(player, currency, amount);
        balanceRepository.save(balance);
    }

    @Override
    public boolean hasAmount(@NotNull EconomyPlayer player, @NotNull Currency currency, double amount) {
        return getBalance(player, currency) >= amount;
    }

    @Override
    public void deposit(@NotNull EconomyPlayer player, @NotNull Currency currency, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        double currentBalance = getBalance(player, currency);
        setBalance(player, currency, currentBalance + amount);
    }

    @Override
    public void withdraw(@NotNull EconomyPlayer player, @NotNull Currency currency, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdraw amount must be positive");
        }

        if (!hasAmount(player, currency, amount)) {
            throw new IllegalStateException("Insufficient funds");
        }

        double currentBalance = getBalance(player, currency);
        setBalance(player, currency, currentBalance - amount);
    }

    @Override
    public boolean transfer(@NotNull EconomyPlayer from, @NotNull EconomyPlayer to, @NotNull Currency currency,
                            double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        if (!hasAmount(from, currency, amount)) {
            return false;
        }

        withdraw(from, currency, amount);
        deposit(to, currency, amount);

        return true;
    }

    @Override
    public void createAccount(@NotNull EconomyPlayer player) {
        playerRepository.save(player);
    }

    @Override
    public @NotNull Optional<EconomyPlayer> find(@NotNull UUID uniqueId) {
        return playerRepository.findByUuid(uniqueId);
    }

    @Override
    public @NotNull Optional<EconomyPlayer> find(@NotNull String name) {
        return playerRepository.findByUsername(name);
    }

}

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
import java.util.concurrent.CompletableFuture;

public final class EconomyServiceImpl implements EconomyService {

    private final @NotNull EconomyPlayerRepository playerRepository;
    private final @NotNull PlayerBalanceRepository balanceRepository;

    public EconomyServiceImpl(@NotNull EconomyPlayerRepository playerRepository,
                              @NotNull PlayerBalanceRepository balanceRepository) {
        this.playerRepository = playerRepository;
        this.balanceRepository = balanceRepository;
    }

    @Override
    public CompletableFuture<Optional<EconomyPlayer>> find(@NotNull UUID uniqueId) {
        return CompletableFuture.supplyAsync(() -> playerRepository.findByUuid(uniqueId));
    }

    @Override
    public CompletableFuture<Optional<EconomyPlayer>> find(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> playerRepository.findByUsername(name));
    }

    @Override
    public CompletableFuture<Double> getBalance(@NotNull EconomyPlayer player, @NotNull Currency currency) {
        return CompletableFuture.supplyAsync(() ->
                balanceRepository.findByPlayerAndCurrency(player, currency)
                        .map(PlayerBalance::value)
                        .orElse(0.0)
        );
    }

    @Override
    public CompletableFuture<Void> setBalance(@NotNull EconomyPlayer player, @NotNull Currency currency, double amount) {
        return CompletableFuture.runAsync(() -> {
            if (amount < 0) {
                throw new IllegalArgumentException("Balance cannot be negative");
            }
            PlayerBalance balance = new PlayerBalance(player, currency, amount);
            balanceRepository.save(balance);
        });
    }

    @Override
    public CompletableFuture<Boolean> hasAmount(@NotNull EconomyPlayer player, @NotNull Currency currency, double amount) {
        return getBalance(player, currency).thenApply(balance -> balance >= amount);
    }

    @Override
    public CompletableFuture<Void> deposit(@NotNull EconomyPlayer player, @NotNull Currency currency, double amount) {
        return CompletableFuture.runAsync(() -> {
            if (amount <= 0) {
                throw new IllegalArgumentException("Deposit amount must be positive");
            }

            double currentBalance = balanceRepository.findByPlayerAndCurrency(player, currency)
                    .map(PlayerBalance::value)
                    .orElse(0.0);

            PlayerBalance newBalance = new PlayerBalance(player, currency, currentBalance + amount);
            balanceRepository.save(newBalance);
        });
    }

    @Override
    public CompletableFuture<Void> withdraw(@NotNull EconomyPlayer player, @NotNull Currency currency, double amount) {
        return CompletableFuture.runAsync(() -> {
            if (amount <= 0) {
                throw new IllegalArgumentException("Withdraw amount must be positive");
            }

            double currentBalance = balanceRepository.findByPlayerAndCurrency(player, currency)
                    .map(PlayerBalance::value)
                    .orElse(0.0);

            if (currentBalance < amount) {
                throw new IllegalStateException("Insufficient funds");
            }

            PlayerBalance newBalance = new PlayerBalance(player, currency, currentBalance - amount);
            balanceRepository.save(newBalance);
        });
    }

    @Override
    public CompletableFuture<Boolean> transfer(@NotNull EconomyPlayer from, @NotNull EconomyPlayer to,
                                               @NotNull Currency currency, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            if (amount <= 0) {
                throw new IllegalArgumentException("Transfer amount must be positive");
            }

            double fromBalance = balanceRepository.findByPlayerAndCurrency(from, currency)
                    .map(PlayerBalance::value)
                    .orElse(0.0);

            if (fromBalance < amount) {
                return false;
            }

            double toBalance = balanceRepository.findByPlayerAndCurrency(to, currency)
                    .map(PlayerBalance::value)
                    .orElse(0.0);

            balanceRepository.save(new PlayerBalance(from, currency, fromBalance - amount));
            balanceRepository.save(new PlayerBalance(to, currency, toBalance + amount));

            return true;
        });
    }

    @Override
    public CompletableFuture<Void> createAccount(@NotNull EconomyPlayer player) {
        return CompletableFuture.runAsync(() -> playerRepository.save(player));
    }

}

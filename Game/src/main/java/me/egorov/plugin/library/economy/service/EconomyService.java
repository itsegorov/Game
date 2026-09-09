package me.egorov.plugin.library.economy.service;

import me.egorov.plugin.library.economy.model.Currency;
import me.egorov.plugin.library.economy.model.entity.EconomyPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface EconomyService {

    CompletableFuture<Optional<EconomyPlayer>> find(@NotNull UUID uniqueId);

    CompletableFuture<Optional<EconomyPlayer>> find(@NotNull String name);

    CompletableFuture<Double> getBalance(@NotNull EconomyPlayer player, @NotNull Currency currency);

    CompletableFuture<Void> setBalance(@NotNull EconomyPlayer player, @NotNull Currency currency, double amount);

    CompletableFuture<Boolean> hasAmount(@NotNull EconomyPlayer player, @NotNull Currency currency, double amount);

    CompletableFuture<Void> deposit(@NotNull EconomyPlayer player, @NotNull Currency currency, double amount);

    CompletableFuture<Void> withdraw(@NotNull EconomyPlayer player, @NotNull Currency currency, double amount);

    CompletableFuture<Boolean> transfer(@NotNull EconomyPlayer from, @NotNull EconomyPlayer to,
                                        @NotNull Currency currency, double amount);

    CompletableFuture<Void> createAccount(@NotNull EconomyPlayer player);
}

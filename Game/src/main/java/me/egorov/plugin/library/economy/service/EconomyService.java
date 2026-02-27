package me.egorov.plugin.library.economy.service;

import me.egorov.plugin.library.economy.model.Currency;
import me.egorov.plugin.library.economy.model.entity.EconomyPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public interface EconomyService {

    double getBalance(@NotNull EconomyPlayer player, @NotNull Currency currency);

    void setBalance(@NotNull EconomyPlayer player, @NotNull Currency currency, double amount);

    boolean hasAmount(@NotNull EconomyPlayer player, @NotNull Currency currency, double amount);

    void deposit(@NotNull EconomyPlayer player, @NotNull Currency currency, double amount);

    void withdraw(@NotNull EconomyPlayer player, @NotNull Currency currency, double amount);

    boolean transfer(@NotNull EconomyPlayer from, @NotNull EconomyPlayer to, @NotNull Currency currency, double amount);

    void createAccount(@NotNull EconomyPlayer player);

    @NotNull Optional<EconomyPlayer> find(@NotNull UUID uniqueId);

    @NotNull Optional<EconomyPlayer> find(@NotNull String name);

}

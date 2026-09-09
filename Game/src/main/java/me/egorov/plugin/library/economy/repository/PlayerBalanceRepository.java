package me.egorov.plugin.library.economy.repository;

import me.egorov.plugin.library.economy.model.Currency;
import me.egorov.plugin.library.economy.model.entity.EconomyPlayer;
import me.egorov.plugin.library.economy.model.entity.PlayerBalance;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface PlayerBalanceRepository {
    
    void save(@NotNull PlayerBalance balance);

    @NotNull Optional<PlayerBalance> findByPlayerAndCurrency(@NotNull EconomyPlayer player, @NotNull Currency currency);
    
}
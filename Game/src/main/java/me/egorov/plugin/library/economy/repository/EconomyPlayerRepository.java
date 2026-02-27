package me.egorov.plugin.library.economy.repository;

import me.egorov.plugin.library.economy.model.entity.EconomyPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public interface EconomyPlayerRepository {
    
    void save(@NotNull EconomyPlayer player);
    
    @NotNull Optional<EconomyPlayer> findByUuid(@NotNull UUID uuid);
    
    @NotNull Optional<EconomyPlayer> findByUsername(@NotNull String username);
    
    boolean checkExists(@NotNull UUID uuid);
    
    boolean checkExists(@NotNull String username);
    
}
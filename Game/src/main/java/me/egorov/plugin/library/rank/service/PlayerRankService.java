package me.egorov.plugin.library.rank.service;

import me.egorov.plugin.library.rank.entity.PlayerRank;
import me.egorov.plugin.library.rank.model.Rank;
import org.jetbrains.annotations.*;

import org.bukkit.entity.Player;

import java.util.*;

public interface PlayerRankService {

    @NotNull Optional<PlayerRank> get(@NotNull UUID playerUuid);

    @NotNull Optional<PlayerRank> get(@NotNull String username);

    boolean hasAccess(@NotNull UUID playerUuid, @NotNull Rank requiredRank);

    void updateRank(@NotNull UUID playerUuid, @NotNull Rank rank, @Nullable Long expireTime, @Nullable String username);

    void updateRank(@NotNull Player player, @NotNull Rank rank, @Nullable Long expireTime);

    void removeRank(@NotNull UUID playerUuid, @NotNull Rank rank, @Nullable String username);

    void removeRank(@NotNull Player player, @NotNull Rank rank);

    void clearRank(@NotNull UUID playerUuid);

    boolean isAbove(@NotNull UUID playerUuid, @NotNull Rank requiredRank);

    boolean isBelow(@NotNull UUID playerUuid, @NotNull Rank requiredRank);

    boolean isEqual(@NotNull UUID playerUuid, @NotNull Rank requiredRank);

    boolean isAtLeast(@NotNull UUID playerUuid, @NotNull Rank requiredRank);

    boolean isAbove(@NotNull String username, @NotNull Rank requiredRank);

    boolean isBelow(@NotNull String username, @NotNull Rank requiredRank);

    boolean isEqual(@NotNull String username, @NotNull Rank requiredRank);

    boolean isAtLeast(@NotNull String username, @NotNull Rank requiredRank);

    int promoteAll(@NotNull Rank fromRank, @Nullable Long newExpireTime);

    int promoteAllTo(@NotNull Rank fromRank, @NotNull Rank toRank, @Nullable Long newExpireTime);

    int demoteAll(@NotNull Rank fromRank, @Nullable Long newExpireTime);

    int demoteAllTo(@NotNull Rank fromRank, @NotNull Rank toRank, @Nullable Long newExpireTime);

    int setRankForAll(@NotNull Rank rank, @Nullable Long expireTime);

    int removeAllExpiredRanks();

    int removeAllTemporaryRanks();

    int resetAllTemporaryRanks();

    int updateRanksByCondition(@NotNull java.util.function.Predicate<PlayerRank> condition,
                               @NotNull Rank newRank,
                               @Nullable Long newExpireTime);

    @NotNull List<PlayerRank> getPlayersByRank(@NotNull Rank rank);

    @NotNull List<PlayerRank> getPlayersWithRankAtLeast(@NotNull Rank minRank);

    @NotNull List<PlayerRank> getPlayersWithTemporaryRanks();

    @NotNull List<PlayerRank> getAllPlayers();

    @NotNull Map<Rank, Integer> getRankStatistics();

    @NotNull List<PlayerRank> getTopPlayers(int limit);

    int getPlayerCountByRank(@NotNull Rank rank);

    int getTotalPlayers();

    void invalidateCache(@NotNull UUID playerUuid);

    void clearCache();

    void clearExpiredRanks();

    void updatePlayerName(@NotNull UUID playerUuid, @NotNull String newName);

    void reloadAll();
}

package me.egorov.plugin.library.rank.repository;

import me.egorov.plugin.library.rank.entity.PlayerRank;
import me.egorov.plugin.library.rank.model.Rank;
import org.jetbrains.annotations.*;
import java.util.*;

public interface PlayerRankRepository {

    @NotNull Optional<PlayerRank> find(@NotNull UUID uuid);

    @NotNull Optional<PlayerRank> find(@NotNull String username);

    void save(@NotNull UUID uuid, @NotNull Rank rank, @Nullable Long expireTime, @Nullable String username);

    void delete(@NotNull UUID uuid);

    int updateAllByRank(@NotNull Rank fromRank, @NotNull Rank toRank, @Nullable Long newExpireTime);

    int deleteAllExpired();

    int deleteAllTemporary();

    int resetAllTemporaryToDefault(@NotNull Rank defaultRank);

    @NotNull List<PlayerRank> findAllByRank(@NotNull Rank rank);

    @NotNull List<PlayerRank> findAllWithRankAtLeast(@NotNull Rank minRank);

    @NotNull List<PlayerRank> findAllTemporary();

    @NotNull List<PlayerRank> findAll();

    @NotNull java.util.Map<Rank, Integer> getRankStatistics();

    @NotNull List<PlayerRank> findTopPlayers(int limit);
}

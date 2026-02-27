package me.egorov.plugin.library.rank.service.impl;

import com.google.common.cache.*;
import me.egorov.plugin.library.rank.entity.PlayerRank;
import me.egorov.plugin.library.rank.model.Rank;
import me.egorov.plugin.library.rank.repository.PlayerRankRepository;
import me.egorov.plugin.library.rank.service.PlayerRankService;
import org.jetbrains.annotations.*;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PlayerRankServiceImpl implements PlayerRankService {

    private final PlayerRankRepository repository;
    private final Cache<UUID, PlayerRank> cache;
    private final Rank defaultRank;

    public PlayerRankServiceImpl(@NotNull PlayerRankRepository repository) {
        this(repository, Rank.PLAYER);
    }

    public PlayerRankServiceImpl(@NotNull PlayerRankRepository repository, @NotNull Rank defaultRank) {
        this.repository = repository;
        this.defaultRank = defaultRank;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    private Optional<PlayerRank> getRank(@NotNull UUID playerUuid) {
        PlayerRank cached = cache.getIfPresent(playerUuid);

        if (cached != null) {
            if (cached.isExpired()) {
                removeExpiredRank(playerUuid);
                return Optional.empty();
            }
            return Optional.of(cached);
        }

        Optional<PlayerRank> databaseRank = repository.find(playerUuid);

        if (databaseRank.isPresent()) {
            PlayerRank rank = databaseRank.get();

            if (rank.isExpired()) {
                removeExpiredRank(playerUuid);
                return Optional.empty();
            }

            cache.put(playerUuid, rank);
            return Optional.of(rank);
        }

        return Optional.empty();
    }

    private void removeExpiredRank(@NotNull UUID playerUuid) {
        repository.delete(playerUuid);
        cache.invalidate(playerUuid);
    }

    @Override
    public @NotNull Optional<PlayerRank> get(@NotNull UUID playerUuid) {
        return getRank(playerUuid);
    }

    @Override
    public @NotNull Optional<PlayerRank> get(@NotNull String username) {
        for (PlayerRank rank : cache.asMap().values()) {
            if (username.equalsIgnoreCase(rank.userName())) {
                if (rank.isExpired()) {
                    removeExpiredRank(rank.uniqueId());
                    return Optional.empty();
                }
                return Optional.of(rank);
            }
        }

        Optional<PlayerRank> databaseRank = repository.find(username);

        if (databaseRank.isPresent()) {
            PlayerRank rank = databaseRank.get();

            if (rank.isExpired()) {
                removeExpiredRank(rank.uniqueId());
                return Optional.empty();
            }

            cache.put(rank.uniqueId(), rank);
            return Optional.of(rank);
        }

        return Optional.empty();
    }

    @Override
    public boolean hasAccess(@NotNull UUID playerUuid, @NotNull Rank requiredRank) {
        Optional<PlayerRank> playerRank = get(playerUuid);

        return playerRank.map(rank -> rank.rank().isAtLeast(requiredRank))
                .orElseGet(() -> defaultRank.isAtLeast(requiredRank));
    }

    @Override
    public void updateRank(@NotNull UUID playerUuid, @NotNull Rank rank, @Nullable Long expireTime,
                           @Nullable String username) {
        PlayerRank playerRank = new PlayerRank(playerUuid, rank, expireTime, username);

        repository.save(playerUuid, rank, expireTime, username);
        cache.put(playerUuid, playerRank);
    }

    @Override
    public void updateRank(@NotNull Player player, @NotNull Rank rank, @Nullable Long expireTime) {
        PlayerRank playerRank = new PlayerRank(player.getUniqueId(), rank, expireTime, player.getName());

        repository.save(player.getUniqueId(), rank, expireTime, player.getName());
        cache.put(player.getUniqueId(), playerRank);
    }

    @Override
    public void removeRank(@NotNull UUID playerUuid, @NotNull Rank rank, @Nullable String username) {
        PlayerRank playerRank = new PlayerRank(playerUuid, rank, null, username);

        repository.save(playerUuid, rank, null, username);
        cache.put(playerUuid, playerRank);
    }

    @Override
    public void removeRank(@NotNull Player player, @NotNull Rank rank) {
        PlayerRank playerRank = new PlayerRank(player.getUniqueId(), rank, null, player.getName());

        repository.save(player.getUniqueId(), rank, null, player.getName());
        cache.put(player.getUniqueId(), playerRank);
    }

    @Override
    public void clearRank(@NotNull UUID playerUuid) {
        repository.delete(playerUuid);
        cache.invalidate(playerUuid);
    }

    @Override
    public boolean isAbove(@NotNull UUID playerUuid, @NotNull Rank requiredRank) {
        return get(playerUuid)
                .map(rank -> rank.rank().isAbove(requiredRank))
                .orElse(defaultRank.isAbove(requiredRank));
    }

    @Override
    public boolean isBelow(@NotNull UUID playerUuid, @NotNull Rank requiredRank) {
        return get(playerUuid)
                .map(rank -> rank.rank().isBelow(requiredRank))
                .orElse(defaultRank.isBelow(requiredRank));
    }

    @Override
    public boolean isEqual(@NotNull UUID playerUuid, @NotNull Rank requiredRank) {
        return get(playerUuid)
                .map(rank -> rank.rank().isEqual(requiredRank))
                .orElse(defaultRank.isEqual(requiredRank));
    }

    @Override
    public boolean isAtLeast(@NotNull UUID playerUuid, @NotNull Rank requiredRank) {
        return get(playerUuid)
                .map(rank -> rank.rank().isAtLeast(requiredRank))
                .orElse(defaultRank.isAtLeast(requiredRank));
    }

    @Override
    public boolean isAbove(@NotNull String username, @NotNull Rank requiredRank) {
        return get(username)
                .map(rank -> rank.rank().isAbove(requiredRank))
                .orElse(defaultRank.isAbove(requiredRank));
    }

    @Override
    public boolean isBelow(@NotNull String username, @NotNull Rank requiredRank) {
        return get(username)
                .map(rank -> rank.rank().isBelow(requiredRank))
                .orElse(defaultRank.isBelow(requiredRank));
    }

    @Override
    public boolean isEqual(@NotNull String username, @NotNull Rank requiredRank) {
        return get(username)
                .map(rank -> rank.rank().isEqual(requiredRank))
                .orElse(defaultRank.isEqual(requiredRank));
    }

    @Override
    public boolean isAtLeast(@NotNull String username, @NotNull Rank requiredRank) {
        return get(username)
                .map(rank -> rank.rank().isAtLeast(requiredRank))
                .orElse(defaultRank.isAtLeast(requiredRank));
    }

    @Override
    public int promoteAll(@NotNull Rank fromRank, @Nullable Long newExpireTime) {
        Rank nextRank = fromRank.next();
        if (nextRank.equals(fromRank)) {
            return 0;
        }
        return promoteAllTo(fromRank, nextRank, newExpireTime);
    }

    @Override
    public int promoteAllTo(@NotNull Rank fromRank, @NotNull Rank toRank, @Nullable Long newExpireTime) {
        int updated = repository.updateAllByRank(fromRank, toRank, newExpireTime);

        if (updated > 0) {
            getPlayersByRank(fromRank).forEach(rank ->
                    cache.invalidate(rank.uniqueId())
            );
            getPlayersByRank(toRank).forEach(rank ->
                    cache.invalidate(rank.uniqueId())
            );
        }

        return updated;
    }

    @Override
    public int demoteAll(@NotNull Rank fromRank, @Nullable Long newExpireTime) {
        Rank previousRank = fromRank.previous();
        if (previousRank.equals(fromRank)) {
            return 0;
        }
        return demoteAllTo(fromRank, previousRank, newExpireTime);
    }

    @Override
    public int demoteAllTo(@NotNull Rank fromRank, @NotNull Rank toRank, @Nullable Long newExpireTime) {
        int updated = repository.updateAllByRank(fromRank, toRank, newExpireTime);

        if (updated > 0) {
            getPlayersByRank(fromRank).forEach(rank ->
                    cache.invalidate(rank.uniqueId())
            );
            getPlayersByRank(toRank).forEach(rank ->
                    cache.invalidate(rank.uniqueId())
            );
        }

        return updated;
    }

    @Override
    public int setRankForAll(@NotNull Rank rank, @Nullable Long expireTime) {
        List<PlayerRank> allPlayers = getAllPlayers();
        int count = 0;

        for (PlayerRank playerRank : allPlayers) {
            updateRank(playerRank.uniqueId(), rank, expireTime, playerRank.userName());
            count++;
        }

        return count;
    }

    @Override
    public int removeAllExpiredRanks() {
        int deleted = repository.deleteAllExpired();

        if (deleted > 0) {
            cache.invalidateAll();
        }

        return deleted;
    }

    @Override
    public int removeAllTemporaryRanks() {
        int deleted = repository.deleteAllTemporary();

        if (deleted > 0) {
            cache.invalidateAll();
        }

        return deleted;
    }

    @Override
    public int resetAllTemporaryRanks() {
        int updated = repository.resetAllTemporaryToDefault(defaultRank);

        if (updated > 0) {
            cache.invalidateAll();
        }

        return updated;
    }

    @Override
    public int updateRanksByCondition(@NotNull Predicate<PlayerRank> condition,
                                      @NotNull Rank newRank,
                                      @Nullable Long newExpireTime) {
        List<PlayerRank> allPlayers = getAllPlayers();
        int count = 0;

        for (PlayerRank playerRank : allPlayers) {
            if (condition.test(playerRank)) {
                updateRank(playerRank.uniqueId(), newRank, newExpireTime, playerRank.userName());
                count++;
            }
        }

        return count;
    }

    @Override
    public @NotNull List<PlayerRank> getPlayersByRank(@NotNull Rank rank) {
        return repository.findAllByRank(rank);
    }

    @Override
    public @NotNull List<PlayerRank> getPlayersWithRankAtLeast(@NotNull Rank minRank) {
        return repository.findAllWithRankAtLeast(minRank);
    }

    @Override
    public @NotNull List<PlayerRank> getPlayersWithTemporaryRanks() {
        return repository.findAllTemporary();
    }

    @Override
    public @NotNull List<PlayerRank> getAllPlayers() {
        return repository.findAll();
    }

    @Override
    public @NotNull Map<Rank, Integer> getRankStatistics() {
        return repository.getRankStatistics();
    }

    @Override
    public @NotNull List<PlayerRank> getTopPlayers(int limit) {
        return repository.findTopPlayers(limit);
    }

    @Override
    public int getPlayerCountByRank(@NotNull Rank rank) {
        return repository.findAllByRank(rank).size();
    }

    @Override
    public int getTotalPlayers() {
        return repository.findAll().size();
    }

    @Override
    public void invalidateCache(@NotNull UUID playerUuid) {
        cache.invalidate(playerUuid);
    }

    @Override
    public void clearCache() {
        cache.invalidateAll();
    }

    @Override
    public void clearExpiredRanks() {
        cache.asMap().entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    @Override
    public void updatePlayerName(@NotNull UUID playerUuid, @NotNull String newName) {
        Optional<PlayerRank> existing = get(playerUuid);
        existing.ifPresent(rank -> {
            updateRank(playerUuid, rank.rank(), rank.expireTime(), newName);
        });
    }

    @Override
    public void reloadAll() {
        cache.invalidateAll();
    }

    public CacheStats getCacheStats() {
        return cache.stats();
    }
}

package me.egorov.plugin.library.rank.wrapper;

import me.egorov.plugin.library.rank.entity.PlayerRank;
import me.egorov.plugin.library.rank.model.Rank;
import me.egorov.plugin.library.rank.service.PlayerRankService;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RankManagerWrapper {

    private final PlayerRankService rankService;

    public RankManagerWrapper(PlayerRankService rankService) {
        this.rankService = rankService;
    }

    public boolean isEnabled() {
        return rankService != null;
    }

    public Optional<Rank> getPlayerRank(Player player) {
        return rankService.get(player.getUniqueId())
                .map(PlayerRank::rank);
    }

    public Optional<String> getPrimaryGroup(Player player) {
        return getPlayerRank(player).map(Rank::id);
    }

    public Optional<String> getPlayerPrefix(Player player) {
        return getPlayerRank(player).map(Rank::prefix);
    }

    public boolean isInGroup(Player player, String rankId) {
        return getPlayerRank(player)
                .map(rank -> rank.id().equalsIgnoreCase(rankId))
                .orElse(false);
    }

    public boolean isInAnyGroup(Player player, String... rankIds) {
        Optional<Rank> playerRank = getPlayerRank(player);
        if (playerRank.isEmpty()) return false;

        for (String rankId : rankIds) {
            if (playerRank.get().id().equalsIgnoreCase(rankId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInAllGroups(Player player, String... rankIds) {
        Optional<Rank> playerRank = getPlayerRank(player);
        if (playerRank.isEmpty()) return false;

        for (String rankId : rankIds) {
            if (!playerRank.get().id().equalsIgnoreCase(rankId)) {
                return false;
            }
        }
        return true;
    }

    public int getGroupWeight(Player player) {
        return getPlayerRank(player)
                .map(Rank::priority)
                .orElse(-1);
    }

    public int getGroupWeightOrDefault(Player player, int defaultValue) {
        return getPlayerRank(player)
                .map(Rank::priority)
                .orElse(defaultValue);
    }

    public Set<String> getPlayerGroups(Player player) {
        return getPlayerRank(player)
                .map(rank -> Set.of(rank.id()))
                .orElse(Set.of());
    }

    public Set<String> getAllGroups() {
        return java.util.Arrays.stream(Rank.values())
                .map(Rank::id)
                .collect(Collectors.toSet());
    }

    public Optional<String> getPlayerMeta(Player player, String key) {
        // Заглушка - можно реализовать позже
        return Optional.empty();
    }

    public CompletableFuture<Void> updatePlayer(Player player) {
        return CompletableFuture.runAsync(() -> {
            rankService.invalidateCache(player.getUniqueId());
            rankService.get(player.getUniqueId());
        });
    }

    public PlayerRankService getRankService() {
        return rankService;
    }
}

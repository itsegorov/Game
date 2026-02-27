package me.egorov.plugin.library.rank.entity;

import me.egorov.plugin.library.rank.model.Rank;
import org.jetbrains.annotations.*;

import java.util.*;

public class PlayerRank {

    private final @NotNull UUID playerUuid;
    private final @NotNull Rank rank;
    private final @Nullable Long expireTime;
    private final @Nullable String username;

    public PlayerRank(@NotNull UUID playerUuid, @NotNull Rank rank, @Nullable Long expireTime,
                      @Nullable String username) {
        this.playerUuid = Objects.requireNonNull(playerUuid);
        this.rank = Objects.requireNonNull(rank);
        this.expireTime = expireTime;
        this.username = username;
    }

    public boolean isExpired() {
        return expireTime != null && expireTime < System.currentTimeMillis();
    }

    public boolean isTemporary() {
        return expireTime != null;
    }

    public long getTimeLeft() {
        if (expireTime == null) return -1;
        return Math.max(0, expireTime - System.currentTimeMillis());
    }

    public @NotNull UUID uniqueId() {
        return playerUuid;
    }

    public @NotNull Rank rank() {
        return rank;
    }

    public @Nullable Long expireTime() {
        return expireTime;
    }

    public @Nullable String userName() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerRank that = (PlayerRank) o;
        return playerUuid.equals(that.playerUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerUuid);
    }

    @Override
    public String toString() {
        return "PlayerRank{" +
                "player=" + username +
                ", rank=" + rank.id() +
                ", expires=" + (isTemporary() ? new java.util.Date(expireTime) : "never") +
                '}';
    }
}

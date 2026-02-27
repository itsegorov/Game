package me.egorov.plugin.library.rank.repository;

import me.egorov.plugin.library.rank.model.Rank;
import org.jetbrains.annotations.*;

import java.util.*;

public interface RankRepository {

    @NotNull Optional<Rank> find(@NotNull String id);

    @NotNull List<Rank> findAll();

    @NotNull Optional<Rank> findByPriority(int priority);

    @NotNull Optional<Rank> getNextRank(@NotNull Rank current);

    @NotNull Optional<Rank> getPreviousRank(@NotNull Rank current);

    boolean exists(@NotNull String id);
}

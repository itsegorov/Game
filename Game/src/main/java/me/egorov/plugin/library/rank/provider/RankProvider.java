package me.egorov.plugin.library.rank.provider;

import me.egorov.plugin.library.rank.model.Rank;
import org.bukkit.entity.Player;

public interface RankProvider {

    Rank rankOf(Player player);
}

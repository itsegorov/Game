package me.egorov.plugin.library.rank.listener;

import me.egorov.plugin.library.rank.entity.PlayerRank;
import me.egorov.plugin.library.rank.model.Rank;
import me.egorov.plugin.library.rank.service.PlayerRankService;
import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

import java.util.Optional;

public class RanksPlayerListener implements Listener {

    private final PlayerRankService rankService;

    public RanksPlayerListener(@NotNull PlayerRankService rankService) {
        this.rankService = rankService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Optional<PlayerRank> playerRank = rankService.get(player.getUniqueId());

        if (playerRank.isEmpty()) {
            rankService.updateRank(player, Rank.PLAYER, null);
        } else {
            PlayerRank rank = playerRank.get();
            if (rank.isExpired()) {
                rankService.updateRank(player, Rank.PLAYER, null);
            }

            if (!player.getName().equals(rank.userName())) {
                rankService.updatePlayerName(player.getUniqueId(), player.getName());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        rankService.invalidateCache(event.getPlayer().getUniqueId());
    }

    //// TODO: Переделать в ChatListener
    //@EventHandler(priority = EventPriority.LOWEST)
    //public void onPlayerChat(AsyncPlayerChatEvent event) {
    //        Player player = event.getPlayer();
    //
    //        rankService.get(player.getUniqueId()).ifPresent(rank -> {
    //                String format = rank.rank().prefix() + " %s§f: %s";
    //                event.setFormat(format);
    //            });
    //    }
}

package me.egorov.plugin.library.economy.listener;

import me.egorov.plugin.library.economy.model.entity.EconomyPlayer;
import me.egorov.plugin.library.economy.service.EconomyService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EconomyPlayerListener implements Listener {

    private final EconomyService economyService;

    public EconomyPlayerListener(EconomyService economyService) {
        this.economyService = economyService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        economyService.find(player.getUniqueId()).thenAccept(optionalPlayer -> {
            if (optionalPlayer.isEmpty()) {
                EconomyPlayer economyPlayer = new EconomyPlayer(player.getUniqueId(), player.getName());
                economyService.createAccount(economyPlayer);
            }
        });
    }
}

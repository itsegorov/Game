package me.egorov.plugin.library.inventory.listener;

import me.egorov.plugin.library.inventory.session.MenuSession;
import me.egorov.plugin.library.inventory.session.PaginatedSession;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

public class MenuListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory clicked = event.getClickedInventory();
        if (clicked == null) return;

        Inventory topInventory = event.getView().getTopInventory();

        MenuSession session = MenuSession.getSession(player);

        if (session != null && session.ownsInventory(topInventory)) {
            event.setCancelled(true);

            if (topInventory.equals(clicked)) {
                if (!session.getValidationHandler().validate(event)) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return;
                }

                boolean handled = session.getClickHandler().handle(event);

                if (handled && session.getSoundHandler() != null) {
                    session.getSoundHandler().playClickSound(event);
                }
            }
            return;
        }

        PaginatedSession paginatedSession = PaginatedSession.getSession(player);
        if (paginatedSession != null && paginatedSession.getCurrentInventory() != null &&
                paginatedSession.getCurrentInventory().equals(topInventory)) {

            event.setCancelled(true);

            if (topInventory.equals(clicked)) {
                paginatedSession.handleNavigation(player, event.getSlot(), event.getCurrentItem());
            }
            return;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory topInventory = event.getView().getTopInventory();
        MenuSession session = MenuSession.getSession(player);

        if (session != null && session.ownsInventory(topInventory)) {
            event.setCancelled(true);
            return;
        }

        PaginatedSession paginatedSession = PaginatedSession.getSession(player);
        if (paginatedSession != null && paginatedSession.getCurrentInventory() != null &&
                paginatedSession.getCurrentInventory().equals(topInventory)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Inventory closedInventory = event.getInventory();
        MenuSession session = MenuSession.getSession(player);

        if (session != null && session.ownsInventory(closedInventory)) {
            MenuSession.removeSession(player);
        }

        PaginatedSession.removeSession(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        MenuSession.removeSession(player);
        PaginatedSession.removeSession(player.getUniqueId());
    }
}
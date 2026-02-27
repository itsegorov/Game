package me.egorov.plugin.library.inventory.handler;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ClickHandler {

    private final Map<Integer, Consumer<InventoryClickEvent>> slotHandlers;

    private final Map<UUID, Consumer<InventoryClickEvent>> globalHandlers;

    private final UUID handlerId;

    public ClickHandler() {
        this.slotHandlers = new HashMap<>();
        this.globalHandlers = new HashMap<>();
        this.handlerId = UUID.randomUUID();
    }

    public void register(int slot, Consumer<InventoryClickEvent> handler) {
        slotHandlers.put(slot, handler);
    }

    public void registerGlobal(Consumer<InventoryClickEvent> handler) {
        UUID id = UUID.randomUUID();
        globalHandlers.put(id, handler);
    }

    public void unregister(int slot) {
        slotHandlers.remove(slot);
    }

    public void clear() {
        slotHandlers.clear();
        globalHandlers.clear();
    }

    public boolean handle(InventoryClickEvent event) {
        if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return false;
        }

        int slot = event.getRawSlot();

        for (Consumer<InventoryClickEvent> handler : globalHandlers.values()) {
            handler.accept(event);
        }

        if (slotHandlers.containsKey(slot)) {
            slotHandlers.get(slot).accept(event);
            return true;
        }

        return slotHandlers.containsKey(slot);
    }

    public boolean hasHandler(int slot) {
        return slotHandlers.containsKey(slot);
    }

    public boolean hasGlobalHandlers() {
        return !globalHandlers.isEmpty();
    }

    public int size() {
        return slotHandlers.size() + globalHandlers.size();
    }

    public UUID getHandlerId() {
        return handlerId;
    }

    public ClickHandler copy() {
        ClickHandler copy = new ClickHandler();
        copy.slotHandlers.putAll(this.slotHandlers);
        copy.globalHandlers.putAll(this.globalHandlers);
        return copy;
    }

    public static void attachToInventory(Inventory inv, ClickHandler handler) {}

    public static ClickHandler fromInventory(Inventory inv) {
        return null;
    }

    public static Consumer<InventoryClickEvent> close() {
        return event -> {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
        };
    }

    public static Consumer<InventoryClickEvent> message(String message) {
        return event -> {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(message);
        };
    }

    public static Consumer<InventoryClickEvent> playSound(org.bukkit.Sound sound) {
        return event -> {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player) {
                Player player = (Player) event.getWhoClicked();
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            }
        };
    }

    public static Consumer<InventoryClickEvent> command(String command) {
        return event -> {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player) {
                Player player = (Player) event.getWhoClicked();
                player.performCommand(command);
            }
        };
    }

    public static Consumer<InventoryClickEvent> openInventory(Inventory inv) {
        return event -> {
            event.setCancelled(true);
            event.getWhoClicked().openInventory(inv);
        };
    }

    public static Consumer<InventoryClickEvent> cancel() {
        return event -> event.setCancelled(true);
    }

    public static Consumer<InventoryClickEvent> allow() {
        return event -> event.setCancelled(false);
    }
}

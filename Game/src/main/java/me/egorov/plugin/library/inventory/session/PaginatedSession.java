package me.egorov.plugin.library.inventory.session;

import me.egorov.plugin.GamePlugin;
import me.egorov.plugin.library.inventory.animation.object.TransitionType;
import me.egorov.plugin.library.inventory.builder.ItemBuilder;
import me.egorov.plugin.library.inventory.handler.ClickHandler;
import me.egorov.plugin.library.inventory.handler.SoundHandler;
import me.egorov.plugin.library.inventory.handler.ValidationHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class PaginatedSession {

    private static final Map<UUID, PaginatedSession> ACTIVE_PAGINATED_SESSIONS = new ConcurrentHashMap<>();

    private final Inventory baseInventory;
    private final List<ItemStack> allItems;
    private final int itemsPerPage;
    private final Function<Integer, Component> titleGenerator;
    private final ClickHandler baseClickHandler;
    private final ValidationHandler baseValidationHandler;
    private final SoundHandler soundHandler;
    private final TransitionType transitionType;
    private final int transitionDuration;

    private int currentPage = 0;
    private Inventory currentInventory;
    private boolean isTransitioning = false;

    private MenuSession currentSession;

    private static final int PREV_BUTTON_SLOT = 45;
    private static final int NEXT_BUTTON_SLOT = 53;
    private static final int CLOSE_BUTTON_SLOT = 49;

    public PaginatedSession(Inventory baseInventory, List<ItemStack> allItems, int itemsPerPage,
                            Function<Integer, Component> titleGenerator,
                            ClickHandler clickHandler, ValidationHandler validationHandler,
                            SoundHandler soundHandler, TransitionType transitionType,
                            int transitionDuration) {
        this.baseInventory = baseInventory;
        this.allItems = allItems;
        this.itemsPerPage = itemsPerPage;
        this.titleGenerator = titleGenerator;
        this.baseClickHandler = clickHandler;
        this.baseValidationHandler = validationHandler;
        this.soundHandler = soundHandler;
        this.transitionType = transitionType;
        this.transitionDuration = transitionDuration;
    }

    public static void register(UUID playerId, PaginatedSession session) {
        if (playerId != null && session != null) {
            ACTIVE_PAGINATED_SESSIONS.put(playerId, session);
        }
    }

    public static PaginatedSession getSession(UUID playerId) {
        return ACTIVE_PAGINATED_SESSIONS.get(playerId);
    }

    public static PaginatedSession getSession(Player player) {
        return player != null ? ACTIVE_PAGINATED_SESSIONS.get(player.getUniqueId()) : null;
    }

    public static void removeSession(UUID playerId) {
        ACTIVE_PAGINATED_SESSIONS.remove(playerId);
    }

    private void createSessionForPlayer(Player player, Inventory inv) {
        ClickHandler pageClickHandler = baseClickHandler.copy();
        ValidationHandler pageValidationHandler = baseValidationHandler.copy();

        setupNavigationHandlers(pageClickHandler);

        currentSession = new MenuSession(
                player.getUniqueId(),
                inv,
                pageClickHandler,
                pageValidationHandler,
                soundHandler
        );
        MenuSession.register(currentSession);
    }

    private void setupNavigationHandlers(ClickHandler clickHandler) {
        clickHandler.register(PREV_BUTTON_SLOT, e -> {
            if (e.getWhoClicked() instanceof Player p) {
                prevPage(p);
            }
        });

        clickHandler.register(NEXT_BUTTON_SLOT, e -> {
            if (e.getWhoClicked() instanceof Player p) {
                nextPage(p);
            }
        });

        clickHandler.register(CLOSE_BUTTON_SLOT, e -> {
            if (e.getWhoClicked() instanceof Player p) {
                p.closeInventory();
            }
        });
    }

    public Inventory openPage(int page, Player player) {
        if (isTransitioning) return currentInventory;

        int targetPage = Math.max(0, Math.min(page, getMaxPage()));
        if (targetPage == currentPage && currentInventory != null) {
            return currentInventory;
        }

        int oldPage = currentPage;
        currentPage = targetPage;

        if (player == null) {
            currentInventory = createPageInventory(targetPage);
            return currentInventory;
        }

        if (soundHandler != null) {
            soundHandler.playPageSound(player);
        }

        currentInventory = createPageInventory(targetPage);

        createSessionForPlayer(player, currentInventory);

        player.openInventory(currentInventory);

        return currentInventory;
    }

    public Inventory nextPage(Player player) {
        return openPage(currentPage + 1, player);
    }

    public Inventory prevPage(Player player) {
        return openPage(currentPage - 1, player);
    }

    public Inventory firstPage(Player player) {
        return openPage(0, player);
    }

    public Inventory lastPage(Player player) {
        return openPage(getMaxPage(), player);
    }

    private Inventory createPageInventory(int page) {
        Component title = titleGenerator.apply(page);
        Inventory inv;

        if (baseInventory.getType() != org.bukkit.event.inventory.InventoryType.CHEST) {
            inv = Bukkit.createInventory(baseInventory.getHolder(), baseInventory.getType(), title);
        } else {
            inv = Bukkit.createInventory(baseInventory.getHolder(), baseInventory.getSize(), title);
        }

        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, allItems.size());

        for (int i = start; i < end; i++) {
            int slot = i - start;
            if (slot < inv.getSize()) {
                ItemStack item = allItems.get(i);
                if (item != null) {
                    inv.setItem(slot, item.clone());
                }
            }
        }

        addNavigationButtons(inv, page);

        return inv;
    }

    private void addNavigationButtons(Inventory inv, int page) {
        int size = inv.getSize();
        if (size < 54) return;

        NamespacedKey navKey = new NamespacedKey(GamePlugin.instance(), "nav_action");
        NamespacedKey pageKey = new NamespacedKey(GamePlugin.instance(), "nav_page");

        if (page > 0) {
            ItemStack prevButton = ItemBuilder.of(Material.ARROW)
                    .name("§a◀ Предыдущая страница")
                    .modifyMeta(meta -> {
                        meta.getPersistentDataContainer().set(navKey, PersistentDataType.STRING, "prev");
                        meta.getPersistentDataContainer().set(pageKey, PersistentDataType.INTEGER, page - 1);
                    })
                    .build();
            inv.setItem(PREV_BUTTON_SLOT, prevButton);
        }

        if (page < getMaxPage()) {
            ItemStack nextButton = ItemBuilder.of(Material.ARROW)
                    .name("§aСледующая страница ▶")
                    .modifyMeta(meta -> {
                        meta.getPersistentDataContainer().set(navKey, PersistentDataType.STRING, "next");
                        meta.getPersistentDataContainer().set(pageKey, PersistentDataType.INTEGER, page + 1);
                    })
                    .build();
            inv.setItem(NEXT_BUTTON_SLOT, nextButton);
        }

        ItemStack closeButton = ItemBuilder.of(Material.BARRIER)
                .name("§c✕ Закрыть")
                .modifyMeta(meta -> {
                    meta.getPersistentDataContainer().set(navKey, PersistentDataType.STRING, "close");
                })
                .build();
        inv.setItem(CLOSE_BUTTON_SLOT, closeButton);

        ItemStack pageInfo = ItemBuilder.of(Material.PAPER)
                .name("§7Страница " + (page + 1) + " из " + (getMaxPage() + 1))
                .build();
        if (CLOSE_BUTTON_SLOT - 2 >= 0) {
            inv.setItem(CLOSE_BUTTON_SLOT - 2, pageInfo);
        }
    }

    public boolean handleNavigation(Player player, int slot, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        var meta = item.getItemMeta();
        var pdc = meta.getPersistentDataContainer();

        NamespacedKey navKey = new NamespacedKey(GamePlugin.instance(), "nav_action");

        if (!pdc.has(navKey, PersistentDataType.STRING)) return false;

        String action = pdc.get(navKey, PersistentDataType.STRING);

        switch (action) {
            case "prev":
                prevPage(player);
                return true;
            case "next":
                nextPage(player);
                return true;
            case "close":
                player.closeInventory();
                return true;
            default:
                return false;
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public Inventory getCurrentInventory() {
        return currentInventory;
    }

    public int getMaxPage() {
        return (int) Math.ceil((double) allItems.size() / itemsPerPage) - 1;
    }

    public int getTotalItems() {
        return allItems.size();
    }

    public int getTotalPages() {
        return getMaxPage() + 1;
    }

    public boolean hasNextPage() {
        return currentPage < getMaxPage();
    }

    public boolean hasPrevPage() {
        return currentPage > 0;
    }

    public boolean isTransitioning() {
        return isTransitioning;
    }

    public void refresh(Player player) {
        if (player != null && currentInventory != null) {
            currentInventory = createPageInventory(currentPage);

            if (currentSession != null) {
                MenuSession.removeSession(player);
                createSessionForPlayer(player, currentInventory);
            }

            player.openInventory(currentInventory);
        }
    }

    public void updateItems(List<ItemStack> newItems) {
        this.allItems.clear();
        this.allItems.addAll(newItems);
    }

    public void reset() {
        currentPage = 0;
        currentInventory = null;
        currentSession = null;
    }

    public static int getActiveCount() {
        return ACTIVE_PAGINATED_SESSIONS.size();
    }

    public static void clearAll() {
        ACTIVE_PAGINATED_SESSIONS.clear();
    }

    @Override
    public String toString() {
        return "PaginatedSession{" +
                "currentPage=" + currentPage +
                ", totalPages=" + getTotalPages() +
                ", totalItems=" + getTotalItems() +
                '}';
    }
}
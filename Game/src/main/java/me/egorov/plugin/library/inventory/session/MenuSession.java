package me.egorov.plugin.library.inventory.session;

import me.egorov.plugin.library.inventory.animation.CloseAnimation;
import me.egorov.plugin.library.inventory.handler.ClickHandler;
import me.egorov.plugin.library.inventory.handler.SoundHandler;
import me.egorov.plugin.library.inventory.handler.ValidationHandler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MenuSession {

    private static final Map<UUID, MenuSession> ACTIVE_SESSIONS = new ConcurrentHashMap<>();

    private final UUID playerId;
    private final Inventory inventory;
    private final ClickHandler clickHandler;
    private final ValidationHandler validationHandler;
    private final SoundHandler soundHandler;
    private CloseAnimation closeAnimation;
    private long openTime;
    private Object sessionData;

    public MenuSession(UUID playerId, Inventory inventory,
                       ClickHandler clickHandler,
                       ValidationHandler validationHandler,
                       SoundHandler soundHandler) {
        this.playerId = playerId;
        this.inventory = inventory;
        this.clickHandler = clickHandler;
        this.validationHandler = validationHandler;
        this.soundHandler = soundHandler;
        this.openTime = System.currentTimeMillis();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public ClickHandler getClickHandler() {
        return clickHandler;
    }

    public ValidationHandler getValidationHandler() {
        return validationHandler;
    }

    public SoundHandler getSoundHandler() {
        return soundHandler;
    }

    public CloseAnimation getCloseAnimation() {
        return closeAnimation;
    }

    public long getOpenTime() {
        return openTime;
    }

    public Object getSessionData() {
        return sessionData;
    }

    public void setCloseAnimation(CloseAnimation closeAnimation) {
        this.closeAnimation = closeAnimation;
    }

    public void setSessionData(Object sessionData) {
        this.sessionData = sessionData;
    }

    public static void register(MenuSession session) {
        if (session != null && session.getPlayerId() != null) {
            ACTIVE_SESSIONS.put(session.getPlayerId(), session);
        }
    }

    public static MenuSession getSession(UUID playerId) {
        return ACTIVE_SESSIONS.get(playerId);
    }

    public static MenuSession getSession(Player player) {
        return player != null ? ACTIVE_SESSIONS.get(player.getUniqueId()) : null;
    }

    public static void removeSession(UUID playerId) {
        ACTIVE_SESSIONS.remove(playerId);
    }

    public static void removeSession(Player player) {
        if (player != null) {
            ACTIVE_SESSIONS.remove(player.getUniqueId());
        }
    }

    public static boolean hasSession(UUID playerId) {
        return ACTIVE_SESSIONS.containsKey(playerId);
    }

    public static boolean hasSession(Player player) {
        return player != null && ACTIVE_SESSIONS.containsKey(player.getUniqueId());
    }

    public static Map<UUID, MenuSession> getActiveSessions() {
        return new ConcurrentHashMap<>(ACTIVE_SESSIONS);
    }

    public void close(Player player) {
        if (closeAnimation != null) {
            closeAnimation.play(player, inventory);
        }

        if (soundHandler != null) {
            soundHandler.playCloseSound(player);
        }

        removeSession(playerId);
    }

    public void updateOpenTime() {
        this.openTime = System.currentTimeMillis();
    }

    public boolean isExpired(long maxAgeMillis) {
        return System.currentTimeMillis() - openTime > maxAgeMillis;
    }

    public void setData(String key, Object value) {
        if (sessionData == null) {
            sessionData = new ConcurrentHashMap<String, Object>();
        }
        if (sessionData instanceof Map) {
            ((Map<String, Object>) sessionData).put(key, value);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getData(String key) {
        if (sessionData instanceof Map) {
            return (T) ((Map<String, Object>) sessionData).get(key);
        }
        return null;
    }

    public void removeData(String key) {
        if (sessionData instanceof Map) {
            ((Map<String, Object>) sessionData).remove(key);
        }
    }

    public boolean hasData(String key) {
        if (sessionData instanceof Map) {
            return ((Map<String, Object>) sessionData).containsKey(key);
        }
        return false;
    }

    public void clearData() {
        if (sessionData instanceof Map) {
            ((Map<?, ?>) sessionData).clear();
        }
    }

    public static int getActiveCount() {
        return ACTIVE_SESSIONS.size();
    }

    public static void clearAll() {
        ACTIVE_SESSIONS.clear();
    }

    public boolean ownsInventory(Inventory inv) {
        return inventory != null && inventory.equals(inv);
    }

    public boolean ownsPlayer(Player player) {
        return player != null && player.getUniqueId().equals(playerId);
    }

    @Override
    public String toString() {
        return "MenuSession{" +
                "playerId=" + playerId +
                ", inventory=" + (inventory != null ? inventory.getType() : "null") +
                ", openTime=" + openTime +
                '}';
    }
}

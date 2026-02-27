package me.egorov.plugin.library.inventory.container;

import me.egorov.plugin.GamePlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class ContainerHelper {

    private final Map<NamespacedKey, ContainerValue<?, ?>> dataMap;
    private final GamePlugin plugin;

    public ContainerHelper() {
        this.plugin = GamePlugin.instance();
        this.dataMap = new HashMap<>();
    }

    private static class ContainerValue<T, Z> {
        private final PersistentDataType<T, Z> type;
        private final Z value;

        public ContainerValue(PersistentDataType<T, Z> type, Z value) {
            this.type = type;
            this.value = value;
        }
    }

    private NamespacedKey createKey(String key) {
        return new NamespacedKey(plugin, key);
    }

    public ContainerHelper setString(String key, String value) {
        dataMap.put(createKey(key), new ContainerValue<>(PersistentDataType.STRING, value));
        return this;
    }

    public ContainerHelper setString(NamespacedKey key, String value) {
        dataMap.put(key, new ContainerValue<>(PersistentDataType.STRING, value));
        return this;
    }

    public ContainerHelper setInt(String key, int value) {
        dataMap.put(createKey(key), new ContainerValue<>(PersistentDataType.INTEGER, value));
        return this;
    }

    public ContainerHelper setInt(NamespacedKey key, int value) {
        dataMap.put(key, new ContainerValue<>(PersistentDataType.INTEGER, value));
        return this;
    }

    public ContainerHelper setBoolean(String key, boolean value) {
        dataMap.put(createKey(key), new ContainerValue<>(PersistentDataType.BOOLEAN, value));
        return this;
    }

    public ContainerHelper setBoolean(NamespacedKey key, boolean value) {
        dataMap.put(key, new ContainerValue<>(PersistentDataType.BOOLEAN, value));
        return this;
    }

    public ContainerHelper setLong(String key, long value) {
        dataMap.put(createKey(key), new ContainerValue<>(PersistentDataType.LONG, value));
        return this;
    }

    public ContainerHelper setLong(NamespacedKey key, long value) {
        dataMap.put(key, new ContainerValue<>(PersistentDataType.LONG, value));
        return this;
    }

    public ContainerHelper setDouble(String key, double value) {
        dataMap.put(createKey(key), new ContainerValue<>(PersistentDataType.DOUBLE, value));
        return this;
    }

    public ContainerHelper setDouble(NamespacedKey key, double value) {
        dataMap.put(key, new ContainerValue<>(PersistentDataType.DOUBLE, value));
        return this;
    }

    public <T, Z> ContainerHelper set(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        dataMap.put(key, new ContainerValue<>(type, value));
        return this;
    }

    public void applyTo(ItemMeta meta) {
        if (meta == null || dataMap.isEmpty()) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();

        for (Map.Entry<NamespacedKey, ContainerValue<?, ?>> entry : dataMap.entrySet()) {
            NamespacedKey key = entry.getKey();
            ContainerValue<?, ?> value = entry.getValue();

            applyValue(container, key, value);
        }
    }

    @SuppressWarnings("unchecked")
    private <T, Z> void applyValue(PersistentDataContainer container, NamespacedKey key, ContainerValue<T, Z> value) {
        container.set(key, value.type, value.value);
    }

    public static String getString(ItemMeta meta, String key) {
        if (meta == null) return null;
        NamespacedKey namespacedKey = new NamespacedKey(GamePlugin.instance(), key);
        return meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
    }

    public static String getString(ItemMeta meta, NamespacedKey key) {
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    public static Integer getInt(ItemMeta meta, String key) {
        if (meta == null) return null;
        NamespacedKey namespacedKey = new NamespacedKey(GamePlugin.instance(), key);
        return meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.INTEGER);
    }

    public static Integer getInt(ItemMeta meta, NamespacedKey key) {
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
    }

    public static Boolean getBoolean(ItemMeta meta, String key) {
        if (meta == null) return null;
        NamespacedKey namespacedKey = new NamespacedKey(GamePlugin.instance(), key);
        return meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.BOOLEAN);
    }

    public static Boolean getBoolean(ItemMeta meta, NamespacedKey key) {
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(key, PersistentDataType.BOOLEAN);
    }

    public static boolean hasKey(ItemMeta meta, String key) {
        if (meta == null) return false;
        NamespacedKey namespacedKey = new NamespacedKey(GamePlugin.instance(), key);
        return meta.getPersistentDataContainer().has(namespacedKey);
    }

    public static boolean hasKey(ItemMeta meta, NamespacedKey key) {
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(key);
    }

    public static <T, Z> boolean hasKey(ItemMeta meta, NamespacedKey key, PersistentDataType<T, Z> type) {
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(key, type);
    }

    public ContainerHelper clear() {
        dataMap.clear();
        return this;
    }

    public ContainerHelper remove(String key) {
        dataMap.remove(createKey(key));
        return this;
    }

    public ContainerHelper remove(NamespacedKey key) {
        dataMap.remove(key);
        return this;
    }

    public boolean isEmpty() {
        return dataMap.isEmpty();
    }

    public int size() {
        return dataMap.size();
    }
}

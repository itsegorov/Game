package me.egorov.plugin.library.configuration.manager;

import me.egorov.plugin.library.configuration.BaseConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.*;

import java.util.*;

public final class ConfigurationManager {

    private final JavaPlugin plugin;
    private final Map<Class<?>, BaseConfiguration<?>> configs;
    private final Map<String, BaseConfiguration<?>> namedConfigs;

    public ConfigurationManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        this.namedConfigs = new HashMap<>();
    }

    @NotNull
    public <T extends BaseConfiguration<T>> T register(@NotNull Class<T> configClass) {
        try {
            T instance = configClass.getConstructor(JavaPlugin.class).newInstance(plugin);
            return register(instance);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось создать экземпляр конфигурации " + configClass.getSimpleName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public <T extends BaseConfiguration<T>> T register(@NotNull T config) {
        configs.put(config.getClass(), config);
        namedConfigs.put(config.getFileName(), config);
        return config;
    }

    @NotNull
    public <T extends BaseConfiguration<T>> T register(@NotNull String name, @NotNull T config) {
        namedConfigs.put(name, config);
        configs.put(config.getClass(), config);
        return config;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends BaseConfiguration<T>> T get(@NotNull Class<T> configClass) {
        return (T) configs.get(configClass);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends BaseConfiguration<T>> T get(@NotNull String name) {
        return (T) namedConfigs.get(name);
    }

    @NotNull
    public <T extends BaseConfiguration<T>> T getOrThrow(@NotNull Class<T> configClass) {
        T config = get(configClass);
        if (config == null) {
            throw new IllegalStateException("Конфигурация " + configClass.getSimpleName() + " не зарегистрирована");
        }
        return config;
    }

    public void reloadAll() {
        configs.values().forEach(BaseConfiguration::reload);
    }

    public void saveAll() {
        configs.values().forEach(BaseConfiguration::save);
    }

    @NotNull
    public <T extends BaseConfiguration<T>> T reload(@NotNull Class<T> configClass) {
        T config = getOrThrow(configClass);
        config.reload();
        return config;
    }

    public boolean isRegistered(@NotNull Class<?> configClass) {
        return configs.containsKey(configClass);
    }

    public int size() {
        return configs.size();
    }
}

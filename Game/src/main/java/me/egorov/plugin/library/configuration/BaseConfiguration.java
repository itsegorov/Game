package me.egorov.plugin.library.configuration;

import me.egorov.plugin.library.configuration.parser.ConfigurationParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BaseConfiguration<T extends BaseConfiguration<T>> {

    private final ConfigurationParser parser;
    protected final JavaPlugin plugin;

    protected BaseConfiguration(@NotNull JavaPlugin plugin, @NotNull String fileName) {
        this.plugin = plugin;
        this.parser = new ConfigurationParser(plugin, fileName);
        this.parser.loadConfig();
    }

    @NotNull
    protected ConfigurationParser getParser() {
        return parser;
    }

    @NotNull
    public String getFileName() {
        return parser.getFileName();
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public T reload() {
        parser.reloadConfig();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public T save() {
        parser.saveConfig();
        return (T) this;
    }

    @Nullable
    protected String getString(@NotNull String path, @Nullable String def) {
        return parser.get(path, def);
    }

    protected int getInt(@NotNull String path, int def) {
        Integer value = parser.get(path, null);
        return value != null ? value : def;
    }

    protected boolean getBoolean(@NotNull String path, boolean def) {
        Boolean value = parser.get(path, null);
        return value != null ? value : def;
    }

    protected double getDouble(@NotNull String path, double def) {
        Double value = parser.get(path, null);
        return value != null ? value : def;
    }

    @NotNull
    protected List<String> getStringList(@NotNull String path) {
        List<String> list = parser.get(path, null);
        return list != null ? list : Collections.emptyList();
    }

    @NotNull
    protected <R> List<R> getMappedList(@NotNull String path, @NotNull Function<String, R> mapper) {
        return getStringList(path).stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @NotNull
    protected Set<String> getStringSet(@NotNull String path) {
        return new HashSet<>(getStringList(path));
    }

    @Nullable
    protected ConfigurationSection getSection(@NotNull String path) {
        return parser.getRawConfig().getConfigurationSection(path);
    }

    @NotNull
    protected Set<String> getKeys(@NotNull String path) {
        ConfigurationSection section = getSection(path);
        return section != null ? section.getKeys(false) : Collections.emptySet();
    }

    protected boolean contains(@NotNull String path) {
        return parser.getRawConfig().contains(path);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    protected T set(@NotNull String path, @Nullable Object value) {
        parser.getRawConfig().set(path, value);
        return (T) this;
    }

    protected boolean isFirstLoad() {
        return !parser.exists();
    }
}

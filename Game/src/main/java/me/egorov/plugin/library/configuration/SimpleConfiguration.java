package me.egorov.plugin.library.configuration;

import me.egorov.plugin.library.configuration.parser.ConfigurationParser;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.Consumer;

public final class SimpleConfiguration {

    private final ConfigurationParser parser;

    public SimpleConfiguration(@NotNull JavaPlugin plugin, @NotNull String fileName) {
        this.parser = new ConfigurationParser(plugin, fileName);
        this.parser.loadConfig();
    }

    @Nullable
    public String getString(@NotNull String path) {
        return parser.get(path, null);
    }

    @NotNull
    public String getString(@NotNull String path, @NotNull String def) {
        String value = parser.get(path, null);
        return value != null ? value : def;
    }

    public int getInt(@NotNull String path, int def) {
        Integer value = parser.get(path, null);
        return value != null ? value : def;
    }

    public boolean getBoolean(@NotNull String path, boolean def) {
        Boolean value = parser.get(path, null);
        return value != null ? value : def;
    }

    public double getDouble(@NotNull String path, double def) {
        Double value = parser.get(path, null);
        return value != null ? value : def;
    }

    @NotNull
    public List<String> getStringList(@NotNull String path) {
        List<String> list = parser.get(path, null);
        return list != null ? list : List.of();
    }

    public boolean contains(@NotNull String path) {
        return parser.getRawConfig().contains(path);
    }

    public void set(@NotNull String path, @Nullable Object value) {
        parser.getRawConfig().set(path, value);
    }

    public void save() {
        parser.saveConfig();
    }

    public void reload() {
        parser.reloadConfig();
    }

    public void edit(@NotNull Consumer<SimpleConfiguration> action) {
        action.accept(this);
        save();
    }

    @NotNull
    public Set<String> getKeys(@NotNull String path) {
        var section = parser.getRawConfig().getConfigurationSection(path);
        return section != null ? section.getKeys(false) : Set.of();
    }

    @NotNull
    public ConfigurationParser getParser() {
        return parser;
    }
}

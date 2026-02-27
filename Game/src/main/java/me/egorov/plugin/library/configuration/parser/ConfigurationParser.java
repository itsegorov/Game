package me.egorov.plugin.library.configuration.parser;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public final class ConfigurationParser {

    private final JavaPlugin plugin;
    private final String fileName;
    private final File configFile;
    private YamlConfiguration config;

    public ConfigurationParser(@NotNull JavaPlugin plugin, @NotNull String fileName) {
        this.plugin = plugin;
        this.fileName = fileName.endsWith(".yml") ? fileName : fileName + ".yml";
        this.configFile = new File(plugin.getDataFolder(), this.fileName);
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }

        this.config = YamlConfiguration.loadConfiguration(configFile);

        try (InputStream defaultStream = plugin.getResource(fileName)) {
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
                );
                config.setDefaults(defaultConfig);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING,
                    "Не удалось загрузить стандартные значения для " + fileName, e
            );
        }
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE,
                    "Не удалось сохранить " + fileName, e
            );
        }
    }

    public void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    @NotNull
    public YamlConfiguration getRawConfig() {
        return config;
    }

    @Nullable
    public <T> T get(@NotNull String path, @Nullable T def) {
        if (!config.contains(path)) {
            return def;
        }

        Object value = config.get(path);

        if (value == null) {
            return def;
        }

        try {
            @SuppressWarnings("unchecked")
            T result = (T) value;
            return result;
        } catch (ClassCastException e) {
            plugin.getLogger().warning(
                    String.format("Ошибка приведения типа для пути '%s' в файле '%s'", path, fileName)
            );
            return def;
        }
    }

    public boolean exists() {
        return configFile.exists();
    }

    @NotNull
    public String getFileName() {
        return fileName;
    }
}

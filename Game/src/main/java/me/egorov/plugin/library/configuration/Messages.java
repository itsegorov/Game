package me.egorov.plugin.library.configuration;

import me.egorov.plugin.utility.StringUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Messages {

    private final ConfigurationSection messagesSection;
    private final Map<String, List<String>> cachedMessages = new HashMap<>();

    public Messages(@Nullable ConfigurationSection messagesSection) {
        Objects.requireNonNull(messagesSection, "Секция сообщений не может быть null");

        this.messagesSection = messagesSection;
    }

    public void send(@NotNull Player player, @NotNull String key) {
        Collection<Component> message = get(player, key);

        if (message.isEmpty()) {
            return;
        }

        for (Component component : message) {
            player.sendMessage(component);
        }
    }

    public void send(@NotNull Player player, @NotNull String key, Map<String, String> replacers) {
        Collection<Component> message = get(player, key);

        if (message.isEmpty()) {
            return;
        }

        for (Component component : message) {
            for (Map.Entry<String, String> entry : replacers.entrySet()) {
                component = component.replaceText(TextReplacementConfig.builder()
                        .match(entry.getKey())
                        .replacement(StringUtility.parseString(entry.getValue()))
                        .build()
                );
            }

            player.sendMessage(component);
        }
    }

    public Collection<Component> process(@NotNull Player player, @NotNull String key, Map<String, String> replacers) {
        List<Component> components = new ArrayList<>();
        Collection<Component> message = get(player, key);

        if (message.isEmpty()) {
            return List.of();
        }

        for (Component component : message) {
            for (Map.Entry<String, String> entry : replacers.entrySet()) {
                component = component.replaceText(TextReplacementConfig.builder()
                        .match(entry.getKey())
                        .replacement(entry.getValue())
                        .build()
                );
            }

            components.add(component);
        }

        return components;
    }

    @NotNull
    public Collection<Component> get(@NotNull Player player, @NotNull String key) {
        if (messagesSection.contains(key)) {
            return parse(player, key);
        }

        return Collections.emptyList();
    }

    public static ReplaceMap replacer() {
        return new ReplaceMap();
    }

    public static class ReplaceMap {

        private final Map<String, String> replacements = new HashMap<>();

        private ReplaceMap() {

        }

        public ReplaceMap with(String key, String value) {
            replacements.put(key, value);

            return this;
        }

        public Map<String, String> build() {
            return replacements;
        }

    }

    private Collection<Component> parse(Player player, String key) {
        List<String> message = new ArrayList<>();

        if (cachedMessages.containsKey(key)) {
            message.addAll(cachedMessages.get(key));
        } else {
            message.addAll(messagesSection.getStringList(key));
            cachedMessages.put(key, message);
        }

        List<Component> result = new ArrayList<>();

        for (String line : message) {
            if (line.isEmpty()) {
                continue;
            }

            result.add(StringUtility.parseString(player, line));
        }

        return result;
    }

}
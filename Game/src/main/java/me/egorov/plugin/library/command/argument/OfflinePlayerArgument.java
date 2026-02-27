package me.egorov.plugin.library.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class OfflinePlayerArgument {

    private static final Map<String, Set<String>> NICKNAME_CACHE = new ConcurrentHashMap<>();
    private static boolean cacheLoaded = false;

    public static StringArgumentType offlinePlayer() {
        return StringArgumentType.word();
    }

    public static SuggestionProvider<CommandSourceStack> suggestOfflinePlayers() {
        return (context, builder) -> {
            ensureCacheLoaded();
            return suggestFromCache(builder);
        };
    }

    public static void loadCache(JavaPlugin plugin) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Set<String> names = new HashSet<>();
            File playerDataFolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "playerdata");

            if (playerDataFolder.exists() && playerDataFolder.isDirectory()) {
                File[] files = playerDataFolder.listFiles((dir, name) -> name.endsWith(".dat"));

                if (files != null) {
                    for (File file : files) {
                        String uuidStr = file.getName().replace(".dat", "");
                        try {
                            UUID uuid = UUID.fromString(uuidStr);
                            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                            String name = player.getName();

                            if (name != null && !name.isEmpty()) {
                                names.add(name);
                                char firstChar = name.toLowerCase().charAt(0);
                                NICKNAME_CACHE.computeIfAbsent(String.valueOf(firstChar), k -> ConcurrentHashMap.newKeySet())
                                        .add(name);
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }

            cacheLoaded = true;
            plugin.getLogger().info("✅ Загружено " + names.size() + " ников оффлайн игроков");
        });
    }

    private static void ensureCacheLoaded() {
        if (!cacheLoaded) {
            loadCacheSync();
        }
    }

    private static void loadCacheSync() {
        File playerDataFolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "playerdata");

        if (playerDataFolder.exists() && playerDataFolder.isDirectory()) {
            File[] files = playerDataFolder.listFiles((dir, name) -> name.endsWith(".dat"));

            if (files != null) {
                for (File file : files) {
                    String uuidStr = file.getName().replace(".dat", "");
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                        String name = player.getName();

                        if (name != null) {
                            char firstChar = name.toLowerCase().charAt(0);
                            NICKNAME_CACHE.computeIfAbsent(String.valueOf(firstChar), k -> ConcurrentHashMap.newKeySet())
                                    .add(name);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        cacheLoaded = true;
    }

    private static CompletableFuture<Suggestions> suggestFromCache(SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();

        if (remaining.isEmpty()) {
            return builder.buildFuture();
        }

        char firstChar = remaining.charAt(0);
        Set<String> candidates = NICKNAME_CACHE.get(String.valueOf(firstChar));

        if (candidates != null) {
            candidates.stream()
                    .filter(name -> name.toLowerCase().startsWith(remaining))
                    .limit(50)
                    .forEach(builder::suggest);
        }

        return builder.buildFuture();
    }

    public static void updatePlayer(String name) {
        if (name != null && !name.isEmpty()) {
            char firstChar = name.toLowerCase().charAt(0);
            NICKNAME_CACHE.computeIfAbsent(String.valueOf(firstChar), k -> ConcurrentHashMap.newKeySet())
                    .add(name);
        }
    }
}

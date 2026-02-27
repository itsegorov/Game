package me.egorov.plugin.library.command.cooldown;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CooldownManager {

    private final JavaPlugin plugin;
    private final Map<String, Map<UUID, Long>> cooldowns = new HashMap<>();
    private final Map<String, Long> defaultCooldowns = new HashMap<>();

    public CooldownManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setDefaultCooldown(String command, long seconds) {
        defaultCooldowns.put(command.toLowerCase(), seconds * 1000);
    }

    public CooldownResult checkCooldown(Player player, String command, String permission) {
        if (permission != null && !permission.isEmpty() && !player.hasPermission(permission)) {
            return CooldownResult.allowed();
        }

        return checkCooldown(player, command, defaultCooldowns.getOrDefault(command.toLowerCase(), 0L));
    }

    public CooldownResult checkCooldown(Player player, String command, long cooldownMillis, String permission) {
        if (permission != null && !permission.isEmpty() && !player.hasPermission(permission)) {
            return CooldownResult.allowed();
        }

        return checkCooldown(player, command, cooldownMillis);
    }

    public CooldownResult checkCooldown(Player player, String command) {
        return checkCooldown(player, command, defaultCooldowns.getOrDefault(command.toLowerCase(), 0L));
    }

    public CooldownResult checkCooldown(Player player, String command, long cooldownMillis) {
        if (cooldownMillis <= 0 || player.hasPermission("fourcube.bypass.cooldown")) {
            return CooldownResult.allowed();
        }

        Map<UUID, Long> commandCooldowns = cooldowns.computeIfAbsent(
                command.toLowerCase(), k -> new HashMap<>()
        );

        long now = System.currentTimeMillis();
        Long lastUse = commandCooldowns.get(player.getUniqueId());

        if (lastUse != null) {
            long timeLeft = cooldownMillis - (now - lastUse);
            if (timeLeft > 0) {
                return CooldownResult.onCooldown(timeLeft);
            }
        }

        commandCooldowns.put(player.getUniqueId(), now);
        return CooldownResult.allowed();
    }

    public void clearCooldowns(Player player) {
        for (Map<UUID, Long> cmdCooldowns : cooldowns.values()) {
            cmdCooldowns.remove(player.getUniqueId());
        }
    }

    public static class CooldownResult {
        private final boolean allowed;
        private final long remainingMillis;

        private CooldownResult(boolean allowed, long remainingMillis) {
            this.allowed = allowed;
            this.remainingMillis = remainingMillis;
        }

        public static CooldownResult allowed() {
            return new CooldownResult(true, 0);
        }

        public static CooldownResult onCooldown(long remainingMillis) {
            return new CooldownResult(false, remainingMillis);
        }

        public boolean isAllowed() {
            return allowed;
        }

        public long getRemainingMillis() {
            return remainingMillis;
        }

        public long getRemainingSeconds() {
            return TimeUnit.MILLISECONDS.toSeconds(remainingMillis);
        }

        public Component getErrorMessage() {
            return Component.text("❌ Подождите ещё " + getRemainingSeconds() + " сек.", NamedTextColor.RED);
        }
    }

}

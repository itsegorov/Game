package me.egorov.plugin.library.inventory.handler;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ValidationHandler {

    private final Map<Integer, ValidationRule> slotRules;

    private final Map<String, ValidationRule> globalRules;

    public ValidationHandler() {
        this.slotRules = new HashMap<>();
        this.globalRules = new HashMap<>();
    }

    public void register(int slot, Predicate<Player> condition, String message) {
        slotRules.put(slot, new ValidationRule(condition, message));
    }

    public void registerGlobal(String ruleId, Predicate<Player> condition, String message) {
        globalRules.put(ruleId, new ValidationRule(condition, message));
    }

    public void requirePermission(int slot, String permission, String message) {
        register(slot, player -> player.hasPermission(permission), message);
    }

    public void requireGlobalPermission(String permission, String message) {
        registerGlobal("perm_" + permission, player -> player.hasPermission(permission), message);
    }

    public void requireMoney(int slot, double amount, String message) {
        register(slot, player -> {
            return true;
        }, message);
    }

    public void requireLevel(int slot, int level, String message) {
        register(slot, player -> player.getLevel() >= level, message);
    }

    public void requireItem(int slot, org.bukkit.Material material, int amount, String message) {
        register(slot, player -> {
            int count = 0;
            for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == material) {
                    count += item.getAmount();
                }
            }
            return count >= amount;
        }, message);
    }

    public boolean validate(int slot, Player player) {
        for (ValidationRule rule : globalRules.values()) {
            if (!rule.test(player)) {
                player.sendMessage(rule.getMessage());
                return false;
            }
        }

        if (slotRules.containsKey(slot)) {
            ValidationRule rule = slotRules.get(slot);
            if (!rule.test(player)) {
                player.sendMessage(rule.getMessage());
                return false;
            }
        }

        return true;
    }

    public boolean validate(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return false;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        return validate(slot, player);
    }

    public void unregister(int slot) {
        slotRules.remove(slot);
    }

    public void unregisterGlobal(String ruleId) {
        globalRules.remove(ruleId);
    }

    public void clear() {
        slotRules.clear();
        globalRules.clear();
    }

    public boolean hasRule(int slot) {
        return slotRules.containsKey(slot);
    }

    public boolean hasGlobalRules() {
        return !globalRules.isEmpty();
    }

    public int size() {
        return slotRules.size() + globalRules.size();
    }

    public ValidationHandler copy() {
        ValidationHandler copy = new ValidationHandler();
        copy.slotRules.putAll(this.slotRules);
        copy.globalRules.putAll(this.globalRules);
        return copy;
    }

    public static Predicate<Player> hasPermission(String permission) {
        return player -> player.hasPermission(permission);
    }

    public static Predicate<Player> lacksPermission(String permission) {
        return player -> !player.hasPermission(permission);
    }

    public static Predicate<Player> hasLevel(int level) {
        return player -> player.getLevel() >= level;
    }

    public static Predicate<Player> hasExp(int exp) {
        return player -> player.getTotalExperience() >= exp;
    }

    public static Predicate<Player> hasHealth(double health) {
        return player -> player.getHealth() >= health;
    }

    public static Predicate<Player> inWorld(String worldName) {
        return player -> player.getWorld().getName().equals(worldName);
    }

    public static Predicate<Player> isDay() {
        return player -> {
            long time = player.getWorld().getTime();
            return time < 12300 || time > 23850;
        };
    }

    public static Predicate<Player> isNight() {
        return player -> {
            long time = player.getWorld().getTime();
            return time >= 12300 && time <= 23850;
        };
    }

    public static Predicate<Player> inRegion(String region) {
        return player -> {
            return true;
        };
    }

    @SafeVarargs
    public static Predicate<Player> all(Predicate<Player>... conditions) {
        return player -> {
            for (Predicate<Player> condition : conditions) {
                if (!condition.test(player)) {
                    return false;
                }
            }
            return true;
        };
    }

    @SafeVarargs
    public static Predicate<Player> any(Predicate<Player>... conditions) {
        return player -> {
            for (Predicate<Player> condition : conditions) {
                if (condition.test(player)) {
                    return true;
                }
            }
            return false;
        };
    }

    public static Predicate<Player> not(Predicate<Player> condition) {
        return player -> !condition.test(player);
    }

    private static class ValidationRule {
        private final Predicate<Player> condition;
        private final String message;

        public ValidationRule(Predicate<Player> condition, String message) {
            this.condition = condition;
            this.message = message;
        }

        public boolean test(Player player) {
            return condition.test(player);
        }

        public String getMessage() {
            return message;
        }
    }
}

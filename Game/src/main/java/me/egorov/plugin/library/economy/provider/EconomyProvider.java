package me.egorov.plugin.library.economy.provider;

import org.bukkit.entity.Player;

public interface EconomyProvider {

    double balanceOf(Player player);

    default String format(double amount) {
        if (amount >= 1_000_000_000) {
            return String.format("%.1fтрлн.", amount / 1_000_000_000D);
        }
        if (amount >= 1_000_000) {
            return String.format("%.1fмлн.", amount / 1_000_000D);
        }
        if (amount >= 1_000) {
            return String.format("%.1fтыс.", amount / 1_000D);
        }
        if (amount == Math.rint(amount)) {
            return String.format("%.0f", amount);
        }
        return String.format("%.2f", amount);
    }

}

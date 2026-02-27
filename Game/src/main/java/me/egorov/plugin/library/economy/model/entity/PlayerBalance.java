package me.egorov.plugin.library.economy.model.entity;

import me.egorov.plugin.library.economy.model.Currency;
import org.jetbrains.annotations.NotNull;

public class PlayerBalance {

    private final @NotNull EconomyPlayer player;
    private final @NotNull Currency currency;
    private double amount;

    public PlayerBalance(@NotNull EconomyPlayer player, @NotNull Currency currency, double amount) {
        this.player = player;
        this.currency = currency;
        this.amount = amount;
    }

    public @NotNull EconomyPlayer player() {
        return player;
    }

    public @NotNull Currency currency() {
        return currency;
    }

    public double value() {
        return amount;
    }

    public void value(double amount) {
        this.amount = amount;
    }

}

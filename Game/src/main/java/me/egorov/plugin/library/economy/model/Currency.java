package me.egorov.plugin.library.economy.model;

import org.jetbrains.annotations.NotNull;

public class Currency {

    private final @NotNull String id;
    private final @NotNull String symbol;
    private final @NotNull String displayName;

    public Currency(@NotNull String id, @NotNull String symbol) {
        this(id, symbol, id);
    }

    public Currency(@NotNull String id, @NotNull String symbol, @NotNull String displayName) {
        this.id = id;
        this.symbol = symbol;
        this.displayName = displayName;
    }

    public @NotNull String getId() { return id; }
    public @NotNull String getSymbol() { return symbol; }
    public @NotNull String getDisplayName() { return displayName; }

}

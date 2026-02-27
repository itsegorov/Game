package me.egorov.plugin.library.rank.model;

import org.jetbrains.annotations.*;
import java.util.*;

public enum Rank {

    SRADMIN (290, "sradmin", "$4[SRADMIN]"),
    CURATOR (270, "curator", "$c[CURATOR]"),
    ADMIN (250, "admin", "$c[ADMIN]"),
    SRMODER (230, "srmoder", "$2[SRMODERATOR]"),
    MODER (210, "moder", "$2[MODERATOR]"),
    SRHELPER (190, "srhelper", "$2[SRHELPER]"),
    HELPER (170, "helper", "$2[HELPER]"),
    MEDIA (150, "media", "$2[MEDIA]"),

    OBSIDIAN (130, "obsidian", "$b[OBSIDIAN]"),
    AMETHYST (110, "amethyst", "$4[AMETHYST]"),
    RUBY (90, "ruby", "$2[RUBY]"),
    SAPPHIRE (70, "sapphire", "&[SAPPHIRE]"),
    CRYSTAL (50, "crystal", "[CRYSTAL]"),
    PLAYER (0, "player", "<gradient:#008cf0:#386646>[PLAYER]");

    private final int priority;

    @NotNull
    private final String id;

    @NotNull
    private final String prefix;

    Rank(int priority, @NotNull String id, @NotNull String prefix) {
        this.priority = priority;
        this.id = Objects.requireNonNull(id);
        this.prefix = Objects.requireNonNull(prefix);
    }

    @NotNull
    public static Rank fromString(@NotNull String id) {
        return Arrays.stream(values())
                .filter(rank -> rank.id.equalsIgnoreCase(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown rank: " + id));
    }

    @Nullable
    public static Rank fromStringSafe(@NotNull String id) {
        return Arrays.stream(values())
                .filter(rank -> rank.id.equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    public int priority() {
        return priority;
    }

    @NotNull
    public String id() {
        return id;
    }

    @NotNull
    public String prefix() {
        return prefix;
    }

    public boolean isAbove(@NotNull Rank other) {
        return this.priority > other.priority;
    }

    public boolean isBelow(@NotNull Rank other) {
        return this.priority < other.priority;
    }

    public boolean isEqual(@NotNull Rank other) {
        return this.priority == other.priority;
    }

    public boolean isAtLeast(@NotNull Rank other) {
        return this.priority >= other.priority;
    }

    public Rank next() {
        Rank[] values = values();
        int currentIndex = ordinal();
        if (currentIndex > 0) {
            return values[currentIndex - 1];
        }
        return this;
    }

    public Rank previous() {
        Rank[] values = values();
        int currentIndex = ordinal();
        if (currentIndex < values.length - 1) {
            return values[currentIndex + 1];
        }
        return this;
    }
}

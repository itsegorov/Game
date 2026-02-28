package me.egorov.plugin.library.rank.model;

import org.jetbrains.annotations.*;
import java.util.*;

public enum Rank {

    SRADMIN (290, "sradmin", "<gradient:#d36e70:#e3a9be><bold>ᴄᴛ.ᴀдᴍиʜ"),
    CURATOR (270, "curator", "<gradient:#75151e:#a0522d><bold>ᴋуᴘᴀᴛᴏᴘ"),
    ADMIN (250, "admin", "<gradient:#bb8b54:#270a1f><bold>ᴀдᴍиʜ"),
    SRMODER (230, "srmoder", "<gradient:#c8a2c8:#ffcc00><bold>ᴄᴛ.ᴍᴏдᴇᴘ"),
    MODER (210, "moder", "<gradient:#483c32:#1e1112><bold>ᴍᴏдᴇᴘ"),
    SRHELPER (190, "srhelper", "<gradient:#434b4d:#a65e2e><bold>ᴄᴛ.xᴇлпᴇᴘ"),
    HELPER (170, "helper", "<gradient:#fae7b5:#66ff00><bold>xᴇлпᴇᴘ"),
    MEDIA (150, "media", "<gradient:#898176:#f8173e><bold>ᴍᴇдиᴀ"),

    OBSIDIAN (130, "obsidian", "<gradient:#316650:#ffc0cb><bold>ᴏбᴄидиᴀʜ"),
    AMETHYST (110, "amethyst", "<gradient:#f4c430:#4d5d53><bold>ᴀᴍᴇᴛиᴄᴛ"),
    RUBY (90, "ruby", "<gradient:#ffe2b7:#21421e><bold>ᴘубиʜ"),
    SAPPHIRE (70, "sapphire", "<gradient:#9966cc:#9f8200><bold>ᴄᴀпфиᴘ"),
    CRYSTAL (50, "crystal", "<gradient:#95918c:#00538a><bold>ᴋᴘиᴄᴛᴀлл"),
    PLAYER (0, "player", "<gradient:#008cf0:#386646><bold>игᴘᴏᴋ");

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

package me.egorov.plugin.library.economy.model.entity;

import org.jetbrains.annotations.NotNull;
import java.util.UUID;

public record EconomyPlayer(@NotNull UUID uuid, @NotNull String name) {}

package me.egorov.plugin.game.module.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.egorov.plugin.library.command.commander.AbstractCommand;
import me.egorov.plugin.library.economy.model.Currency;
import me.egorov.plugin.library.economy.model.entity.EconomyPlayer;
import me.egorov.plugin.library.economy.service.EconomyService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class EconomyCommand extends AbstractCommand {

    private final EconomyService economyService;

    public EconomyCommand(EconomyService economyService) {
        super("money", "Тестовая команда валюты", "eco, balance, bal");
        this.economyService = economyService;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return withStandardRequirements(LiteralArgumentBuilder.<CommandSourceStack>literal(getName())
                // money
                .executes(context -> {
                    if (getOptionalPlayer(context).isEmpty()) {
                        return 0;
                    }
                    Player player = getOptionalPlayer(context).get();

                    economyService.find(player.getUniqueId()).thenAccept(targetPlayer -> {
                        if (targetPlayer.isEmpty()) return;

                        EconomyPlayer economyPlayer = targetPlayer.get();

                        success(context, "Ваш игровой баланс:");
                        for (Currency currency : currencies) {
                            economyService.getBalance(economyPlayer, currency).thenAccept(balance -> {
                                Component message = Component.text()
                                        .append(Component.text(String.format("%-10s", currency.getDisplayName() + ": ")))
                                        .append(Component.text(String.format("%.2f", balance)))
                                        .append(Component.text(" " + currency.getSymbol()))
                                        .build();

                                sendMessage(context, message);
                            });
                        }
                    });
                    return 1;
                })

                // /money <nick>
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("игрок", StringArgumentType.word())
                        .suggests(suggestPlayers())
                        .executes(context -> {
                            String targetName = getString(context, "игрок");
                            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

                            if (target.getName() == null) {
                                error(context, "Игрок не найден");
                                return 0;
                            }

                            economyService.find(target.getUniqueId()).thenAccept(targetPlayer -> {
                                if (targetPlayer.isPresent()) {
                                    EconomyPlayer targetEconomyPlayer = targetPlayer.get();

                                    success(context, "Игровой баланс игрока " + target.getName() + ":");
                                    for (Currency currency : currencies) {
                                        economyService.getBalance(targetEconomyPlayer, currency).thenAccept(balance -> {
                                            Component message = Component.text()
                                                    .append(Component.text(String.format("%-10s", currency.getDisplayName() + ": ")))
                                                    .append(Component.text(String.format("%.2f", balance)))
                                                    .append(Component.text(" " + currency.getSymbol()))
                                                    .build();

                                            sendMessage(context, message);
                                        });
                                    }
                                }
                                sendMessage(context, "test");
                            });
                            return 1;
                        })
                )

                // /money give <player> <amount> [type]
                .then(literal("give")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("игрок", StringArgumentType.word())
                                .suggests(suggestPlayers())
                                .then(RequiredArgumentBuilder.<CommandSourceStack, Double>argument("сумма", DoubleArgumentType.doubleArg(10.0))
                                    .executes(context -> {
                                        String targetName = getString(context, "игрок");
                                        double amount = DoubleArgumentType.getDouble(context, "сумма");

                                        Currency defaultCurrency = currencies.stream()
                                                .filter(c -> c.getId().equals("money"))
                                                .findFirst().get();

                                        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

                                        if (target.getName() == null) {
                                            error(context, "Игрок не найден");
                                            return 0;
                                        }

                                        economyService.find(target.getUniqueId()).thenAccept(targetPlayer -> {
                                            if (targetPlayer.isEmpty()) return;
                                            EconomyPlayer targetEconomyPlayer = targetPlayer.get();
                                            economyService.deposit(targetEconomyPlayer, defaultCurrency, amount);
                                            success(context, "Успешно! (OfflinePlayer#" + targetName + ", DepositBalance=" + amount + ", Type=" + defaultCurrency.getId());

                                            if (target.isOnline() && target.getPlayer() != null) {
                                                target.getPlayer().sendMessage("Ваш баланс пополнен на " + amount + defaultCurrency.getSymbol());
                                            }
                                        });
                                        return 1;
                                    })
                                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("валюта", StringArgumentType.word())
                                                .suggests(suggestCurrencies())
                                                .executes(context -> {
                                                    String targetName = getString(context, "игрок");
                                                    double amount = DoubleArgumentType.getDouble(context, "сумма");
                                                    String currencyId = StringArgumentType.getString(context, "валюта");

                                                    Currency currency = currencies.stream()
                                                            .filter(c -> c.getId().equalsIgnoreCase(currencyId))
                                                            .findFirst()
                                                            .orElse(null);

                                                    if (currency == null) {
                                                        error(context, "Валюта не найдена");
                                                        return 0;
                                                    }

                                                    OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

                                                    if (target.getName() == null) {
                                                        error(context, "Игрок не найден");
                                                        return 0;
                                                    }

                                                    economyService.find(target.getUniqueId()).thenAccept(targetPlayer -> {
                                                        if (targetPlayer.isEmpty()) return;
                                                        EconomyPlayer targetEconomyPlayer = targetPlayer.get();
                                                        economyService.deposit(targetEconomyPlayer, currency, amount);
                                                        success(context, "Успешно! (OfflinePlayer#" + targetName + ", DepositBalance=" + amount + ", Type=" + currency.getId());

                                                        if (target.isOnline() && target.getPlayer() != null) {
                                                            target.getPlayer().sendMessage("Ваш баланс пополнен на " + amount + currency.getSymbol());
                                                        }
                                                    });
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )

                // /money take <player> <amount> [type]
                .then(literal("take")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("игрок", StringArgumentType.word())
                                .suggests(suggestPlayers())
                                .then(RequiredArgumentBuilder.<CommandSourceStack, Double>argument("сумма", DoubleArgumentType.doubleArg(10.0))
                                        .executes(context -> {
                                            String targetName = getString(context, "игрок");
                                            double amount = DoubleArgumentType.getDouble(context, "сумма");

                                            Currency defaultCurrency = currencies.stream()
                                                    .filter(c -> c.getId().equals("money"))
                                                    .findFirst().get();

                                            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

                                            if (target.getName() == null) {
                                                error(context, "Игрок не найден");
                                                return 0;
                                            }

                                            economyService.find(target.getUniqueId()).thenAccept(targetPlayer -> {
                                                if (targetPlayer.isEmpty()) return;
                                                EconomyPlayer targetEconomyPlayer = targetPlayer.get();
                                                economyService.withdraw(targetEconomyPlayer, defaultCurrency, amount);
                                                success(context, "Успешно! (OfflinePlayer#" + targetName + ", WithdrawBalance=" + amount + ", Type=" + defaultCurrency.getId());

                                                if (target.isOnline() && target.getPlayer() != null) {
                                                    target.getPlayer().sendMessage("Ваш баланс пополнен на " + amount + defaultCurrency.getSymbol());
                                                }
                                            });
                                            return 1;
                                        })
                                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("валюта", StringArgumentType.word())
                                                .suggests(suggestCurrencies())
                                                .executes(context -> {
                                                    String targetName = getString(context, "игрок");
                                                    double amount = DoubleArgumentType.getDouble(context, "сумма");
                                                    String currencyId = StringArgumentType.getString(context, "валюта");

                                                    Currency currency = currencies.stream()
                                                            .filter(c -> c.getId().equalsIgnoreCase(currencyId))
                                                            .findFirst()
                                                            .orElse(null);

                                                    if (currency == null) {
                                                        error(context, "Валюта не найдена");
                                                        return 0;
                                                    }

                                                    OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

                                                    if (target.getName() == null) {
                                                        error(context, "Игрок не найден");
                                                        return 0;
                                                    }

                                                    economyService.find(target.getUniqueId()).thenAccept(targetPlayer -> {
                                                        if (targetPlayer.isEmpty()) return;
                                                        EconomyPlayer targetEconomyPlayer = targetPlayer.get();
                                                        economyService.withdraw(targetEconomyPlayer, currency, amount);
                                                        success(context, "Успешно! (OfflinePlayer#" + targetName + ", WithdrawBalance=" + amount + ", Type=" + currency.getId());

                                                        if (target.isOnline() && target.getPlayer() != null) {
                                                            target.getPlayer().sendMessage("Ваш баланс пополнен на " + amount + currency.getSymbol());
                                                        }
                                                    });
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )

                // /money set <player> <amount> [type]
                .then(literal("set")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("игрок", StringArgumentType.word())
                                .suggests(suggestPlayers())
                                .then(RequiredArgumentBuilder.<CommandSourceStack, Double>argument("сумма", DoubleArgumentType.doubleArg(10.0))
                                        .executes(context -> {
                                            String targetName = getString(context, "игрок");
                                            double amount = DoubleArgumentType.getDouble(context, "сумма");

                                            Currency defaultCurrency = currencies.stream()
                                                    .filter(c -> c.getId().equals("money"))
                                                    .findFirst().get();

                                            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

                                            if (target.getName() == null) {
                                                error(context, "Игрок не найден");
                                                return 0;
                                            }

                                            economyService.find(target.getUniqueId()).thenAccept(targetPlayer -> {
                                                if (targetPlayer.isEmpty()) return;
                                                EconomyPlayer targetEconomyPlayer = targetPlayer.get();
                                                economyService.setBalance(targetEconomyPlayer, defaultCurrency, amount);
                                                success(context, "Успешно! (OfflinePlayer#" + targetName + ", SetBalance=" + amount + ", Type=" + defaultCurrency.getId());

                                                if (target.isOnline() && target.getPlayer() != null) {
                                                    target.getPlayer().sendMessage("Ваш баланс пополнен на " + amount + defaultCurrency.getSymbol());
                                                }
                                            });
                                            return 1;
                                        })
                                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("валюта", StringArgumentType.word())
                                                .suggests(suggestCurrencies())
                                                .executes(context -> {
                                                    String targetName = getString(context, "игрок");
                                                    double amount = DoubleArgumentType.getDouble(context, "сумма");
                                                    String currencyId = StringArgumentType.getString(context, "валюта");

                                                    Currency currency = currencies.stream()
                                                            .filter(c -> c.getId().equalsIgnoreCase(currencyId))
                                                            .findFirst()
                                                            .orElse(null);

                                                    if (currency == null) {
                                                        error(context, "Валюта не найдена");
                                                        return 0;
                                                    }

                                                    OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

                                                    if (target.getName() == null) {
                                                        error(context, "Игрок не найден");
                                                        return 0;
                                                    }

                                                    economyService.find(target.getUniqueId()).thenAccept(targetPlayer -> {
                                                        if (targetPlayer.isEmpty()) return;
                                                        EconomyPlayer targetEconomyPlayer = targetPlayer.get();
                                                        economyService.setBalance(targetEconomyPlayer, currency, amount);
                                                        success(context, "Успешно! (OfflinePlayer#" + targetName + ", SetBalance=" + amount + ", Type=" + currency.getId());

                                                        if (target.isOnline() && target.getPlayer() != null) {
                                                            target.getPlayer().sendMessage("Ваш баланс пополнен на " + amount + currency.getSymbol());
                                                        }
                                                    });
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )
        );
    }
}

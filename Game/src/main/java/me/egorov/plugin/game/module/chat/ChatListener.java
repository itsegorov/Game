package me.egorov.plugin.game.module.chat;

import me.egorov.plugin.library.rank.wrapper.RankManagerWrapper;
import me.egorov.plugin.utility.StringUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatListener implements Listener {

    private static RankManagerWrapper manager;

    public ChatListener(RankManagerWrapper manager) {
        this.manager = manager;
    }

    private static final Pattern URL_PATTERN = Pattern.compile("(?:(https?)://)?([-\\w_.]{2,}\\.[a-z]{2,4})(/\\S*)?");

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {event.joinMessage(Component.empty());}

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {event.deathMessage(Component.empty());}

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {event.quitMessage(Component.empty());}

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        sendChatComponent(event);
    }

    @SuppressWarnings("deprecation")
    private void sendChatComponent(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        event.setCancelled(true);

        Component component = Component.text("")
                .append(Component.text("▎")
                        .decorate(TextDecoration.BOLD)
                        .color(TextColor.color(0, 255, 0)))
                .appendSpace();

        Optional<String> prefixOpt = Optional.empty();
        if (manager != null  && manager.isEnabled()) {
            prefixOpt = manager.getPlayerPrefix(player);
        }

        if (prefixOpt.isPresent()) {
            Component prefixComponent = StringUtility.parseString(prefixOpt.get());
            component = component.append(prefixComponent);
        }

        component = component
                .appendSpace()
                .append(Component.text(player.getName()))
                .appendSpace()
                .append(Component.text("→").color(TextColor.color(85, 85, 85)))
                .appendSpace()
                .append(Component.text(preventMessage(player, event.getMessage())));


        for (Player recipient : event.getRecipients()) {
            recipient.sendMessage(component);
        }
        Bukkit.getConsoleSender().sendMessage(component);
    }

    private static String preventMessage(Player player, String message) {
        Matcher matcher = URL_PATTERN.matcher(message);

        if (!matcher.find()) {
            return message;
        }

        matcher.reset();

        Set<String> allowedDomains = Set.of(
                "google.com",
                "youtube.com"
        );

        boolean canBypass = false;

        if (manager != null  && manager.isEnabled()) {
            canBypass = manager.isInAnyGroup(player,
                    "sradmin",
                    "curator",
                    "admin",
                    "srmoder"
            );
        }

        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            String host = matcher.group(2);
            String fullUrl = matcher.group(0);

            result.append(message, lastEnd, matcher.start());

            boolean allowed = canBypass || (host != null && allowedDomains.stream().anyMatch(host::endsWith));

            if (allowed) {
                result.append(fullUrl);
            } else {
                result.append("<ссылка удалена>");
            }

            lastEnd = matcher.end();
        }

        result.append(message.substring(lastEnd));

        return result.toString();
    }
}

package me.egorov.plugin.library.inventory.builder;

import me.egorov.plugin.library.inventory.animation.CloseAnimation;
import me.egorov.plugin.library.inventory.animation.OpenAnimation;
import me.egorov.plugin.library.inventory.animation.object.TransitionType;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public interface MenuBuilder {

    MenuBuilder holder(InventoryHolder holder);

    MenuBuilder size(int size);

    MenuBuilder type(InventoryType type);

    MenuBuilder title(Component title);

    MenuBuilder title(String legacyTitle);

    MenuBuilder titleMini(String miniMessage);

    MenuBuilder set(int slot, ItemStack item);

    MenuBuilder set(int slot, ItemStack item, Consumer<InventoryClickEvent> handler);

    MenuBuilder set(int slot, ItemStack item, Consumer<InventoryClickEvent> handler,
                    Predicate<Player> validator, String validatorMessage);

    MenuBuilder onClick(int slot, Consumer<InventoryClickEvent> handler);

    MenuBuilder onAnyClick(Consumer<InventoryClickEvent> handler);

    MenuBuilder pattern(String[] pattern, Map<Character, ItemStack> mapping);

    MenuBuilder withBorder();

    MenuBuilder withBorder(Material material, Component name);

    MenuBuilder withBorderMini(Material material, String miniName);

    MenuBuilder withOpenAnimation(OpenAnimation animation);

    MenuBuilder withCloseAnimation(CloseAnimation animation);

    MenuBuilder withItemAnimation(int intervalTicks, List<ItemStack> frames, int... slots);

    MenuBuilder paginated(List<ItemStack> items, int itemsPerPage);

    MenuBuilder pageTransition(TransitionType type);

    MenuBuilder transitionDuration(int ticks);

    MenuBuilder pageTitleGenerator(Function<Integer, Component> generator);

    MenuBuilder openSound(Sound sound);

    MenuBuilder closeSound(Sound sound);

    MenuBuilder clickSound(Sound sound);

    MenuBuilder pageSound(Sound sound);

    MenuBuilder soundVolume(float volume);

    MenuBuilder soundPitch(float pitch);

    MenuBuilder requirePermission(String permission, String message);

    MenuBuilder requireCondition(Predicate<Player> condition, String message);

    MenuBuilder validateClick(int slot, Predicate<Player> condition, String message);

    Inventory build();

    Inventory buildAndOpen(Player player);
}

package me.egorov.plugin.library.inventory.builder;

import me.egorov.plugin.library.inventory.container.ContainerHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ItemBuilder {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final ItemStack itemStack;
    private ItemMeta itemMeta;
    private final me.egorov.plugin.library.inventory.container.ContainerHelper ContainerHelper;

    private ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
        this.itemMeta = itemStack.getItemMeta();
        this.ContainerHelper = new ContainerHelper();
    }

    private ItemBuilder(ItemStack existing) {
        this.itemStack = existing.clone();
        this.itemMeta = itemStack.getItemMeta();
        this.ContainerHelper = new ContainerHelper();
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    public static ItemBuilder of(ItemStack item) {
        return new ItemBuilder(item);
    }

    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder nameMini(String miniMessage) {
        Component component = MINI_MESSAGE.deserialize(miniMessage);
        itemMeta.displayName(component);
        return this;
    }

    public ItemBuilder name(Component component) {
        itemMeta.displayName(component);
        return this;
    }

    public ItemBuilder name(String legacyText) {
        Component component = LegacyComponentSerializer.legacySection().deserialize(legacyText);
        itemMeta.displayName(component);
        return this;
    }

    public ItemBuilder loreMini(String... miniLines) {
        List<Component> lore = new ArrayList<>();
        for (String line : miniLines) {
            lore.add(MINI_MESSAGE.deserialize(line));
        }
        itemMeta.lore(lore);
        return this;
    }

    public ItemBuilder loreMini(List<String> miniLines) {
        List<Component> lore = new ArrayList<>();
        for (String line : miniLines) {
            lore.add(MINI_MESSAGE.deserialize(line));
        }
        itemMeta.lore(lore);
        return this;
    }

    public ItemBuilder lore(Component... components) {
        itemMeta.lore(Arrays.asList(components));
        return this;
    }

    public ItemBuilder lore(List<Component> components) {
        itemMeta.lore(components);
        return this;
    }

    public ItemBuilder lore(String... legacyLines) {
        List<Component> lore = new ArrayList<>();
        for (String line : legacyLines) {
            lore.add(LegacyComponentSerializer.legacySection().deserialize(line));
        }
        itemMeta.lore(lore);
        return this;
    }

    public ItemBuilder lore(List<String> legacyLines, boolean legacy) {
        List<Component> lore = new ArrayList<>();
        for (String line : legacyLines) {
            lore.add(LegacyComponentSerializer.legacySection().deserialize(line));
        }
        itemMeta.lore(lore);
        return this;
    }

    public ItemBuilder addLoreMini(String miniLine) {
        List<Component> currentLore = itemMeta.lore();
        if (currentLore == null) {
            currentLore = new ArrayList<>();
        }
        currentLore.add(MINI_MESSAGE.deserialize(miniLine));
        itemMeta.lore(currentLore);
        return this;
    }

    public ItemBuilder addLore(String legacyLine) {
        List<Component> currentLore = itemMeta.lore();
        if (currentLore == null) {
            currentLore = new ArrayList<>();
        }
        currentLore.add(LegacyComponentSerializer.legacySection().deserialize(legacyLine));
        itemMeta.lore(currentLore);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        itemMeta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder enchant(Enchantment... enchantments) {
        for (Enchantment enchant : enchantments) {
            itemMeta.addEnchant(enchant, 1, true);
        }
        return this;
    }

    public ItemBuilder glowing() {
        itemMeta.addEnchant(Enchantment.LURE, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        itemMeta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder hideAll() {
        itemMeta.addItemFlags(
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_DYE
        );
        return this;
    }

    public ItemBuilder damage(int damage) {
        if (itemMeta instanceof org.bukkit.inventory.meta.Damageable) {
            ((org.bukkit.inventory.meta.Damageable) itemMeta).setDamage(damage);
        }
        return this;
    }

    public ItemBuilder unbreakable() {
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        return this;
    }

    public ItemBuilder withPDC(String key, String value) {
        ContainerHelper.setString(key, value);
        return this;
    }

    public ItemBuilder withPDC(String key, int value) {
        ContainerHelper.setInt(key, value);
        return this;
    }

    public ItemBuilder withPDC(String key, boolean value) {
        ContainerHelper.setBoolean(key, value);
        return this;
    }

    public ItemBuilder withPDC(NamespacedKey key, String value) {
        ContainerHelper.setString(key, value);
        return this;
    }

    public <T, Z> ItemBuilder withPDC(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        ContainerHelper.set(key, type, value);
        return this;
    }

    private void applyPDC() {
        if (ContainerHelper.isEmpty()) return;


        ContainerHelper.applyTo(itemMeta);
    }

    public ItemBuilder customModelData(int data) {
        itemMeta.setCustomModelData(data);
        return this;
    }

    public ItemBuilder modifyMeta(Consumer<ItemMeta> modifier) {
        modifier.accept(itemMeta);
        return this;
    }

    public ItemBuilder modifyStack(Consumer<ItemStack> modifier) {
        modifier.accept(itemStack);
        return this;
    }

    public ItemStack build() {
        applyPDC();

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public static ItemStack quick(Material material, String name) {
        return of(material).name(name).build();
    }

    public static ItemStack quick(Material material, String name, String... loreLines) {
        return of(material).name(name).lore(loreLines).build();
    }

    public static ItemStack separator(Material glassType) {
        return of(glassType).name(" ").build();
    }

    public static ItemStack button(Material material, String name, String... loreLines) {
        return of(material).name(name).lore(loreLines).build();
    }

    public static ItemStack button(Material material, String name, String action, String... loreLines) {
        return of(material)
                .name(name)
                .lore(loreLines)
                .withPDC("action", action)
                .build();
    }

    public static ItemStack empty() {
        return new ItemStack(Material.AIR);
    }
}
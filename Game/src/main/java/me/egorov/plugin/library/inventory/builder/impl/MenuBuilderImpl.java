package me.egorov.plugin.library.inventory.builder.impl;

import me.egorov.plugin.GamePlugin;
import me.egorov.plugin.library.inventory.animation.CloseAnimation;
import me.egorov.plugin.library.inventory.animation.OpenAnimation;
import me.egorov.plugin.library.inventory.animation.object.TransitionType;
import me.egorov.plugin.library.inventory.builder.ItemBuilder;
import me.egorov.plugin.library.inventory.builder.MenuBuilder;
import me.egorov.plugin.library.inventory.handler.ClickHandler;
import me.egorov.plugin.library.inventory.handler.SoundHandler;
import me.egorov.plugin.library.inventory.handler.ValidationHandler;
import me.egorov.plugin.library.inventory.session.MenuSession;
import me.egorov.plugin.library.inventory.session.PaginatedSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class MenuBuilderImpl implements MenuBuilder {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final GamePlugin plugin;

    private InventoryHolder holder;
    private int size;
    private InventoryType type;
    private Component title;
    private String titleMini;

    private final Map<Integer, ItemStack> items = new HashMap<>();
    private final ClickHandler clickHandler;
    private final ValidationHandler validationHandler;
    private final SoundHandler soundHandler;

    private String[] pattern;
    private Map<Character, ItemStack> patternMapping;
    private boolean bordered = false;
    private Material borderMaterial = Material.GRAY_STAINED_GLASS_PANE;
    private Component borderName = Component.text(" ");
    private String borderNameMini;

    private OpenAnimation openAnimation;
    private CloseAnimation closeAnimation;
    private final List<ItemAnimation> itemAnimations = new ArrayList<>();

    private boolean paginated = false;
    private List<ItemStack> pageItems;
    private int itemsPerPage = 45;
    private TransitionType transitionType = TransitionType.FADE;
    private int transitionDuration = 10;
    private Function<Integer, Component> pageTitleGenerator = page -> Component.text("Страница " + (page + 1));

    private Predicate<Player> openCondition;
    private String openConditionMessage;

    private static class ItemAnimation {
        final int interval;
        final List<ItemStack> frames;
        final int[] slots;

        ItemAnimation(int interval, List<ItemStack> frames, int[] slots) {
            this.interval = interval;
            this.frames = frames;
            this.slots = slots;
        }
    }

    public MenuBuilderImpl() {
        this.plugin = GamePlugin.instance();
        this.clickHandler = new ClickHandler();
        this.validationHandler = new ValidationHandler();
        this.soundHandler = new SoundHandler();
    }

    @Override
    public MenuBuilder holder(InventoryHolder holder) {
        this.holder = holder;
        return this;
    }

    @Override
    public MenuBuilder size(int size) {
        if (size % 9 != 0) {
            throw new IllegalArgumentException("Размер инвентаря должен быть кратен 9");
        }
        this.size = size;
        this.type = null;
        return this;
    }

    @Override
    public MenuBuilder type(InventoryType type) {
        this.type = type;
        this.size = 0;
        return this;
    }

    @Override
    public MenuBuilder title(Component title) {
        this.title = title;
        this.titleMini = null;
        return this;
    }

    @Override
    public MenuBuilder title(String legacyTitle) {
        this.title = LegacyComponentSerializer.legacySection().deserialize(legacyTitle);
        this.titleMini = null;
        return this;
    }

    @Override
    public MenuBuilder titleMini(String miniMessage) {
        this.titleMini = miniMessage;
        this.title = MINI_MESSAGE.deserialize(miniMessage);
        return this;
    }

    @Override
    public MenuBuilder set(int slot, ItemStack item) {
        items.put(slot, item);
        return this;
    }

    @Override
    public MenuBuilder set(int slot, ItemStack item, Consumer<InventoryClickEvent> handler) {
        items.put(slot, item);
        clickHandler.register(slot, handler);
        return this;
    }

    @Override
    public MenuBuilder set(int slot, ItemStack item, Consumer<InventoryClickEvent> handler,
                           Predicate<Player> validator, String validatorMessage) {
        items.put(slot, item);
        clickHandler.register(slot, handler);
        validationHandler.register(slot, validator, validatorMessage);
        return this;
    }

    @Override
    public MenuBuilder onClick(int slot, Consumer<InventoryClickEvent> handler) {
        clickHandler.register(slot, handler);
        return this;
    }

    @Override
    public MenuBuilder onAnyClick(Consumer<InventoryClickEvent> handler) {
        clickHandler.registerGlobal(handler);
        return this;
    }

    @Override
    public MenuBuilder pattern(String[] pattern, Map<Character, ItemStack> mapping) {
        this.pattern = pattern;
        this.patternMapping = mapping;
        return this;
    }

    @Override
    public MenuBuilder withBorder() {
        this.bordered = true;
        return this;
    }

    @Override
    public MenuBuilder withBorder(Material material, Component name) {
        this.bordered = true;
        this.borderMaterial = material;
        this.borderName = name;
        return this;
    }

    @Override
    public MenuBuilder withBorderMini(Material material, String miniName) {
        this.bordered = true;
        this.borderMaterial = material;
        this.borderNameMini = miniName;
        this.borderName = MINI_MESSAGE.deserialize(miniName);
        return this;
    }

    @Override
    public MenuBuilder withOpenAnimation(OpenAnimation animation) {
        this.openAnimation = animation;
        return this;
    }

    @Override
    public MenuBuilder withCloseAnimation(CloseAnimation animation) {
        this.closeAnimation = animation;
        return this;
    }

    @Override
    public MenuBuilder withItemAnimation(int intervalTicks, List<ItemStack> frames, int... slots) {
        itemAnimations.add(new ItemAnimation(intervalTicks, frames, slots));
        return this;
    }

    @Override
    public MenuBuilder paginated(List<ItemStack> items, int itemsPerPage) {
        this.paginated = true;
        this.pageItems = items;
        this.itemsPerPage = itemsPerPage;
        return this;
    }

    @Override
    public MenuBuilder pageTransition(TransitionType type) {
        this.transitionType = type;
        return this;
    }

    @Override
    public MenuBuilder transitionDuration(int ticks) {
        this.transitionDuration = ticks;
        return this;
    }

    @Override
    public MenuBuilder pageTitleGenerator(Function<Integer, Component> generator) {
        this.pageTitleGenerator = generator;
        return this;
    }

    @Override
    public MenuBuilder openSound(Sound sound) {
        soundHandler.setOpenSound(sound);
        return this;
    }

    @Override
    public MenuBuilder closeSound(Sound sound) {
        soundHandler.setCloseSound(sound);
        return this;
    }

    @Override
    public MenuBuilder clickSound(Sound sound) {
        soundHandler.setClickSound(sound);
        return this;
    }

    @Override
    public MenuBuilder pageSound(Sound sound) {
        soundHandler.setPageSound(sound);
        return this;
    }

    @Override
    public MenuBuilder soundVolume(float volume) {
        soundHandler.setVolume(volume);
        return this;
    }

    @Override
    public MenuBuilder soundPitch(float pitch) {
        soundHandler.setPitch(pitch);
        return this;
    }

    @Override
    public MenuBuilder requirePermission(String permission, String message) {
        this.openCondition = p -> p.hasPermission(permission);
        this.openConditionMessage = message;
        return this;
    }

    @Override
    public MenuBuilder requireCondition(Predicate<Player> condition, String message) {
        this.openCondition = condition;
        this.openConditionMessage = message;
        return this;
    }

    @Override
    public MenuBuilder validateClick(int slot, Predicate<Player> condition, String message) {
        validationHandler.register(slot, condition, message);
        return this;
    }

    @Override
    public Inventory build() {
        validateBuilder();
        Inventory inv = createInventory();

        if (paginated) {
            return createPaginatedInventory();
        }

        fillItems(inv);
        applyPattern(inv);
        applyBorder(inv);

        return inv;
    }

    @Override
    public Inventory buildAndOpen(Player player) {
        if (openCondition != null && !openCondition.test(player)) {
            player.sendMessage(openConditionMessage);
            return null;
        }

        soundHandler.playOpenSound(player);

        Inventory inv = build();

        if (openAnimation != null) {
            openAnimation.play(player, inv, clickHandler, validationHandler, soundHandler, closeAnimation);
        } else {
            player.openInventory(inv);
            MenuSession session = new MenuSession(player.getUniqueId(), inv, clickHandler, validationHandler, soundHandler);
            session.setCloseAnimation(closeAnimation);
            MenuSession.register(session);
        }

        return inv;
    }

    private void validateBuilder() {
        if (type == null && size == 0) {
            throw new IllegalStateException("Не указан размер или тип инвентаря");
        }
        if (title == null && titleMini == null) {
            throw new IllegalStateException("Не указано название инвентаря");
        }
    }

    private Inventory createInventory() {
        Component finalTitle = title != null ? title : Component.text("Меню");

        if (type != null) {
            return Bukkit.createInventory(holder, type, finalTitle);
        } else {
            return Bukkit.createInventory(holder, size, finalTitle);
        }
    }

    private void fillItems(Inventory inv) {
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            if (entry.getKey() < inv.getSize()) {
                inv.setItem(entry.getKey(), entry.getValue().clone());
            }
        }
    }

    private void applyPattern(Inventory inv) {
        if (pattern == null || patternMapping == null) return;

        for (int row = 0; row < pattern.length; row++) {
            String line = pattern[row];
            for (int col = 0; col < line.length(); col++) {
                char c = line.charAt(col);
                if (patternMapping.containsKey(c)) {
                    int slot = row * 9 + col;
                    if (slot < inv.getSize()) {
                        inv.setItem(slot, patternMapping.get(c).clone());
                    }
                }
            }
        }
    }

    private void applyBorder(Inventory inv) {
        if (!bordered) return;

        Component finalBorderName = borderNameMini != null ?
                MINI_MESSAGE.deserialize(borderNameMini) : borderName;

        ItemStack borderItem = ItemBuilder.of(borderMaterial)
                .name(finalBorderName)
                .build();

        int rows = inv.getSize() / 9;

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, borderItem.clone());
            inv.setItem((rows - 1) * 9 + i, borderItem.clone());
        }

        if (rows > 2) {
            for (int r = 1; r < rows - 1; r++) {
                inv.setItem(r * 9, borderItem.clone());
                inv.setItem(r * 9 + 8, borderItem.clone());
            }
        }
    }

    private Inventory createPaginatedInventory() {
        Inventory baseInv = createInventory();

        PaginatedSession session = new PaginatedSession(
                baseInv,
                pageItems,
                itemsPerPage,
                pageTitleGenerator,
                clickHandler,
                validationHandler,
                soundHandler,
                transitionType,
                transitionDuration
        );

        if (holder instanceof Player) {
            Player player = (Player) holder;
            PaginatedSession.register(player.getUniqueId(), session);
            return session.openPage(0, player);
        }

        return session.openPage(0, null);
    }

    public List<ItemAnimation> getItemAnimations() {
        return itemAnimations;
    }

    public OpenAnimation getOpenAnimation() {
        return openAnimation;
    }

    public CloseAnimation getCloseAnimation() {
        return closeAnimation;
    }

    public SoundHandler getSoundHandler() {
        return soundHandler;
    }

    public ClickHandler getClickHandler() {
        return clickHandler;
    }

    public ValidationHandler getValidationHandler() {
        return validationHandler;
    }
}
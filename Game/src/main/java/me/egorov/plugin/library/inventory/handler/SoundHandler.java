package me.egorov.plugin.library.inventory.handler;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SoundHandler {

    private Sound openSound;
    private Sound closeSound;
    private Sound clickSound;
    private Sound pageSound;
    private float volume = 1.0f;
    private float pitch = 1.0f;

    public void setOpenSound(Sound openSound) {
        this.openSound = openSound;
    }

    public void setCloseSound(Sound closeSound) {
        this.closeSound = closeSound;
    }

    public void setClickSound(Sound clickSound) {
        this.clickSound = clickSound;
    }

    public void setPageSound(Sound pageSound) {
        this.pageSound = pageSound;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public Sound getOpenSound() {
        return openSound;
    }

    public Sound getCloseSound() {
        return closeSound;
    }

    public Sound getClickSound() {
        return clickSound;
    }

    public Sound getPageSound() {
        return pageSound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

    public void playOpenSound(Player player) {
        if (openSound != null && player != null && player.isOnline()) {
            player.playSound(player.getLocation(), openSound, volume, pitch);
        }
    }

    public void playCloseSound(Player player) {
        if (closeSound != null && player != null && player.isOnline()) {
            player.playSound(player.getLocation(), closeSound, volume, pitch);
        }
    }

    public void playClickSound(Player player) {
        if (clickSound != null && player != null && player.isOnline()) {
            player.playSound(player.getLocation(), clickSound, volume, pitch);
        }
    }

    public void playClickSound(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            playClickSound((Player) event.getWhoClicked());
        }
    }

    public void playPageSound(Player player) {
        if (pageSound != null && player != null && player.isOnline()) {
            player.playSound(player.getLocation(), pageSound, volume, pitch);
        } else if (clickSound != null) {
            player.playSound(player.getLocation(), clickSound, volume, pitch);
        }
    }

    public boolean hasSounds() {
        return openSound != null || closeSound != null || clickSound != null || pageSound != null;
    }

    public boolean hasOpenSound() {
        return openSound != null;
    }

    public boolean hasCloseSound() {
        return closeSound != null;
    }

    public boolean hasClickSound() {
        return clickSound != null;
    }

    public boolean hasPageSound() {
        return pageSound != null;
    }

    public void clear() {
        openSound = null;
        closeSound = null;
        clickSound = null;
        pageSound = null;
    }

    public SoundHandler copy() {
        SoundHandler copy = new SoundHandler();
        copy.openSound = this.openSound;
        copy.closeSound = this.closeSound;
        copy.clickSound = this.clickSound;
        copy.pageSound = this.pageSound;
        copy.volume = this.volume;
        copy.pitch = this.pitch;
        return copy;
    }

    public static SoundHandler defaultSounds() {
        SoundHandler handler = new SoundHandler();
        handler.setOpenSound(Sound.BLOCK_CHEST_OPEN);
        handler.setCloseSound(Sound.BLOCK_CHEST_CLOSE);
        handler.setClickSound(Sound.UI_BUTTON_CLICK);
        handler.setPageSound(Sound.ITEM_BOOK_PAGE_TURN);
        return handler;
    }

    public static SoundHandler epicSounds() {
        SoundHandler handler = new SoundHandler();
        handler.setOpenSound(Sound.ENTITY_ENDER_DRAGON_GROWL);
        handler.setCloseSound(Sound.ENTITY_ENDERMAN_TELEPORT);
        handler.setClickSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST);
        handler.setPageSound(Sound.ENTITY_BLAZE_SHOOT);
        return handler;
    }

    public static SoundHandler quietSounds() {
        SoundHandler handler = new SoundHandler();
        handler.setOpenSound(Sound.BLOCK_LEVER_CLICK);
        handler.setCloseSound(Sound.BLOCK_LEVER_CLICK);
        handler.setClickSound(Sound.UI_BUTTON_CLICK);
        handler.setPageSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        handler.setVolume(0.3f);
        return handler;
    }

    public static SoundHandler magicSounds() {
        SoundHandler handler = new SoundHandler();
        handler.setOpenSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE);
        handler.setCloseSound(Sound.ENTITY_ILLUSIONER_MIRROR_MOVE);
        handler.setClickSound(Sound.ENTITY_EVOKER_CAST_SPELL);
        handler.setPageSound(Sound.ENTITY_FIREWORK_ROCKET_TWINKLE);
        return handler;
    }

    public static SoundHandler shopSounds() {
        SoundHandler handler = new SoundHandler();
        handler.setOpenSound(Sound.ENTITY_VILLAGER_TRADE);
        handler.setCloseSound(Sound.ENTITY_VILLAGER_NO);
        handler.setClickSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        handler.setPageSound(Sound.ITEM_BOOK_PAGE_TURN);
        return handler;
    }

    public static SoundHandler battleSounds() {
        SoundHandler handler = new SoundHandler();
        handler.setOpenSound(Sound.ITEM_ARMOR_EQUIP_DIAMOND);
        handler.setCloseSound(Sound.ENTITY_PLAYER_HURT);
        handler.setClickSound(Sound.ENTITY_PLAYER_ATTACK_SWEEP);
        handler.setPageSound(Sound.ENTITY_ARROW_SHOOT);
        return handler;
    }

    public static SoundHandler settingsSounds() {
        SoundHandler handler = new SoundHandler();
        handler.setOpenSound(Sound.BLOCK_LEVER_CLICK);
        handler.setCloseSound(Sound.BLOCK_LEVER_CLICK);
        handler.setClickSound(Sound.UI_BUTTON_CLICK);
        handler.setPageSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        return handler;
    }

    public static SoundHandler silent() {
        return new SoundHandler();
    }
}

package me.egorov.plugin.library.inventory.animation;

import me.egorov.plugin.GamePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public interface CloseAnimation {

    void play(Player player, Inventory inventory);

    int getDuration();

    static CloseAnimation fadeIn() {
        return new CloseAnimation() {
            @Override
            public void play(Player player, Inventory inventory) {
                Inventory tempInv = Bukkit.createInventory(player, inventory.getSize(), Component.text("§8fadeIn"));
                ItemStack[] contents = inventory.getContents().clone();
                int rows = inventory.getSize() / 9;

                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.3f, 1.5f);

                new BukkitRunnable() {
                    int tick = 0;
                    final int totalTicks = rows + 2;

                    @Override
                    public void run() {
                        if (tick >= totalTicks) {
                            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.3f, 0.8f);
                            player.closeInventory();
                            cancel();
                            return;
                        }

                        if (tick % 2 == 0 && tick > 0) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, 1.5f - (tick * 0.05f));
                        }

                        tempInv.clear();

                        for (int row = 0; row < Math.min(tick, rows); row++) {
                            for (int col = 0; col < 9; col++) {
                                int slot = row * 9 + col;
                                ItemStack item = contents[slot];
                                if (item != null && item.getType() != Material.AIR) {
                                    tempInv.setItem(slot, item.clone());
                                }
                            }
                        }

                        player.openInventory(tempInv);
                        tick++;
                    }
                }.runTaskTimer(GamePlugin.instance(), 0, 2);
            }

            @Override
            public int getDuration() {
                return 20;
            }
        };
    }

    static CloseAnimation toCenter() {
        return new CloseAnimation() {
            @Override
            public void play(Player player, Inventory inventory) {
                Inventory tempInv = Bukkit.createInventory(player, inventory.getSize(), Component.text("§8toCenter"));
                ItemStack[] contents = inventory.getContents().clone();
                int rows = inventory.getSize() / 9;
                int centerRow = rows / 2;
                int centerCol = 4;
                int centerSlot = centerRow * 9 + centerCol;

                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 0.8f);

                new BukkitRunnable() {
                    int tick = 0;
                    final int totalTicks = 10;

                    @Override
                    public void run() {
                        if (tick >= totalTicks) {
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.2f);
                            player.closeInventory();
                            cancel();
                            return;
                        }

                        if (tick == totalTicks / 2) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.3f, 0.5f);
                        }

                        tempInv.clear();

                        for (int row = 0; row < rows; row++) {
                            for (int col = 0; col < 9; col++) {
                                int slot = row * 9 + col;
                                ItemStack item = contents[slot];
                                if (item == null || item.getType() == Material.AIR) continue;

                                int distance = Math.abs(row - centerRow) + Math.abs(col - centerCol);
                                if (distance <= tick) {
                                    tempInv.setItem(slot, item.clone());
                                }
                            }
                        }

                        ItemStack centerItem = contents[centerSlot];
                        if (centerItem != null && centerItem.getType() != Material.AIR) {
                            tempInv.setItem(centerSlot, centerItem.clone());
                        }

                        player.openInventory(tempInv);
                        tick++;
                    }
                }.runTaskTimer(GamePlugin.instance(), 0, 2);
            }

            @Override
            public int getDuration() {
                return 20;
            }
        };
    }

    static CloseAnimation waveLeftToRight() {
        return new CloseAnimation() {
            @Override
            public void play(Player player, Inventory inventory) {
                Inventory tempInv = Bukkit.createInventory(player, inventory.getSize(), Component.text("§8waveLeftToRight"));
                ItemStack[] contents = inventory.getContents().clone();

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.3f, 0.8f);

                new BukkitRunnable() {
                    int tick = 0;
                    final int totalTicks = 12;

                    @Override
                    public void run() {
                        if (tick >= totalTicks) {
                            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.3f, 0.8f);
                            player.closeInventory();
                            cancel();
                            return;
                        }

                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, 0.8f - (tick * 0.03f));

                        tempInv.clear();

                        for (int slot = 0; slot < contents.length; slot++) {
                            ItemStack item = contents[slot];
                            if (item == null || item.getType() == Material.AIR) continue;

                            int col = slot % 9;
                            if (col <= tick) {
                                tempInv.setItem(slot, item.clone());
                            }
                        }

                        player.openInventory(tempInv);
                        tick++;
                    }
                }.runTaskTimer(GamePlugin.instance(), 0, 2);
            }

            @Override
            public int getDuration() {
                return 24;
            }
        };
    }

    static CloseAnimation waveRightToLeft() {
        return new CloseAnimation() {
            @Override
            public void play(Player player, Inventory inventory) {
                Inventory tempInv = Bukkit.createInventory(player, inventory.getSize(), Component.text("§8waveRightToLeft"));
                ItemStack[] contents = inventory.getContents().clone();

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.3f, 1.2f);

                new BukkitRunnable() {
                    int tick = 0;
                    final int totalTicks = 12;

                    @Override
                    public void run() {
                        if (tick >= totalTicks) {
                            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.3f, 0.8f);
                            player.closeInventory();
                            cancel();
                            return;
                        }

                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, 1.2f - (tick * 0.03f));

                        tempInv.clear();

                        for (int slot = 0; slot < contents.length; slot++) {
                            ItemStack item = contents[slot];
                            if (item == null || item.getType() == Material.AIR) continue;

                            int col = slot % 9;
                            if (col >= 8 - tick) {
                                tempInv.setItem(slot, item.clone());
                            }
                        }

                        player.openInventory(tempInv);
                        tick++;
                    }
                }.runTaskTimer(GamePlugin.instance(), 0, 2);
            }

            @Override
            public int getDuration() {
                return 24;
            }
        };
    }

    static CloseAnimation waveTopToBottom() {
        return new CloseAnimation() {
            @Override
            public void play(Player player, Inventory inventory) {
                Inventory tempInv = Bukkit.createInventory(player, inventory.getSize(), Component.text("§8waveTopToBottom"));
                ItemStack[] contents = inventory.getContents().clone();
                int rows = inventory.getSize() / 9;

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.3f, 0.8f);

                new BukkitRunnable() {
                    int tick = 0;
                    final int totalTicks = rows + 2;

                    @Override
                    public void run() {
                        if (tick >= totalTicks) {
                            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.3f, 0.8f);
                            player.closeInventory();
                            cancel();
                            return;
                        }

                        if (tick % 2 == 0) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, 0.8f - (tick * 0.03f));
                        }

                        tempInv.clear();

                        for (int slot = 0; slot < contents.length; slot++) {
                            ItemStack item = contents[slot];
                            if (item == null || item.getType() == Material.AIR) continue;

                            int row = slot / 9;
                            if (row <= tick) {
                                tempInv.setItem(slot, item.clone());
                            }
                        }

                        player.openInventory(tempInv);
                        tick++;
                    }
                }.runTaskTimer(GamePlugin.instance(), 0, 2);
            }

            @Override
            public int getDuration() {
                return 24;
            }
        };
    }

    static CloseAnimation waveBottomToTop() {
        return new CloseAnimation() {
            @Override
            public void play(Player player, Inventory inventory) {
                Inventory tempInv = Bukkit.createInventory(player, inventory.getSize(), Component.text("§8waveBottomToTop"));
                ItemStack[] contents = inventory.getContents().clone();
                int rows = inventory.getSize() / 9;

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.3f, 1.2f);

                new BukkitRunnable() {
                    int tick = 0;
                    final int totalTicks = rows + 2;

                    @Override
                    public void run() {
                        if (tick >= totalTicks) {
                            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.3f, 0.8f);
                            player.closeInventory();
                            cancel();
                            return;
                        }

                        if (tick % 2 == 0) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, 1.2f - (tick * 0.03f));
                        }

                        tempInv.clear();

                        for (int slot = 0; slot < contents.length; slot++) {
                            ItemStack item = contents[slot];
                            if (item == null || item.getType() == Material.AIR) continue;

                            int row = slot / 9;
                            if (row >= rows - 1 - tick) {
                                tempInv.setItem(slot, item.clone());
                            }
                        }

                        player.openInventory(tempInv);
                        tick++;
                    }
                }.runTaskTimer(GamePlugin.instance(), 0, 2);
            }

            @Override
            public int getDuration() {
                return 24;
            }
        };
    }

    static CloseAnimation waveDiagonal() {
        return new CloseAnimation() {
            @Override
            public void play(Player player, Inventory inventory) {
                Inventory tempInv = Bukkit.createInventory(player, inventory.getSize(), Component.text("§8waveDiagonal"));
                ItemStack[] contents = inventory.getContents().clone();
                int rows = inventory.getSize() / 9;

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.3f, 0.8f);

                new BukkitRunnable() {
                    int tick = 0;
                    final int totalTicks = rows + 9;

                    @Override
                    public void run() {
                        if (tick >= totalTicks) {
                            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.3f, 0.8f);
                            player.closeInventory();
                            cancel();
                            return;
                        }

                        if (tick % 3 == 0) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, 0.8f - (tick * 0.02f));
                        }

                        tempInv.clear();

                        for (int slot = 0; slot < contents.length; slot++) {
                            ItemStack item = contents[slot];
                            if (item == null || item.getType() == Material.AIR) continue;

                            int row = slot / 9;
                            int col = slot % 9;

                            if (row + col <= tick) {
                                tempInv.setItem(slot, item.clone());
                            }
                        }

                        player.openInventory(tempInv);
                        tick++;
                    }
                }.runTaskTimer(GamePlugin.instance(), 0, 1);
            }

            @Override
            public int getDuration() {
                return 24;
            }
        };
    }

    static CloseAnimation waveReverseDiagonal() {
        return new CloseAnimation() {
            @Override
            public void play(Player player, Inventory inventory) {
                Inventory tempInv = Bukkit.createInventory(player, inventory.getSize(), Component.text("§8waveReverseDiagonal"));
                ItemStack[] contents = inventory.getContents().clone();
                int rows = inventory.getSize() / 9;

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.3f, 1.2f);

                new BukkitRunnable() {
                    int tick = 0;
                    final int totalTicks = rows + 9;

                    @Override
                    public void run() {
                        if (tick >= totalTicks) {
                            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.3f, 0.8f);
                            player.closeInventory();
                            cancel();
                            return;
                        }

                        if (tick % 3 == 0) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, 1.2f - (tick * 0.02f));
                        }

                        tempInv.clear();

                        for (int slot = 0; slot < contents.length; slot++) {
                            ItemStack item = contents[slot];
                            if (item == null || item.getType() == Material.AIR) continue;

                            int row = slot / 9;
                            int col = slot % 9;

                            if (row - col <= tick && col - row <= tick) {
                                tempInv.setItem(slot, item.clone());
                            }
                        }

                        player.openInventory(tempInv);
                        tick++;
                    }
                }.runTaskTimer(GamePlugin.instance(), 0, 1);
            }

            @Override
            public int getDuration() {
                return 24;
            }
        };
    }

    static CloseAnimation snake() {
        return new CloseAnimation() {
            @Override
            public void play(Player player, Inventory inventory) {
                Inventory tempInv = Bukkit.createInventory(player, inventory.getSize(), Component.text("§8🐍"));
                ItemStack[] contents = inventory.getContents().clone();
                int rows = inventory.getSize() / 9;
                int cols = 9;

                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.3f, 0.8f);

                new BukkitRunnable() {
                    int tick = 0;
                    final int totalTicks = rows * cols;

                    int[][] snakePath = new int[rows * cols][2];

                    {
                        int index = 0;
                        for (int row = 0; row < rows; row++) {
                            if (row % 2 == 0) {
                                for (int col = 0; col < cols; col++) {
                                    snakePath[index][0] = row;
                                    snakePath[index][1] = col;
                                    index++;
                                }
                            } else {
                                for (int col = cols - 1; col >= 0; col--) {
                                    snakePath[index][0] = row;
                                    snakePath[index][1] = col;
                                    index++;
                                }
                            }
                        }
                    }

                    @Override
                    public void run() {
                        if (tick >= totalTicks) {
                            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.3f, 0.8f);
                            player.closeInventory();
                            cancel();
                            return;
                        }

                        if (tick % 2 == 0) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, 0.8f - (tick * 0.01f));
                        }

                        for (int slot = 0; slot < contents.length; slot++) {
                            if (contents[slot] != null && contents[slot].getType() != Material.AIR) {
                                tempInv.setItem(slot, contents[slot].clone());
                            }
                        }

                        for (int i = 0; i <= tick; i++) {
                            int[] pos = snakePath[totalTicks - 1 - i];
                            tempInv.clear(pos[0] * 9 + pos[1]);
                        }

                        player.openInventory(tempInv);
                        tick++;
                    }
                }.runTaskTimer(GamePlugin.instance(), 0, 1);
            }

            @Override
            public int getDuration() {
                return 40;
            }
        };
    }

    static CloseAnimation simple() {
        return new CloseAnimation() {
            @Override
            public void play(Player player, Inventory inventory) {
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.5f, 1.0f);
                player.closeInventory();
            }

            @Override
            public int getDuration() {
                return 0;
            }
        };
    }

    static CloseAnimation none() {
        return new CloseAnimation() {
            @Override
            public void play(Player player, Inventory inventory) {
                player.closeInventory();
            }

            @Override
            public int getDuration() {
                return 0;
            }
        };
    }

    static CloseAnimation random() {
        CloseAnimation[] animations = {
                fadeIn(),
                toCenter(),
                waveLeftToRight(),
                waveRightToLeft(),
                waveTopToBottom(),
                waveBottomToTop(),
                waveDiagonal(),
                waveReverseDiagonal(),
                snake()
        };

        return animations[ThreadLocalRandom.current().nextInt(animations.length)];
    }
}
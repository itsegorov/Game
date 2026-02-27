package me.egorov.plugin.utility;

import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BukkitHelper {

    private BukkitHelper() {throw new UnsupportedOperationException();}

    public static @NotNull ServerLevel asMinecraft(@NotNull World world) {
        return ((CraftWorld) world).getHandle();
    }

    public static @NotNull BlockPos asMinecraft(@NotNull Location location) {
        return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static BlockData blockData(int blockStateId) {
        BlockState state = Block.stateById(blockStateId);
        return CraftBlockData.fromData(state);
    }

    public static int blockState(@NotNull BlockData blockData) {
        BlockState state = ((CraftBlockData) blockData).getState();
        return Block.getId(state);
    }

    @SuppressWarnings("resource")
    public static @NotNull Location minecraftEntityLocation(@NotNull Entity entity) {
        String worldName = entity.level().getWorld().getName();
        World world = Bukkit.getWorld(worldName);

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        float yaw = entity.getYRot();
        float pitch = entity.getXRot();

        return new Location(world, x, y, z, yaw, pitch);
    }

    public static void checkPlayer(@NotNull Player player) {
        checkPlayer(player, false);
    }

    public static void checkPlayer(@NotNull Player player, boolean offlineCheck) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!player.isOnline() && offlineCheck) {
            throw new IllegalStateException("Игрок не в сети");
        }
    }

    public static void checkTextComponent(@NotNull Component textComponent) {
        Objects.requireNonNull(textComponent, "Компонент не может быть null");
    }

    public static void checkInventorySize(int size) {
        if (size < 9 || size > 54) {
            throw new IllegalStateException("Размер инвентаря типа Chest не может быть меньше 9 и больше 54");
        }
    }

}

package me.egorov.plugin.utility;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class WorldUtility {

    public static long chunkKey(@NotNull Location location) {
        return location.getChunk().getChunkKey();
    }

    public static long chunkKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    public static int[] chunkCoords(long key) {
        int x = (int) (key >> 32);
        int z = (int) key;
        return new int[]{x, z};
    }

    public static boolean isChunkLoaded(@NotNull World world, int chunkX, int chunkZ) {
        return world.isChunkLoaded(chunkX, chunkZ);
    }

    public static void loadChunk(@NotNull World world, int chunkX, int chunkZ) {
        world.loadChunk(chunkX, chunkZ);
    }

    public static boolean isSimilar(@NotNull Location from, @NotNull Location to) {
        return from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ();
    }

    public static boolean isSameWorld(@NotNull Location from, @NotNull Location to) {
        return from.getWorld() != null &&
                to.getWorld() != null &&
                from.getWorld().equals(to.getWorld());
    }

    public static boolean isSimilar(@NotNull Location from, @NotNull Location to, double tolerance) {
        return Math.abs(from.getX() - to.getX()) <= tolerance &&
                Math.abs(from.getY() - to.getY()) <= tolerance &&
                Math.abs(from.getZ() - to.getZ()) <= tolerance;
    }

    public static boolean isSimilar2D(@NotNull Location from, @NotNull Location to) {
        return from.getX() == to.getX() && from.getZ() == to.getZ();
    }

    public static Location getBlockCenter(@NotNull Location location) {
        return location.clone().add(0.5, 0.5, 0.5);
    }

    public static Location getBlockCenter(@NotNull World world, int x, int y, int z) {
        return new Location(world, x + 0.5, y + 0.5, z + 0.5);
    }

    public static Location toBlockLocation(@NotNull Location location) {
        return new Location(location.getWorld(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static Block getBlockBelow(@NotNull Location location) {
        return location.getBlock().getRelative(BlockFace.DOWN);
    }

    public static Block getBlockAbove(@NotNull Location location) {
        return location.getBlock().getRelative(BlockFace.UP);
    }

    public static double distance2D(@NotNull Location from, @NotNull Location to) {
        double dx = from.getX() - to.getX();
        double dz = from.getZ() - to.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static double distanceSquared2D(@NotNull Location from, @NotNull Location to) {
        double dx = from.getX() - to.getX();
        double dz = from.getZ() - to.getZ();
        return dx * dx + dz * dz;
    }

    public static Vector getDirection(@NotNull Location from, @NotNull Location to) {
        return to.toVector().subtract(from.toVector()).normalize();
    }

    public static double getAngle(@NotNull Vector v1, @NotNull Vector v2) {
        return Math.acos(v1.dot(v2) / (v1.length() * v2.length()));
    }

    public static boolean teleportSafe(@NotNull Player player, @NotNull Location location) {
        Location safeLoc = findSafeLocation(location);
        if (safeLoc != null) {
            return player.teleport(safeLoc);
        }
        return false;
    }

    @Nullable
    public static Location findSafeLocation(@NotNull Location location) {
        World world = location.getWorld();
        if (world == null) return null;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        for (int checkY = y; checkY > world.getMinHeight(); checkY--) {
            Block block = world.getBlockAt(x, checkY, z);
            Block above = world.getBlockAt(x, checkY + 1, z);
            Block below = world.getBlockAt(x, checkY - 1, z);

            if (isSafe(block, above, below)) {
                return new Location(world, x + 0.5, checkY + 1, z + 0.5);
            }
        }
        return null;
    }

    private static boolean isSafe(Block block, Block above, Block below) {
        return !block.getType().isSolid() &&
                !above.getType().isSolid() &&
                below.getType().isSolid();
    }

    @Nullable
    public static Player getNearestPlayer(@NotNull Location location, double radius) {
        return location.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distance(location) <= radius)
                .min(Comparator.comparingDouble(p -> p.getLocation().distance(location)))
                .orElse(null);
    }

    @NotNull
    public static List<Player> getPlayersInRadius(@NotNull Location location, double radius) {
        return location.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distance(location) <= radius)
                .collect(Collectors.toList());
    }

    @NotNull
    public static Location getRandomLocation(@NotNull Location center, double radius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = random.nextDouble() * radius;

        double dx = Math.cos(angle) * distance;
        double dz = Math.sin(angle) * distance;

        return center.clone().add(dx, 0, dz);
    }

    public static boolean isSafeLocation(@NotNull Location location) {
        Block block = location.getBlock();
        Block below = block.getRelative(BlockFace.DOWN);

        return !isDangerousBlock(block) &&
                !isDangerousBlock(below) &&
                below.getType().isSolid();
    }

    private static boolean isDangerousBlock(Block block) {
        Material type = block.getType();
        return type == Material.LAVA ||
                type == Material.FIRE ||
                type == Material.WATER ||
                type == Material.CACTUS ||
                type == Material.MAGMA_BLOCK;
    }

    @NotNull
    public static List<World> getLoadedWorlds() {
        return Bukkit.getWorlds();
    }

    @Nullable
    public static World getWorld(@NotNull String name) {
        return Bukkit.getWorld(name);
    }

    @NotNull
    public static String locationToShortString(@NotNull Location location) {
        return String.format("%d;%d;%d",
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    @NotNull
    public static String locationToString(@NotNull Location location) {
        return String.format("%s;%f;%f;%f;%f;%f",
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

    @Nullable
    public static Location stringToLocation(@NotNull String string) {
        String[] parts = string.split(";");
        if (parts.length >= 6) {
            World world = Bukkit.getWorld(parts[0]);
            if (world != null) {
                return new Location(
                        world,
                        Double.parseDouble(parts[1]),
                        Double.parseDouble(parts[2]),
                        Double.parseDouble(parts[3]),
                        Float.parseFloat(parts[4]),
                        Float.parseFloat(parts[5])
                );
            }
        }
        return null;
    }

    @NotNull
    public static Vector toVector(@NotNull Location location) {
        return location.toVector();
    }

    @NotNull
    public static Location toLocation(@NotNull World world, @NotNull Vector vector) {
        return new Location(world, vector.getX(), vector.getY(), vector.getZ());
    }

    @NotNull
    public static Vector getLookDirection(@NotNull Player player) {
        return player.getLocation().getDirection();
    }

    public static boolean isDay(@NotNull World world) {
        long time = world.getTime();
        return time >= 0 && time < 12300;
    }

    public static boolean isNight(@NotNull World world) {
        long time = world.getTime();
        return time >= 13000 && time < 23000;
    }

    public static boolean hasSkyAccess(@NotNull Location location) {
        World world = location.getWorld();
        if (world == null) return false;

        int x = location.getBlockX();
        int z = location.getBlockZ();

        for (int y = location.getBlockY() + 1; y < world.getMaxHeight(); y++) {
            if (world.getBlockAt(x, y, z).getType().isSolid()) {
                return false;
            }
        }
        return true;
    }

    public static int getHighestBlockYAt(@NotNull World world, int x, int z) {
        return world.getHighestBlockYAt(x, z);
    }

    @NotNull
    public static Location getRandomSurfaceLocation(@NotNull World world, int radius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int x = random.nextInt(-radius, radius);
        int z = random.nextInt(-radius, radius);
        int y = world.getHighestBlockYAt(x, z);

        return new Location(world, x + 0.5, y + 1, z + 0.5);
    }

}

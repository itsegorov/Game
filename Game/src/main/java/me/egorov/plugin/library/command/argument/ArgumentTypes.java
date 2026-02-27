package me.egorov.plugin.library.command.argument;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.LiteralMessage;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public final class ArgumentTypes {

    private ArgumentTypes() {}

    public static CustomArgumentType<Player, String> onlinePlayer() {
        return new PlayerArgumentType();
    }

    public static CustomArgumentType<World, String> world() {
        return new WorldArgumentType();
    }

    public static ArgumentType<Location> location() {
        return new LocationArgumentType();
    }

    public static ArgumentType<Location> relativeLocation() {
        return new RelativeLocationArgumentType();
    }

    private static class PlayerArgumentType implements CustomArgumentType<Player, String> {

        @Override
        public Player parse(StringReader reader) throws CommandSyntaxException {
            int start = reader.getCursor();
            String playerName = reader.readString();

            Player player = Bukkit.getPlayerExact(playerName);
            if (player == null) {
                reader.setCursor(start);
                String errorMessage = "Игрок '" + playerName + "' не найден!";
                throw new SimpleCommandExceptionType(
                        new LiteralMessage(errorMessage)
                ).create();
            }

            return player;
        }

        @Override
        public ArgumentType<String> getNativeType() {
            return StringArgumentType.word();
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return StringArgumentType.word().listSuggestions(context, builder);
        }

        @Override
        public Collection<String> getExamples() {
            return Arrays.asList("Egorov", "owdmozorka", "Notch");
        }
    }

    private static class WorldArgumentType implements CustomArgumentType<World, String> {

        @Override
        public World parse(StringReader reader) throws CommandSyntaxException {
            String worldName = reader.readString();
            World world = Bukkit.getWorld(worldName);

            if (world == null) {
                throw new SimpleCommandExceptionType(
                        (Message) Component.text("Мир не найден: " + worldName)
                ).create();
            }

            return world;
        }

        @Override
        public ArgumentType<String> getNativeType() {
            return StringArgumentType.word();
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .filter(name -> name.toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
                    .forEach(builder::suggest);
            return builder.buildFuture();
        }

        @Override
        public Collection<String> getExamples() {
            return Arrays.asList("world", "world_nether", "world_the_end");
        }
    }

    private static class LocationArgumentType implements ArgumentType<Location> {
        @Override
        public Location parse(StringReader reader) throws CommandSyntaxException {
            double x = reader.readDouble();
            reader.skipWhitespace();
            double y = reader.readDouble();
            reader.skipWhitespace();
            double z = reader.readDouble();

            return new Location(null, x, y, z);
        }

        @Override
        public Collection<String> getExamples() {
            return Arrays.asList("0 64 0", "100 50 100");
        }
    }

    private static class RelativeLocationArgumentType implements ArgumentType<Location> {
        @Override
        public Location parse(StringReader reader) throws CommandSyntaxException {
            double x = parseCoordinate(reader, 0);
            reader.skipWhitespace();
            double y = parseCoordinate(reader, 0);
            reader.skipWhitespace();
            double z = parseCoordinate(reader, 0);

            return new Location(null, x, y, z);
        }

        private double parseCoordinate(StringReader reader, double relativeTo) throws CommandSyntaxException {
            if (reader.peek() == '~') {
                reader.skip();
                if (reader.canRead() && (Character.isDigit(reader.peek()) || reader.peek() == '-')) {
                    return relativeTo + reader.readDouble();
                }
                return relativeTo;
            }
            return reader.readDouble();
        }

        @Override
        public Collection<String> getExamples() {
            return Arrays.asList("~ ~ ~", "~10 ~ ~-5", "0 64 0");
        }
    }
}
    package me.egorov.plugin.utility;

    import net.kyori.adventure.text.Component;
    import net.kyori.adventure.text.format.TextDecoration;
    import net.kyori.adventure.text.minimessage.MiniMessage;
    import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
    import org.bukkit.Color;
    import org.bukkit.entity.Player;
    import org.jetbrains.annotations.NotNull;

    import java.time.Duration;
    import java.util.*;
    import java.util.regex.Matcher;
    import java.util.regex.Pattern;

    public class StringUtility {

        public static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
        private static final Pattern LEGACY_HEX_TO_MINIMESSAGE = Pattern.compile("§x((§[a-zA-Z0-9]){6})(.*)");

        private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();

        private static final char dummyChar = Character.MAX_VALUE;

        public static @NotNull Component deserialize(@NotNull String input) {
            return MINI_MESSAGE.deserialize(input).decoration(TextDecoration.ITALIC, false);
        }

        public static @NotNull String asRoman(int level) {
            switch (level) {
                case 0 -> { return ""; }
                case 1 -> { return "I"; }
                case 2 -> { return "II"; }
                case 3 -> { return "III"; }
                case 4 -> { return "IV"; }
                case 5 -> { return "V"; }
                case 6 -> { return "VI"; }
                case 7 -> { return "VII"; }
                case 8 -> { return "VIII"; }
                case 9 -> { return "IX"; }
                case 10 -> { return "X"; }
                default -> { return String.valueOf(level); }
            }
        }

        public static @NotNull String reformatMiniMessage(@NotNull String text) {
            if (!text.contains("<")) {
                return text;
            }

            try {
                String serialized = SERIALIZER.serialize(deserialize(text + dummyChar));
                return serialized.substring(0, serialized.length() - 1);
            } catch (Throwable ignored) {
                return text;
            }
        }

        public static @NotNull String legacyHexToMiniMessage(@NotNull String input, boolean displayColor) {
            Matcher matcher = LEGACY_HEX_TO_MINIMESSAGE.matcher(input);

            if (!matcher.find()) {
                return input;
            }

            String color = "<color:#" + matcher.group(1).replaceAll("§", "") + ">";

            return (displayColor ? color : "") + matcher.group(3);
        }

        private static boolean isLetter(char c) {
            return (c >= 'a' && c <= 'z') ||
                    (c >= 'A' && c <= 'Z');
        }

        public static @NotNull Color hexToBukkitColor(@NotNull String input) {
            return Color.fromRGB(
                    Integer.valueOf(input.substring(0, 2), 16),
                    Integer.valueOf(input.substring(2, 4), 16),
                    Integer.valueOf(input.substring(4, 6), 16)
            );
        }

        /**
         * <a href="https://www.cyberforum.ru/post8175336.html">Original code</a>
         * choosePluralMerge(number, "секунда", "секунды", "секунд")
         * */
        public static @NotNull String choosePluralMerge(long number, @NotNull String caseOne, @NotNull String caseTwo,
                                                        @NotNull String caseFive) {
            String str = "";
            number = Math.abs(number);

            if (number % 10 == 1 && number % 100 != 11) {
                str += caseOne;
            } else if (number % 10 >= 2 && number % 10 <= 4 && (number % 100 < 10 || number % 100 >= 20)) {
                str += caseTwo;
            } else {
                str += caseFive;
            }

            return str;
        }

        public static void checkString(@NotNull String input) {
            checkString(input, true);
        }

        public static void checkString(@NotNull String input, boolean emptyCheck) {
            Objects.requireNonNull(input, "Input string cannot be null");

            if (input.isEmpty() && emptyCheck) {
                throw new IllegalStateException("Input string cannot be empty");
            }
        }

        public static @NotNull Collection<Component> parseStrings(@NotNull Collection<String> stringCollection) {
            List<Component> componentCollection = new ArrayList<>();

            for (String line : stringCollection) {
                Component parsedComponent = parseString(line);
                parsedComponent = parsedComponent.decoration(TextDecoration.ITALIC, false);

                componentCollection.add(parsedComponent);
            }

            return componentCollection;
        }

        public static @NotNull Component parseString(@NotNull String input) {
            StringUtility.checkString(input, false);
            Component component = deserialize(input);
            component = component.decoration(TextDecoration.ITALIC, false);

            return component;
        }

        public static Component parseString(@NotNull Player player, @NotNull String input) {
            BukkitHelper.checkPlayer(player);
            StringUtility.checkString(input, false);

            Component component = deserialize(input);
            component = component.decoration(TextDecoration.ITALIC, false);

            return component;
        }

        // Minecraft 1.16.5 source code
        public static String ticksToElapsedTime(int par0) {
            int var1 = par0 / 20;
            int var2 = var1 / 60;
            var1 %= 60;

            return var1 < 10 ? var2 + ":0" + var1 : var2 + ":" + var1;
        }

        public static boolean brackets(String input) throws IllegalAccessException {
            if (input == null) throw new IllegalAccessException("Input cannot be null");

            Stack<Character> stack = new Stack<Character>();

            for (int i = 0; i < input.length(); i++) {
                char symbol = input.charAt(i);
                if (symbol == '(' || symbol == '{' || symbol == '[')
                    stack.push(symbol);
                else if (symbol == ']') {
                    if (stack.empty() || stack.pop() != '[')
                        return false;
                } else if (symbol == '}') {
                    if (stack.empty() || stack.pop() != '{')
                        return false;
                } else if (symbol == ')') {
                    if (stack.empty() || stack.pop() != '(')
                        return false;
                }
            }
            return stack.empty();
        }

        public static String formatTime(Duration duration) {
            StringBuilder builder = new StringBuilder();

            long seconds = duration.getSeconds();
            long minutes = seconds / 60L;
            long hours = minutes / 60L;
            long days = hours / 24L;

            seconds %= 60L;
            minutes %= 60L;
            hours %= 24L;
            days %= 7L;

            if (seconds >= 0L && hours <= 0 && days <= 0) {
                builder.insert(0, seconds + "с");
            }

            if (minutes > 0L) {
                if (!builder.isEmpty()) {
                    builder.insert(0, ' ');
                }

                builder.insert(0, minutes + "м");
            }

            if (hours > 0L) {
                if (!builder.isEmpty()) {
                    builder.insert(0, ' ');
                }

                builder.insert(0, hours + "ч");
            }

            if (days > 0L) {
                if (!builder.isEmpty()) {
                    builder.insert(0, ' ');
                }

                builder.insert(0, days + "д");
            }

            return builder.toString();
        }
    }

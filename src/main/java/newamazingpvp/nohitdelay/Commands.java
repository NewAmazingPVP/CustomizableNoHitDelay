package newamazingpvp.nohitdelay;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Commands implements CommandExecutor, TabCompleter {
    private final NoHitDelay plugin;
    private final Config config;
    private static final Pattern HEX_REGEX = Pattern.compile("&#([0-9A-F])([0-9A-F])([0-9A-F])([0-9A-F])([0-9A-F])([0-9A-F])", Pattern.CASE_INSENSITIVE);

    public Commands(NoHitDelay plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String prefix = colorize(config.getConfig().getString("messages.prefix", "&f[NoHitDelay] "));
            boolean usePrefix = config.getConfig().getBoolean("messages.use-prefix", true);

            if (command.getName().equalsIgnoreCase("nohitdelay")) {
                if (args.length == 0) {
                    sendCommandList(player);
                    return true;
                }
                switch (args[0].toLowerCase()) {
                    case "setdelay":
                        if (args.length == 2) {
                            try {
                                long delay = Long.parseLong(args[1]);
                                config.getConfig().set("delay", delay);
                                plugin.saveConfig();
                                player.sendMessage(formatMessage(usePrefix, prefix, config.getConfig().getString("messages.delay-set", "&aDelay set to: &e%value%&a."), delay));
                            } catch (NumberFormatException e) {
                                player.sendMessage(formatMessage(usePrefix, prefix, config.getConfig().getString("messages.invalid-delay", "&cInvalid delay value. Please enter a number.")));
                            }
                        } else {
                            player.sendMessage(formatMessage(usePrefix, prefix, config.getConfig().getString("messages.usage-setdelay", "&cUsage: /nohitdelay setdelay <delay>")));
                        }
                        break;
                    case "getdelay":
                        player.sendMessage(formatMessage(usePrefix, prefix, config.getConfig().getString("messages.current-delay", "&aDelay is currently set to: &e%value%"), config.getConfig().getLong("delay")));
                        break;
                    case "setmode":
                        if (args.length == 2) {
                            String mode = args[1].toLowerCase();
                            if (Arrays.asList("pvp", "evp", "pvp-evp", "any", "player-only").contains(mode)) {
                                config.getConfig().set("mode", mode);
                                plugin.saveConfig();
                                player.sendMessage(formatMessage(usePrefix, prefix, config.getConfig().getString("messages.mode-set", "&aMode set to: &e%value%&a."), mode));
                            } else {
                                player.sendMessage(formatMessage(usePrefix, prefix, config.getConfig().getString("messages.invalid-mode", "&cInvalid mode value. Please use 'pvp', 'evp', 'pvp-evp', 'any', or 'player-only'.")));
                            }
                        } else {
                            player.sendMessage(formatMessage(usePrefix, prefix, config.getConfig().getString("messages.usage-setmode", "&cUsage: /nohitdelay setmode <mode>")));
                        }
                        break;
                    case "getmode":
                        player.sendMessage(formatMessage(usePrefix, prefix, config.getConfig().getString("messages.current-mode", "&aMode is currently set to: &e%value%"), config.getConfig().getString("mode")));
                        break;
                    case "reloadconfig":
                        config.reloadConfig();
                        player.sendMessage(formatMessage(usePrefix, prefix, config.getConfig().getString("messages.config-reloaded", "&aConfiguration reloaded.")));
                        break;
                    default:
                        sendCommandList(player);
                        break;
                }
            }
        }
        return true;
    }

    private void sendCommandList(Player player) {
        List<String> commandList = config.getConfig().getStringList("messages.command-list");
        String prefix = colorize(config.getConfig().getString("messages.prefix", "&f[NoHitDelay] "));
        for (String raw : commandList) {
            String line = raw.replace("%prefix%", prefix);
            player.sendMessage(colorize(line));
        }
    }

    private String formatMessage(boolean usePrefix, String prefix, String message) {
        return formatMessage(usePrefix, prefix, message, null);
    }

    private String formatMessage(boolean usePrefix, String prefix, String message, Object value) {
        if (value != null) {
            message = message.replace("%value%", value.toString());
        }
        message = message.replace("%prefix%", prefix);
        String colored = colorize(message);
        return (usePrefix ? prefix : "") + colored;
    }

    public String replace(String s) {
        return HEX_REGEX.matcher(s).replaceAll("&x&$1&$2&$3&$4&$5&$6");
    }

    private String colorize(String s) {
        return ChatColor.translateAlternateColorCodes('&', replace(s));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("nohitdelay")) {
            if (args.length == 1) {
                return Arrays.asList("setdelay", "getdelay", "setmode", "getmode", "reloadconfig")
                        .stream()
                        .filter(subcommand -> subcommand.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 2 && args[0].equalsIgnoreCase("setmode")) {
                return Arrays.asList("pvp", "evp", "pvp-evp", "any", "player-only")
                        .stream()
                        .filter(mode -> mode.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return null;
    }
}

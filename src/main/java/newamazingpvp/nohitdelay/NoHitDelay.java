package newamazingpvp.nohitdelay;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class NoHitDelay extends JavaPlugin implements Listener, TabCompleter {
    public FileConfiguration config;
    private static final Pattern HEX_REGEX = Pattern.compile("&#([0-9A-F])([0-9A-F])([0-9A-F])([0-9A-F])([0-9A-F])([0-9A-F])", Pattern.CASE_INSENSITIVE);

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        config = getConfig();
        getCommand("nohitdelay").setExecutor(this);
        getCommand("nohitdelay").setTabCompleter(this);
    }


    @EventHandler
    private void onEntityDamage(EntityDamageByEntityEvent event) {
        long hitDelay = config.getLong("delay");
        String mode = config.getString("mode");

        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (mode != null) {
            switch (mode.toLowerCase()) {
                case "pvp":
                    if (damager instanceof Player && entity instanceof Player) {
                        resetNoDamageTicks((LivingEntity) entity, hitDelay);
                    }
                    break;
                case "evp":
                    if (!(damager instanceof Player) && entity instanceof Player) {
                        resetNoDamageTicks((LivingEntity) entity, hitDelay);
                    }
                    break;
                case "pvp-evp":
                    if ((damager instanceof Player && entity instanceof LivingEntity) || (entity instanceof Player && damager instanceof LivingEntity)) {
                        resetNoDamageTicks((LivingEntity) entity, hitDelay);
                    }
                    break;
                case "any":
                    if (entity instanceof LivingEntity) {
                        resetNoDamageTicks((LivingEntity) entity, hitDelay);
                    }
                    break;
                case "player-only":
                    if (damager instanceof Player && entity instanceof LivingEntity) {
                        resetNoDamageTicks((LivingEntity) entity, hitDelay);
                    }
                    break;
            }
        }
    }

    private void resetNoDamageTicks(LivingEntity entity, long hitDelay) {
        Bukkit.getScheduler().runTaskLater(this, () -> entity.setNoDamageTicks((int) hitDelay), 1);
    }

    public String replace(String s) {
        return HEX_REGEX.matcher(s).replaceAll("&x&$1&$2&$3&$4&$5&$6");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String prefix = config.getString("messages.prefix", "&f[NoHitDelay] ");
            prefix = replace(prefix);
            prefix = prefix.replace("&", "§");
            prefix = ChatColor.translateAlternateColorCodes('§', prefix);
            boolean usePrefix = config.getBoolean("messages.use-prefix", true);

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
                                config.set("delay", delay);
                                saveConfig();
                                player.sendMessage(formatMessage(usePrefix, prefix, config.getString("messages.delay-set", "&aDelay set to: &e%value%&a."), delay));
                            } catch (NumberFormatException e) {
                                player.sendMessage(formatMessage(usePrefix, prefix, config.getString("messages.invalid-delay", "&cInvalid delay value. Please enter a number.")));
                            }
                        } else {
                            player.sendMessage(formatMessage(usePrefix, prefix, config.getString("messages.usage-setdelay", "&cUsage: /nohitdelay setdelay <delay>")));
                        }
                        break;
                    case "getdelay":
                        player.sendMessage(formatMessage(usePrefix, prefix, config.getString("messages.current-delay", "&aDelay is currently set to: &e%value%"), config.getLong("delay")));
                        break;
                    case "setmode":
                        if (args.length == 2) {
                            String mode = args[1].toLowerCase();
                            if (Arrays.asList("pvp", "evp", "pvp-evp", "any", "player-only").contains(mode)) {
                                config.set("mode", mode);
                                saveConfig();
                                player.sendMessage(formatMessage(usePrefix, prefix, config.getString("messages.mode-set", "&aMode set to: &e%value%&a."), mode));
                            } else {
                                player.sendMessage(formatMessage(usePrefix, prefix, config.getString("messages.invalid-mode", "&cInvalid mode value. Please use 'pvp', 'evp', 'pvp-evp', 'any', or 'player-only'.")));
                            }
                        } else {
                            player.sendMessage(formatMessage(usePrefix, prefix, config.getString("messages.usage-setmode", "&cUsage: /nohitdelay setmode <mode>")));
                        }
                        break;
                    case "getmode":
                        player.sendMessage(formatMessage(usePrefix, prefix, config.getString("messages.current-mode", "&aMode is currently set to: &e%value%"), config.getString("mode")));
                        break;
                    case "reloadconfig":
                        reloadConfig();
                        config = getConfig();
                        player.sendMessage(formatMessage(usePrefix, prefix, config.getString("messages.config-reloaded", "&aConfiguration reloaded.")));
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
        List<String> commandList = config.getStringList("messages.command-list");
        for (String line : commandList) {
            line = line.replace("%prefix%", config.getString("messages.prefix", "&f[NoHitDelay] "));
            line = replace(line);
            line = line.replace("&", "§");
            line = ChatColor.translateAlternateColorCodes('§', line);
            player.sendMessage(line);
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
        message = replace(message);
        message = message.replace("&", "§");
        message = ChatColor.translateAlternateColorCodes('§', message);
        return (usePrefix ? prefix : "") + message;
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

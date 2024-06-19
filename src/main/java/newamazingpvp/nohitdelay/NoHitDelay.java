package newamazingpvp.nohitdelay;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class NoHitDelay extends JavaPlugin implements Listener, TabCompleter {
    public FileConfiguration config;
    private Logger log;
    private ConcurrentMap<Entity, Long> damageTimestamps;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        config = getConfig();
        getCommand("nohitdelay").setExecutor(this);
        getCommand("nohitdelay").setTabCompleter(this);
        damageTimestamps = new ConcurrentHashMap<>();
    }

    @EventHandler
    public void onMythicDamage(MythicDamageEvent event){
        long hitDelay = config.getLong("delay");
        boolean onlyMythicMobDamageHitDelay = config.getBoolean("only-Mythicmob-damage-hit-delay");
        if (onlyMythicMobDamageHitDelay) {
            resetNoDamageTicks((LivingEntity) event.getTarget().getBukkitEntity(), hitDelay);
        }
    }

    @EventHandler
    private void onEntityDamage(EntityDamageByEntityEvent event) {
        long hitDelay = config.getLong("delay");
        String mode = config.getString("mode");
        boolean onlyMythicMobDamageHitDelay = config.getBoolean("only-Mythicmob-damage-hit-delay");

        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (onlyMythicMobDamageHitDelay) {
            return;
        }

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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("nohitdelay")) {
                if (args.length == 0) {
                    player.sendMessage(ChatColor.RED + "Usage: /nohitdelay <setdelay|getdelay|setmode|getmode|reloadconfig> [value]");
                    return true;
                }
                switch (args[0].toLowerCase()) {
                    case "setdelay":
                        if (args.length == 2) {
                            try {
                                long delay = Long.parseLong(args[1]);
                                config.set("delay", delay);
                                saveConfig();
                                player.sendMessage(ChatColor.GREEN + "Delay set to: " + ChatColor.YELLOW + delay + ChatColor.RESET + ". Do make sure the delay is at least 2 because setting it below that will make some hits not register.");
                            } catch (NumberFormatException e) {
                                player.sendMessage(ChatColor.RED + "Invalid delay value. Please enter a number.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Usage: /nohitdelay setdelay <delay>");
                        }
                        break;
                    case "getdelay":
                        player.sendMessage(ChatColor.GREEN + "Delay is currently set to: " + ChatColor.YELLOW + config.getLong("delay"));
                        break;
                    case "setmode":
                        if (args.length == 2) {
                            String mode = args[1].toLowerCase();
                            if (mode.equals("pvp") || mode.equals("evp") || mode.equals("pvp-evp") || mode.equals("any") || mode.equals("player-only")) {
                                config.set("mode", mode);
                                saveConfig();
                                player.sendMessage(ChatColor.GREEN + "Mode set to: " + ChatColor.YELLOW + mode + ChatColor.RESET + ".");
                            } else {
                                player.sendMessage(ChatColor.RED + "Invalid mode value. Please use 'pvp', 'evp', 'pvp-evp', 'any', or 'player-only'.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Usage: /nohitdelay setmode <mode>");
                        }
                        break;
                    case "getmode":
                        player.sendMessage(ChatColor.GREEN + "Mode is currently set to: " + ChatColor.YELLOW + config.getString("mode"));
                        break;
                    case "reloadconfig":
                        reloadConfig();
                        config = getConfig();
                        player.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + "Usage: /nohitdelay <setdelay|getdelay|setmode|getmode|reloadconfig> [value]");
                        break;
                }
            }
        }
        return true;
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

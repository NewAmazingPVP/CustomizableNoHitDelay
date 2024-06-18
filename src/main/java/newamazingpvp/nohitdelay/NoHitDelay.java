package newamazingpvp.nohitdelay;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class NoHitDelay extends JavaPlugin implements Listener {
    public FileConfiguration config;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        config = getConfig();
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
        Bukkit.getScheduler().runTaskLater((Plugin) this, () -> entity.setNoDamageTicks((int) hitDelay), 1);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("setdelay")) {
                if (args.length == 1) {
                    try {
                        long delay = Long.parseLong(args[0]);
                        config.set("delay", delay);
                        saveConfig();
                        player.sendMessage(ChatColor.GREEN + "Delay set to: " + ChatColor.YELLOW + delay + ChatColor.RESET + ". Do make sure the delay is at least 2 because setting it below that will make some hits not register.");
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid delay value. Please enter a number.");
                    }
                }
            } else if (command.getName().equalsIgnoreCase("getdelay")) {
                player.sendMessage(ChatColor.GREEN + "Delay is currently set to: " + ChatColor.YELLOW + config.getLong("delay"));
            } else if (command.getName().equalsIgnoreCase("setmode")) {
                if (args.length == 1) {
                    String mode = args[0].toLowerCase();
                    if (mode.equals("pvp") || mode.equals("evp") || mode.equals("pvp-evp") || mode.equals("any") || mode.equals("player-only")) {
                        config.set("mode", mode);
                        saveConfig();
                        player.sendMessage(ChatColor.GREEN + "Mode set to: " + ChatColor.YELLOW + mode + ChatColor.RESET + ".");
                    } else {
                        player.sendMessage(ChatColor.RED + "Invalid mode value. Please use 'pvp', 'evp', 'pvp-evp', 'any', or 'player-only'.");
                    }
                }
            } else if (command.getName().equalsIgnoreCase("getmode")) {
                player.sendMessage(ChatColor.GREEN + "Mode is currently set to: " + ChatColor.YELLOW + config.getString("mode"));
            }
        }
        return true;
    }
}

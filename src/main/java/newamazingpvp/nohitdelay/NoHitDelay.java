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
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            Bukkit.getScheduler().runTaskLater((Plugin) this, () -> livingEntity.setNoDamageTicks(0), hitDelay);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 1 && command.getName().equalsIgnoreCase("setdelay")) {
                try {
                    long delay = Long.parseLong(args[0]);
                    config.set("delay", delay);
                    saveConfig();
                    player.sendMessage(ChatColor.GREEN + "Delay set to: " + ChatColor.YELLOW + delay + ChatColor.RESET + ". Do make sure the delay is at least 2 because setting it below that will make some hits not register.");
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid delay value. Please enter a number.");
                }
            } else if (args.length == 0 && command.getName().equalsIgnoreCase("getdelay")) {
                player.sendMessage(ChatColor.GREEN + "Delay is currently set to: " + ChatColor.YELLOW + config.getLong("delay"));
            }
        }
        return true;
    }
}

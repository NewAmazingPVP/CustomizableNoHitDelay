package newamazingpvp.nohitdelay;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class NoHitDelay extends JavaPlugin implements Listener {
    public long Delay = 2L;
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    private void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            Bukkit.getScheduler().runTaskLater((Plugin)this, () -> livingEntity.setNoDamageTicks(0), Delay);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 1) {
                try {
                    long delay = Long.parseLong(args[0]);
                    Delay = delay;
                    player.sendMessage("Delay set to: " + ChatColor.GREEN + delay + "Do make sure the delay is at least 2 because setting it below that will make some hits not register");
                } catch (NumberFormatException e) {
                    player.sendMessage("Invalid delay value. Please enter a number.");
                }
            } else if (args.length == 0 && command.getName().equalsIgnoreCase("getdelay")) {
                player.sendMessage("Delay is currently set to: " + ChatColor.GREEN + Delay);
            }
        }
        return true;
    }
}

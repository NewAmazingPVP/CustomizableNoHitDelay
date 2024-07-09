package newamazingpvp.nohitdelay;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class Events implements Listener {
    private final NoHitDelay plugin;
    private final Config config;

    public Events(NoHitDelay plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }

    @EventHandler
    private void onEntityDamage(EntityDamageByEntityEvent event) {
        long hitDelay = config.getConfig().getLong("delay");
        String mode = config.getConfig().getString("mode");

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
        Bukkit.getScheduler().runTaskLater(plugin, () -> entity.setNoDamageTicks((int) hitDelay), 1);
    }
}

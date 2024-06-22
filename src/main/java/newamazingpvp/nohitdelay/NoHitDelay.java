package newamazingpvp.nohitdelay;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.players.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class NoHitDelay extends JavaPlugin implements Listener, TabCompleter {
    public FileConfiguration config;
    private static final Pattern HEX_REGEX = Pattern.compile("&#([0-9A-F])([0-9A-F])([0-9A-F])([0-9A-F])([0-9A-F])([0-9A-F])", Pattern.CASE_INSENSITIVE);
    private final HashMap<UUID, Long> lastMythicAttackTime = new HashMap<>();
    private final HashMap<Entity, Long> entityCooldown = new HashMap<>();
    private final HashMap<Entity, Long> entitySkillCooldown = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        config = getConfig();
        getCommand("nohitdelay").setExecutor(this);
        getCommand("nohitdelay").setTabCompleter(this);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMythicDamage(MythicDamageEvent event) {
        long hitDelay = config.getLong("delay");
        boolean onlyMythicMobDamageHitDelay = config.getBoolean("only-Mythicmob-damage-hit-delay");
        LivingEntity target = (LivingEntity) event.getTarget().getBukkitEntity();
        UUID targetId = target.getUniqueId();

        // Register the time of the mythic attack
        lastMythicAttackTime.put(targetId, System.currentTimeMillis());
        if (onlyMythicMobDamageHitDelay) {
            event.getCaster().getEntity().getBukkitEntity().sendMessage("Applied 0 ticks of delay to entity with custom manual cooldown");
            resetNoDamageTicks(target, 0); // Ensure no delay for mythic attacks if setting is enabled
        }
    }

    @EventHandler
    public void onEntityDmg(EntityDamageEvent event) {
        // Set a cooldown for the entity upon taking damage
        if(config.getBoolean("only-Mythicmob-damage-hit-delay")) {
            if (!isEntityOnCooldown(event.getEntity())) {
                entityCooldown.put(event.getEntity(), System.currentTimeMillis() + 1000); // 1 second cooldown for normal attacks
            } else {
                event.setCancelled(true);
            }
        }
    }

    public boolean isEntityOnCooldown(Entity e) {
        return entityCooldown.getOrDefault(e, 0L) - System.currentTimeMillis() > 0;
    }

    public boolean isEntityOnSkillCooldown(Entity e) {
        return entitySkillCooldown.getOrDefault(e, 0L) - System.currentTimeMillis() > 0;
    }

    public boolean recentSkillAttack(Player p) {
        Long lastMythicAttack = lastMythicAttackTime.get(p.getUniqueId());
        return lastMythicAttack != null && (System.currentTimeMillis() - lastMythicAttack <= 49);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onEntityDamage(EntityDamageByEntityEvent event) {
        long hitDelay = config.getLong("delay");
        String mode = config.getString("mode");
        boolean onlyMythicMobDamageHitDelay = config.getBoolean("only-Mythicmob-damage-hit-delay");

        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (onlyMythicMobDamageHitDelay && damager instanceof Player) {
            Player player = (Player) damager;

            if (recentSkillAttack(player) && isEntityOnSkillCooldown(entity)) {
                player.sendMessage("Recent skill attack and entity is on skill cooldown");
                event.setCancelled(true); // Cancel the event if the entity is still on skill cooldown
            } else if (recentSkillAttack(player)) {
                player.sendMessage("recent skill attack");
                entitySkillCooldown.put(entity, System.currentTimeMillis() + hitDelay * 50); // Apply skill cooldown
            } else if (!recentSkillAttack(player) && isEntityOnCooldown(entity)) {
                player.sendMessage("no recent skill attack and entity is on normal attack cooldown");
                event.setCancelled(true); // Cancel the event if the entity is on normal cooldown
            }
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

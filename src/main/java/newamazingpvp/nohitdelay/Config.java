package newamazingpvp.nohitdelay;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {
    private final NoHitDelay plugin;
    private FileConfiguration config;

    public Config(NoHitDelay plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
}

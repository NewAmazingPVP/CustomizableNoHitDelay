package newamazingpvp.nohitdelay;

import org.bukkit.plugin.java.JavaPlugin;

public final class NoHitDelay extends JavaPlugin {

    @Override
    public void onEnable() {
        Config config = new Config(this);
        Commands commands = new Commands(this, config);
        Events events = new Events(this, config);

        getServer().getPluginManager().registerEvents(events, this);
        getCommand("nohitdelay").setExecutor(commands);
        getCommand("nohitdelay").setTabCompleter(commands);
    }
}

# CustomizableNoHitDelay
CustomizableNoHitDelay is a plugin for Spigot that improves PVP experience by allowing users to change entity hit invulnerability delay ticks

Spigot Link: https://www.spigotmc.org/resources/customizablenohitdelay.109763/

# Features
- Compatible with Minecraft versions 1.8 and higher for both Spigot and Bungeecord, including the latest versions and most likely future versions as well.

- /setdelay command allows you to change hit delay ticks from ingame (permission: nohitdelay.setdelay)

- /getdelay command to get the current hit delay in ticks (permission: nohitdelay.getdelay)

- Config.yml that allows you to change hit delay in ticks

# Planned Features
- If you have any suggestions or feature requests, please create a new issue in the project's GitHub repository.

# Installation
- Download the latest release of the plugin from the releases page.
- Copy the downloaded .jar file to the plugins directory of your Minecraft server.
- Start the server.
- After installation, CustomizableNoHitDelay will automatically change the hit invulnerability period for entities.

Check config.yml for more info!

# Config.yml
```yaml
# Delay value (Note: Do make sure the delay is at least 2 because setting it below that will make some hits not register")
delay: 2 # in ticks
# Default delay in minecraft is 20 ticks

# In game you can use /setdelay command to change the delay that you wish and it will automatically change here
# You can also use /getdelay to get the current hit delay in ticks
```

# License
AutoViaUpdater is released under the MIT License. See the LICENSE file for more information.

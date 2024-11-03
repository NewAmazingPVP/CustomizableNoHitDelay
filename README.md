# CustomizableNoHitDelay
CustomizableNoHitDelay is a plugin for Spigot that improves PVP experience by allowing users to change entity hit invulnerability delay ticks

Spigot Link: https://www.spigotmc.org/resources/customizablenohitdelay.109763/

# Features
- Compatible with Minecraft versions 1.8 and higher for both Spigot, including the latest versions and most likely future versions as well.

- /nohitdelay setdelay command allows you to change hit delay ticks from ingame (permission: nohitdelay.setdelay)

- /nohitdelay getdelay command to get the current hit delay in ticks (permission: nohitdelay.getdelay)

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
# Delay value
delay: 2 # in ticks
# Default delay in Minecraft is 20 ticks

# Experimental
knockback-multiplier: 1.0

# Mode options:
# pvp - No hit delay only applies in player versus player combat.
# evp - No hit delay only applies when entities attack players.
# pvp-evp - No hit delay applies to both PvP and EvP interactions as long as a player is involved.
# any - No hit delay applies to any entity interactions.
# player-only - No hit delay applies when players attack any entity, but entities do not have no hit delay when attacking.
mode: any

messages:
  prefix: "&f[NoHitDelay] "
  use-prefix: true
  delay-set: "&aDelay set to: &e%value%&a."
  invalid-delay: "&cInvalid delay value. Please enter a number."
  usage-setdelay: "&cUsage: /nohitdelay setdelay <delay>"
  current-delay: "&aDelay is currently set to: &e%value%"
  mode-set: "&aMode set to: &e%value%&a."
  invalid-mode: "&cInvalid mode value. Please use 'pvp', 'evp', 'pvp-evp', 'any', or 'player-only'."
  usage-setmode: "&cUsage: /nohitdelay setmode <mode>"
  current-mode: "&aMode is currently set to: &e%value%"
  config-reloaded: "&aConfiguration reloaded."
  command-list:
    - "%prefix% command list"
    - "&f"
    - "&f/nohitdelay setdelay <amount> &7(~~~)"
    - "&f/nohitdelay getdelay &7(~~~)"
    - "&f/nohitdelay setmode <mode> &7(~~~)"
    - "&f/nohitdelay getmode &7(~~~)"
    - "&f/nohitdelay reloadconfig &7(~~~)"
```

# License
CustomizableNoHitDelay is released under the MIT License. See the LICENSE file for more information.

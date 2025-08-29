# NoHitDelay

NoHitDelay is a lightweight Spigot/Paper/Folia plugin that lets you customize the hit invulnerability delay (no-damage
ticks) for entities, improving PvP/PvE responsiveness.

Spigot page: https://www.spigotmc.org/resources/customizablenohitdelay.109763/

**Highlights**

- Wide compatibility: Spigot/Paper 1.8+ and Folia support.
- Runtime-safe on Folia via region scheduler; no compile-time Folia dependency.
- Simple commands and config for delay, modes, and optional knockback multiplier.
- CI builds on every push; releases on tags.

## Compatibility

- Servers: Spigot, Paper, Purpur, Folia
- Versions: 1.8 and newer
- Folia: Declared `folia-supported: true` and uses region-safe scheduling.

## Installation

- Download the latest JAR from GitHub Releases or Spigot.
- Drop the JAR into your server's `plugins/` folder.
- Restart or reload your server.

## Commands

- `/nohitdelay` — Shows help.
- `/nohitdelay setdelay <ticks>` — Set invulnerability ticks.
- `/nohitdelay getdelay` — Show current delay.
- `/nohitdelay setmode <mode>` — Set operating mode.
- `/nohitdelay getmode` — Show current mode.
- `/nohitdelay reloadconfig` — Reload configuration.

## Permissions

- `nohitdelay.manage` — Access to all subcommands (default: OP).

## Modes

- `pvp` — Player vs Player only.
- `evp` — Entities vs Player only.
- `pvp-evp` — Any interaction where a player is involved.
- `any` — Any entity interactions.
- `player-only` — Player damages any entity; entities do not gain no-hit delay when damaging.

## Configuration

Default `config.yml`:

```yaml
# Delay value
delay: 2 # in ticks
# Default delay in Minecraft is 20 ticks

# Experimental
knockback-multiplier: 1.0 # Set to 1.0 to disable knockback changes

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

Notes:

- Hex colors in messages using `&#RRGGBB` are converted where supported (1.16+).
- On legacy 1.8 servers, only `&` color codes render.

## Building

- Prereqs: JDK 8+ and Maven.
- Build: `mvn -DskipTests package`
- Output: `target/NoHitDelay-<version>.jar`

CI:

- GitHub Actions builds on each push and uploads the JAR as an artifact.
- Tagging `vX.Y.Z` creates a GitHub Release and attaches the built JAR.

## Versioning

This project follows Semantic Versioning:

- MAJOR: incompatible changes (e.g., config format changes, dropped MC version).
- MINOR: new features, compatible changes.
- PATCH: bug fixes only.

Current release: `1.3.0`.

## Changelog

See `CHANGELOG.md` for a human-readable list of changes per release.

## License

MIT — see `LICENSE`.

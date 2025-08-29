<div align="center">

# **NoHitDelay v1.3.0**

*Customize the no-damage tick window for responsive combat.*

[![SpigotMC](https://img.shields.io/badge/SpigotMC-Resource-orange)](https://www.spigotmc.org/resources/customizablenohitdelay.109763/)
![Platforms](https://img.shields.io/badge/Platforms-Spigot%20%7C%20Paper%20%7C%20Folia-5A67D8)
![MC](https://img.shields.io/badge/Minecraft-1.8%E2%86%92Latest-2EA043)
![Java](https://img.shields.io/badge/Java-8%2B-1F6FEB)
![License](https://img.shields.io/badge/License-MIT-0E8A16)

</div>

> TL;DR
> Drop in the jar → set `delay` → choose a mode. The plugin adjusts entity invulnerability (no-damage ticks) safely on modern servers and Folia.

---

## Table of Contents

* [Highlights](#highlights)
* [Platforms & Requirements](#platforms--requirements)
* [Installation](#installation)
* [Quick Start](#quick-start)
* [Configuration](#configuration)
* [Commands & Permissions](#commands--permissions)
* [How It Works](#how-it-works)
* [Troubleshooting & FAQ](#troubleshooting--faq)
* [Building from Source](#building-from-source)
* [Contributing & License](#contributing--license)

---

## Highlights

* Set entity invulnerability ticks (no-damage ticks) to your preference.
* Five modes: `pvp`, `evp`, `pvp-evp`, `any`, `player-only`.
* Optional knockback multiplier.
* Safe on Folia via region scheduling (no cross-thread access).
* Works back to 1.8; Java 8 compatible.

## Platforms & Requirements

**Server platforms**

* Spigot
* Paper
* Folia

**Minecraft:** 1.8 → Latest

**Java:** 8+

> Note: Folia is a separate server platform. This plugin detects and uses Folia's region scheduler when present.

## Installation

1. Download the latest `.jar` from [Spigot](https://www.spigotmc.org/resources/customizablenohitdelay.109763/) or GitHub Releases.
2. Place it into your server’s `plugins/` directory.
3. Start the server to generate `config.yml`.

## Quick Start

1. Set your delay (ticks):

   `/nohitdelay setdelay 2`

2. Choose a mode:

   `/nohitdelay setmode pvp` (or `evp`, `pvp-evp`, `any`, `player-only`)

3. Optional: tweak knockback in `config.yml`.

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

* Hex colors `&#RRGGBB` are converted on 1.16+.

## Commands & Permissions

**Commands**

* `/nohitdelay` — Help
* `/nohitdelay setdelay <ticks>` — Set invulnerability ticks
* `/nohitdelay getdelay` — Show current delay
* `/nohitdelay setmode <mode>` — Set mode
* `/nohitdelay getmode` — Show current mode
* `/nohitdelay reloadconfig` — Reload configuration

**Permission**

* `nohitdelay.manage` — Access to all subcommands (default: OP)

## How It Works

The plugin listens for damage events and briefly schedules an update to the damaged entity:

1. Optionally scales knockback by a configurable multiplier.
2. Sets the entity’s `noDamageTicks` to the configured value.

On Folia, tasks run on the entity’s region scheduler. On Spigot/Paper, they run on the Bukkit scheduler.

## Troubleshooting & FAQ

**Does this remove the 1.9+ attack cooldown?**

No. This controls entity invulnerability (`noDamageTicks`). The 1.9+ attack cooldown is a separate mechanic.

**No change in-game?**

Check that your mode matches the situation (e.g., `pvp` vs `any`). Other plugins may modify velocity or damage behavior.

**Knockback multiplier seems subtle.**

It scales the entity’s current velocity on the next tick. Some servers or plugins clamp or alter knockback.

## Building from Source

Requirements: JDK 8+, Maven.

```bash
mvn -DskipTests package
```

The jar is produced under `target/`.

## Contributing & License

Issues and PRs are welcome: https://github.com/NewAmazingPVP/NoHitDelay/issues

License: MIT — see `LICENSE`.

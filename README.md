# SSJ Essentials Plugin

A custom plugin collection designed to enhance the SSJ Plugin functionality.

## 🚀 Features

- **Flight Control** - Toggle flying ability for players
- **Player Maintenance** - Easily restore health and hunger
- **Moderation Tools** - Temporary ban system for server management
- **Customization** - Change player nicknames and gamemodes
- **Permission Based** - Fine-grained permission control for all commands

## 📦 Installation

1. Download the plugin JAR file from the [releases page](link-to-releases)
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure permissions in your permissions plugin

### 📝 Default Permissions

```yaml
permissions:
  ssjessentials.fly:
    description: Allows player to toggle flight mode for themselves
    default: op
  ssjessentials.fly.others:
    description: Allows player to toggle flight mode for others
    default: op
  ssjessentials.vanish:
    description: Allows player to toggle visibility for themselves
    default: op
  ssjessentials.vanish.others:
    description: Allows player to toggle visibility for others
    default: op
  ssjessentials.heal:
    description: Allows player to heal themselves
    default: op
  ssjessentials.heal.others:
    description: Allows player to heal others
    default: op
  ssjessentials.feed:
    description: Allows player to feed themselves
    default: op
  ssjessentials.feed.others:
    description: Allows player to feed others
    default: op
  ssjessentials.freeze:
    description: Allows player to freeze themselves
    default: op
  ssjessentials.freeze.others:
    description: Allows player to freeze others
    default: op
  ssjessentials.tempban:
    description: Allows player to temporarily ban others
    default: op
  ssjessentials.nick:
    description: Allows player to change their nickname
    default: op
  ssjessentials.nick.others:
    description: Allows player to change others' nicknames
    default: op
  ssjessentials.gamemode:
    description: Allows player to change their gamemode
    default: op
  ssjessentials.gamemode.others:
    description: Allows player to change others' gamemode
    default: op
  ssjessentials.reload:
    description: Allows player to reload the plugin configuration
    default: op
  ssjessentials.banlist:
    description: Allows player to manage the ban list
    default: op
  ssjessentials.god:
    description: Allows player to toggle god mode
    default: op
  ssjessentials.god.others:
    description: Allows player to toggle god mode for others
    default: op
  ssjessentials.unban:
    description: Allows player to unban others
    default: op
  ssjessentials.spawn:
    description: Allows player to spawn a mob
    default: op
  ssjessentials.spawn.mob:
    description: Allows player to spawn a mob
    default: op
  ssjessentials.spawn.mob.tp:
    description: Allows player to spawn a mob and teleport to it to a target player's location
    default: op
  ssjessentials.setspawn:
    description: Allows player to set the server's spawn point
    default: op
  ssjessentials.tpr:
    description: Allows player to ask to teleport to another player
    default: op
  ssjessentials.tp.staff:
    description: Allows player to teleport to another player if you are staff
    default: op
  ssjessentials.tpr.accept:
    description: Allows player to accept a tp request from /tpr
    default: op
  ssjessentials.kill:
    description: Allows player to kill a target player or entity
    default: op
  ssjessentials.kill.all:
    description: Allows player to kill all players and entitys
    default: op
  ssjessentials.kill.entitys:
    description: Allows player to kill an entity
    default: op
  ssjessentials.ban:
    description: Allows player to ban a target player
    default: op
  ssjessentials.kick:
    description: Allows player to kick a target player
    default: op
  ssjessentials.kick.all:
    description: Allows player to kick all players
    default: op
  ssjessentials.sethome:
    description: Allows player to set a home location
    default: op
  ssjessentials.sethome.del:
    description: Allows player to delete a home location
    default: op
  ssjessentials.sethome.edit:
    description: Allows player to edit a home location name
    default: op
  ssjessentials.sethome.reset:
    description: Allows player to reset a home location
    default: op
  ssjessentials.setwarp:
    description: Allows player to set a warp location
    default: op
  ssjessentials.setwarp.del:
    description: Allows player to delete a warp location
    default: op
  ssjessentials.setwarp.edit:
    description: Allows player to edit a warp location name
    default: op
  ssjessentials.setwarp.reset:
    description: Allows player to reset a warp location
    default: op
```

### 🎮 Commands

| Command | Command Two | Command Three | Command Four | Command Five | Command Six |Description | Permission One | Permission Two | Permission Three | Permission Four | Permission Five |
|-------------|-------------|-------------|-------------|-------------|-------------|-------------|-------------|-------------|-------------|-------------|-------------|
| `/fly` | `/fly <player>` | `None` | `None` | `None` | `None` | Toggle flight mode | `ssjessentials.fly` | `ssjessentials.fly.others` | `None` | `None` | `None` |
| `/feed` | `/feed <player>` | `None` | `None` | `None` | `None` | Restore hunger bar | `ssjessentials.feed` | `ssjessentials.feed.others` | `None` | `None` | `None` |
| `/heal` | `/heal <player>` | `None` | `None` | `None` | `None` | Restore health | `ssjessentials.heal` | `ssjessentials.heal.others` | `None` | `None` | `None` |
| `/tempban <player> <duration> [reason]` | `None` | `None` | `None` | `None` | `None` | Temporarily ban a player | `ssjessentials.tempban` | `None` | `None` | `None` | `None` |
| `/nick <nickname>` | `/nick <player> <nickname>` | `None` | `None` | `None` | `None` | Change your nickname | `ssjessentials.nick` | `ssjessentials.nick.others` | `None` | `None` | `None` |
| `/gm <0/1/2/3>` | `/gm <player> <0/1/2/3>` | `None` | `None` | `None` | `None` | Change gamemode | `ssjessentials.gamemode` | `ssjessentials.gamemode.others` | `None` | `None` | `None` |
| `/freeze` | `/freeze <player>` | `None` | `None` | `None` | `None` | Freeze player | `ssjessentials.freeze`| `ssjessentials.freeze.others` | `None` | `None` | `None` |
| `/vanish` | `/vanish <player>` | `None` | `None` | `None` | `None` | Toggle Vanish | `ssjessentials.vanish`| `ssjessentials.vanish.others` | `None` | `None` | `None` |
| `/ssjereload` | `None` | `None` | `None` | `None` | `None` | Reloads the plugin | `ssjessentials.reload` | `None` | `None` | `None` | `None` |
| `//ban list` | `/banlist` | `None` | `None` | `None` | `None` | Gives a list of banned players with the ban reason, time of ban, who banned the banned player, and when the banned player will be unbanned | `ssjessentials.banlist` | `None` | `None` | `None` | `None` |
| `/god` | `/god <player>` | `None` | `None` | `None` | `None` | Toggles god mod | `ssjessentials.god` | `ssjessentials.god.others` | `None` | `None` | `None` |
| `/unban <player>` | `None` | `None` | `None` | `None` | `None` | Unbans a banned player | `ssjessentials.unban` | `None` | `None` | `None` | `None` |
| `/spawn` | `/spawn <playername>` | `/spawn <entity> (optional <amount>) (optional <health>) (optional <playername>)` | `/spawnall` | `None` | `None` | Teleports you or target player to the spawn point OR spawns a mob where you are standing or at a target player | `ssjessentials.spawntp` | `ssjessentials.spawntp.others` | `ssjessentials.spawn.mob` | `None` | `None` |
| `/set spawn` | `/set spawnpoint` | `None` | `None` | `None` | `None` | Sets the server's spawn point | `ssjessentials.setspawn` | `None` | `None` | `None` | `None` |
| `/tpr <playername>` | `/tp <playername> OR <coords>` | `/tp <playername> (to) <playername> OR <spawn> OR <coords>` | `/tp all <playername OR spawn OR coords>` | `None` | `None` | Ask to teleport to another player (or telelport to a player or teleport a player to another player or spawn if you are staff) | `ssjessentials.tpr` | `None` | `None` | `None` | `None` |
| `/tpr accept` | `None` | `None` | `None` | `None` | `None` | Accept a tp request from /tpr | `ssjessentials.tpr.accept`| `None` | `None` | `None` | `None` |
| `/killall` | `/kill <player>` | `/kill <entity>` | `/killall entities` | `None` | `None` | Kills all players and entitys, kills a target player, or kills an entity | `ssjessentials.kill` | `ssjessentials.kill.all` | `ssjessentials.kill.entitys` | `None` | `None` |
| `/ban <playername>` | `None` | `None` | `None` | `None` | `None` | Bans the target player | `ssjessentials.ban` | `None` | `None` | `None` | `None` |
| `/kick <playername>` | `/kickall` | `None` | `None` | `None` | `None` | Kicks the target player | `ssjessentials.kick` | `ssjessentials.kick.all` | `None` | `None` | `None`|
| `/sethome <name>` | `/delhome <name>` | `/edithome <current name> <new name>` | `/resethome <home name>` | `//home list` | `/home <home name>` | Sets player home location, or teleports the player to their custom home location | `None` | `ssjessentials.sethome.del` | `ssjessentials.sethome.edit` | `ssjessentials.sethome.reset` | `None`|
| `/warp <warp name>` | `/setwarp <warp name>` | `/delwarp <warp name>` | `//warp list` | `/resetwarp <warp name>` | `/warpall` | Sets server warp location | `None` | `ssjessentials.setwarp.del` | `ssjessentials.setwarp.edit` | `ssjessentials.setwarp.reset` | `None`|


## 🔧 Configuration
Create or modify `plugins/SSJEssentials/config.yml`:

```yaml
# General Settings
settings:
  # Prefix for plugin messages
  prefix: "&7[&bSSJ&7] "
  
# Flight Settings
flight:
  # Maximum flight speed (1-10)
  max-speed: 2
  # Whether flight should be disabled on combat
  disable-on-combat: true

# Nickname Settings
nickname:
  # Maximum length for nicknames
  max-length: 16
  # Whether to allow color codes in nicknames
  allow-colors: true
  # Blocked words in nicknames
  blocked-words: []

# Tempban Settings
tempban:
  # Default ban reason if none provided
  default-reason: "No reason specified"
  # Maximum ban duration (in days)
  max-duration: 30
  # Whether to broadcast tempbans
  broadcast: true

# Ban Settings
ban:
  # Whether to broadcast bans
  broadcast: true
  # Default ban reason if none provided
  default-reason: "No reason specified"
```

Modify `plugins/SSJEssentials/PlayerData/<playername>.yml`:

```yaml

# Player Settings

uuid: 

name: 

flying: false

vanished: false

frozen: false

gamemode: SURVIVAL

godmode: false

nickname: 

```

## 📝 License

This project is currently unlicensed. This means that while the code is publicly available, there are no explicit permissions granted for its use, modification, or distribution. 

If you'd like to use this plugin, please contact the developer for permission.

## 🙏 Acknowledgments

- SSJ Plugin - [Link to SSJ Plugin]

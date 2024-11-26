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
```

### 🎮 Commands

| Command | Command Other | Description | Permission One | Permission Two |
|-------------|-----------------|-----------------------------|------------|------------|
| `/fly` | `/fly <player>` | Toggle flight mode | `ssjessentials.fly` | `ssjessentials.fly.others` |
| `/feed` | `/feed <player>` | Restore hunger bar | `ssjessentials.feed` | `ssjessentials.feed.others` |
| `/heal` | `/heal <player>` | Restore health | `ssjessentials.heal` | `ssjessentials.heal.others` |
| `/tempban <player> <duration> [reason]` | `None` | Temporarily ban a player | `ssjessentials.tempban` | `None` |
| `/nick <nickname>` | `/nick <player> <nickname>` | Change your nickname | `ssjessentials.nick` | `ssjessentials.nick.others` |
| `/gm <0/1/2/3>` | `/gm <player> <0/1/2/3>` | Change gamemode | `ssjessentials.gamemode` | `ssjessentials.gamemode.others` |
| `/freeze` | `/freeze <player>` | Freeze player | `ssjessentials.freeze`| `ssjessentials.freeze.others` |
| `/vanish` | `/vanish <player>` | Toggle Vanish | `ssjessentials.vanish`| `ssjessentials.vanish.others` |
| `/ssjereload` | `None` | Reloads the plugin | `ssjessentials.reload` | `None` |
| `//ban list` | `/banlist` | Gives a list of banned players | `ssjessentials.banlist` | `None` |
| `/god` | `/god <player>` | Toggles god mod | `ssjessentials.god` | `ssjessentials.god.others` |
| `/unban <player>` | `None` | Unbans a banned player | `ssjessentials.unban` | `None` |

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

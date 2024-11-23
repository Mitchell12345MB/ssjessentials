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
  ssj.fly:
    description: Allows player to use /fly command
    default: op
  ssj.feed:
    description: Allows player to use /feed command
    default: op
  ssj.heal:
    description: Allows player to use /heal command
    default: op
  ssj.tempban:
    description: Allows player to temporarily ban other players
    default: op
  ssj.nick:
    description: Allows player to change their nickname
    default: op
  ssj.gamemode:
    description: Allows player to change their gamemode
    default: op
  ssj.freeze:
    description: Allows player to freeze another player
    default: op
```

### 🎮 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/fly` | Toggle flight mode | `ssj.fly` |
| `/feed` | Restore hunger bar | `ssj.feed` |
| `/heal` | Restore health | `ssj.heal` |
| `/tempban <player> <duration> [reason]` | Temporarily ban a player | `ssj.tempban` |
| `/nick <nickname>` | Change your nickname | `ssj.nick` |
| `/gm <0/1/2/3>` | Change gamemode | `ssj.gamemode` |
| `/freeze <player` | Freeze player | `ssj.freeze`|

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

# Player Settings

uuid: <player UUID>

name: <playername>

flying: false

vanished: false

```

## 📝 License

This project is currently unlicensed. This means that while the code is publicly available, there are no explicit permissions granted for its use, modification, or distribution. 

If you'd like to use this plugin, please contact the developer for permission.

## 🙏 Acknowledgments

- SSJ Plugin - [Link to SSJ Plugin]

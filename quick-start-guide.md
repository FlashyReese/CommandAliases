# Quick Start Guide

## Verifying Installation

To begin with the basics after the installation, we will type the following command in-game to verify the success of the installation.

```
/commandaliases
```

Output:

```
Running Command Aliases v1.0.0-mc1.19.2
```

The output above may vary depending on the version of the installation chosen.

## Structure

```
.minecraft
├── config
│   ├── commandaliases - Directory of command aliases files.
│   ├── commandaliases-client - Directory of command aliases files for clients.
│   └── command-aliases-config.json - Configuration file for command aliases.
├── commandaliases.client - LevelDB Client Path
├── saves - Single-player worlds
│   └── <world_save>
│       └── commandaliases - LevelDB Path
└── world - Multiplayer world
    └── commandaliases - LevelDB Path
```

Now that you are familiar with the structure of the mod, we will begin covering the basic commands that are essential for assisting in the testing of command aliases when writing them.

```
/commandaliases reload
```

Output:

```
Reloading all Command Aliases!
Reloaded all Command Aliases!
```

Console Output:

```
[16:14:48] [Render thread/INFO] (Minecraft) [System] [CHAT] Reloading all Command Aliases!
[16:14:48] [Server thread/INFO] (Command Aliases) 
commandaliases
[16:14:48] [Server thread/INFO] (Command Aliases) Registered/Reloaded all your commands :P, you can now single command nuke!
[16:14:48] [Render thread/INFO] (Minecraft) [System] [CHAT] Reloaded all Command Aliases!
```

At present, since our directory of command aliases files is empty, we receive a blank structure. To demonstrate how this would appear, I will be downloading the files from [Essentials Command Aliases TOML](https://github.com/FlashyReese/CommandAliases-Collection/tree/1.0.0/essentials/toml) and copying them to `.minecraft/config/commandaliases` or a similar path if you are running a server installation.

If we then run the previous command `/commandaliases reload` again, we will be able to see the structure of the output.

Console Output:

```
[16:14:48] [Render thread/INFO] (Minecraft) [System] [CHAT] Reloading all Command Aliases!
[16:14:48] [Server thread/INFO] (Command Aliases) 
commandaliases
└── essentials
    ├── afk
    │   ├── afk-checker.toml - Successfully loaded
    │   └── afk.toml - Successfully loaded
    ├── tpahere.toml - Successfully loaded
    ├── tpa.toml - Successfully loaded
    ├── essentials.toml - Successfully loaded
    ├── tpaccept.toml - Successfully loaded
    ├── home.toml - Successfully loaded
    ├── spawn.toml - Successfully loaded
    ├── setspawn.toml - Successfully loaded
    ├── sethome.toml - Successfully loaded
    ├── delhome.toml - Successfully loaded
    └── delspawn.toml - Successfully loaded
[16:14:48] [Server thread/INFO] (Command Aliases) Registered/Reloaded all your commands :P, you can now single command nuke!
[16:14:48] [Render thread/INFO] (Minecraft) [System] [CHAT] Reloaded all Command Aliases!
```

The output above may vary as more changes may have been made to the Essentials Command Aliases Collection in the future.

## Supported Formats

Currently, Command Aliases support multiple data formats, including the following:

* JSON - `.json`
* JSON5 - `.json5`
* TOML - `.toml`
* YAML - `.yaml` or `.yml`

## Creating a Command Alias

To begin creating our first command aliases and becoming familiar with the format and types of command aliases mode, we will start by creating a basic redirect without arguments using the `COMMAND_REDIRECT_NOARG` command mode.&#x20;

We can find other command modes on the [Command Modes](command-modes.md) page.&#x20;

To create our first command aliases file with our chosen data format, we will start by creating a file named `survival.json` placed in `.minecraft/config/commandaliases/`. Depending on the preferred format, the proper extension should be used accordingly.&#x20;

We must also define our `schemaVersion` and the command mode we will be using within our chosen data format.

* JSON or JSON5

```json5
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT_NOARG"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT_NOARG"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT_NOARG
```

As the name of the alias indicates, we are creating an alias for the `/gamemode survival` command. We will need to define our command name and redirect to fields accordingly.

* JSON or JSON5

```json5
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT_NOARG",
    "command": "survival",
    "redirectTo": "gamemode survival"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT_NOARG"
command = "survival"
redirectTo = "gamemode survival"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT_NOARG
command: survival
redirectTo: gamemode survival
```

To complete the process, we can reload our command aliases using the `/commandaliases reload` command, which should register our alias and allow us to verify it using the output of the console.

Console Output:

```
[18:55:06] [Render thread/INFO] (Minecraft) [System] [CHAT] Reloading all Command Aliases!
[18:55:06] [Server thread/INFO] (Command Aliases) 
commandaliases
└── survival.toml - Successfully loaded
[18:55:06] [Server thread/INFO] (Command Aliases) Registered/Reloaded all your commands :P, you can now single command nuke!
[18:55:06] [Render thread/INFO] (Minecraft) [System] [CHAT] Reloaded all Command Aliases!
```

To test our work, let's try using the `/survival` command. If everything was done correctly, the command alias should be working as intended.&#x20;

Congratulations, you have now created your first command alias. You can learn more about different command modes on the [Command Modes](command-modes.md) page.

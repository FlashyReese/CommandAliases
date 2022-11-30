# Getting Started

## Verifying Installation

Let's start with the basics after the installation. We will type the following command in-game to verify that the installation was successful.

```
/commandaliases
```

Output:

```
Running Command Aliases v1.0.0-mc1.19.2
```

The output above may differ depending on the version you have chosen to install.

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

Now that you have familiarized yourself with the structure of the mod, we are going to be covering summon basic commands that are essential to assist in testing command aliases when writing them.

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

Currently, since our directory of command aliases files is empty we get a blank structure. To demonstrate how this would appear, I will be downloading the files from [Essentials Command Aliases TOML](https://github.com/FlashyReese/CommandAliases-Collection/tree/1.0.0/essentials/toml) and copying them to `.minecraft/config/commandaliases` or a similar path if you are running a server installation.

Now if we run the previous command `/commandaliases reload` again this time we can see the structure of the output.

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

The output above may differ since more changes may have made it into the Essentials Command Aliases Collection in the future.

## Supported Formats

Command Aliases currently support multiple data formats. The following are:

* JSON - `.json`
* JSON5 - `.json5`
* TOML - `.toml`
* YAML - `.yaml` or `.yml`

## Creating a Command Alias

Let's start creating our first command aliases and get familiar with the format and types of command aliases mode.

We are going to start off by creating a basic redirect without arguments using the command mode `COMMAND_REDIRECT_NOARG`. We can find other command modes on the [Command Modes](command-modes.md) page.

Let's create our first command aliases file with our data format of choice. Start by creating a filed name `survival.json` placed in `.minecraft/config/commandaliases/`. Depending on the format you prefer used the proper extension accordingly.

We must define our `schemaVersion` and the command mode we are going to be using within our data format.

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

As you can tell by the name of the alias we are creating an alias for `/gamemode survival`. We will need to define our command name and redirect to fields.

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

Now we can reload our command aliases using `/commandaliases reload` and we should have our alias registered and can be verified with the output of the console.

Console Output:

```
[18:55:06] [Render thread/INFO] (Minecraft) [System] [CHAT] Reloading all Command Aliases!
[18:55:06] [Server thread/INFO] (Command Aliases) 
commandaliases
└── survival.toml - Successfully loaded
[18:55:06] [Server thread/INFO] (Command Aliases) Registered/Reloaded all your commands :P, you can now single command nuke!
[18:55:06] [Render thread/INFO] (Minecraft) [System] [CHAT] Reloaded all Command Aliases!
```

Now let's try our command `/survival`. If everything was done correctly the command alias should be working as intended.

Congratulations you have now created your first command alias. You can read more about different command modes on the [Command Modes](command-modes.md) page.

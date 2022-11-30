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

Currently, since our directory of command aliases files is empty we get a blank structure. To demonstrate how this would appear, I will be downloading the files from [Essentials Command Aliases TOML](https://github.com/FlashyReese/CommandAliases-Collection/tree/1.0.0/essentials/toml) and copying them to \`.minecraft/config/commandaliases\` or a similar path if you are running a server installation.

Now if we run the previous command \`/commandaliases reload\` again this time we can see the structure of the output.

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

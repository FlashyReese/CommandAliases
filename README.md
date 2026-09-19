# Command Aliases
![Java 25](https://img.shields.io/badge/language-Java%2025-9B599A.svg?style=flat-square)
[![Project License](https://img.shields.io/github/license/FlashyReese/CommandAliases?style=flat-square)](LICENSE.txt)
![Environment: Client](https://img.shields.io/badge/environment-client/server-1976d2?style=flat-square)
![Mod loaders: Fabric and NeoForge](https://img.shields.io/badge/modloaders-Fabric%20%7C%20NeoForge-1976d2?style=flat-square)
![Version](https://img.shields.io/github/v/tag/FlashyReese/CommandAliases?label=version&style=flat-square)
[![CurseForge](http://cf.way2muchnoise.eu/title/409389.svg)](https://www.curseforge.com/minecraft/mc-mods/commandaliases)

Alternate short commands for complex commands (with tab completion)

## Purpose

This mod allows you to rebind multiple commands into a single command, this also implies commands that may require 
vanilla operator permissions.

Additionally, provide format to start building new commands from scratch.

## Commands

#### How to make a command?

You can find more information about them at the [wiki](https://github.com/FlashyReese/CommandAliases/wiki). 

#### Running into issues?
Here are some [current issues](doc/BROKEN.md) with Command Aliases


## Building from source

#### Prerequisites

- Java 25 or above

#### Compiling

Navigate to the directory you've cloned this repository and launch a build with Gradle using `gradlew build` (Windows)
or `./gradlew build` (macOS/Linux). If you are not using the Gradle wrapper, simply replace `gradlew` with `gradle`
or the path to it.

The initial setup may take a few minutes. After Gradle has finished building everything, you can find the resulting
artifacts in `fabric/build/libs` and `neoforge/build/libs`.

## License

Command Aliases is license under MIT, a free and open-source license. For more information, please see the
[license file](LICENSE.txt).

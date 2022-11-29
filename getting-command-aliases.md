# Getting Command Aliases

## Acquisition

Before we begin, we must download the mod or compile the mod.

### Downloading

Command Aliases can be found on many platforms. The official downloads are located in the following platforms:

* [GitHub](https://github.com/FlashyReese/CommandAliases/releases/latest)
* [CurseForge](https://www.curseforge.com/minecraft/mc-mods/commandaliases/files)
* [Modrinth](https://modrinth.com/mod/commandaliases/versions)

### Compiling (Advanced)

#### Prerequisites

* [Java Development Kit 17](https://adoptium.net/) or later
* [Git ](https://git-scm.com/)(optional)

#### Instructions

1.  Clone the repository and navigate into the cloned repository

    ```bash
    git clone https://github.com/FlashyReese/CommandAliases.git
    cd CommandAliases
    ```
2.  Navigate to the directory you've cloned this repository and launch a build with Gradle. If you are not using the Gradle wrapper, simply replace `gradlew` with `gradle` or the path to it.

    * Windows

    ```batch
    gradlew build
    ```

    * Linux/macOS

    ```bash
    ./gradlew build 
    ```
3. The initial setup may take a few minutes. After Gradle has finished building everything, you can find the resulting artifacts in `build/libs`.

## Installation

CommandAliases is currently only a Fabric mod and runs on the [Fabric Mod Loader](https://fabricmc.net/).

Depending on the environment of the installation, the Fabric Mod Loader will generate a new folder named \`mods\`, all fabric mods should be placed in this folder.

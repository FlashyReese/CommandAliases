# Getting Command Aliases

## Acquisition

You can install Command Aliases from a release build or compile it from source.

### Downloading

Command Aliases can be found on many platforms. The official downloads are located in the following platforms:

* [GitHub](https://github.com/FlashyReese/CommandAliases/releases/latest)
* [CurseForge](https://www.curseforge.com/minecraft/mc-mods/commandaliases/files)
* [Modrinth](https://modrinth.com/mod/commandaliases/versions)

### Compiling (Advanced)

#### Prerequisites

* [Java Development Kit 21](https://adoptium.net/) or later for current Minecraft 1.21 builds
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

Command Aliases is a Fabric mod and runs on the [Fabric Mod Loader](https://fabricmc.net/).

Place the downloaded `.jar` file in the `mods` folder for your client or server. If the folder does not exist yet, start Fabric once and it will be created.

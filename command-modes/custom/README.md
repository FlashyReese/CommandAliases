# Custom

When using command mode `COMMAND_CUSTOM`, it creates a new command that can run one or more existing commands with required/optional arguments passthrough.

### Create a custom command using `COMMAND_CUSTOM`

First, we need to set our command mode to `COMMAND_CUSTOM`.

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_CUSTOM"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_CUSTOM"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_CUSTOM
```

Now that we have set our command mode, we can define our new command.

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_CUSTOM",
    "command": "tools"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_CUSTOM"
command = "tools"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_CUSTOM
command: tools
```

Let's send a message to the command executor(the player/console that runs the command).

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_CUSTOM",
    "command": "tools",
    "message": "Here are some free wooden tools"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_CUSTOM"
command = "tools"
message = "Here are some free wooden tools"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_CUSTOM
command: tools
message: Here are some free wooden tools
```

Let's get the executor's name and bind it to our message.

We can get the executor's name using `$executor_name()`.

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_CUSTOM",
    "command": "tools",
    "message": "Here are some free wooden tools, $executor_name()!"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_CUSTOM"
command = "tools"
message = "Here are some free wooden tools, $executor_name()!"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_CUSTOM
command: tools
message: 'Here are some free wooden tools, $executor_name()!'
```

**Scenario**

1. Player123 runs `/tools`
2. Output will return `Here are some free wooden tools, Player123!`

Now let's give the player a wooden sword. This can be done by using the field `actions` which takes in an array of command action objects.

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_CUSTOM",
    "command": "tools",
    "message": "Here are some free wooden tools, $executor_name()!",
    "actions": []
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_CUSTOM"
command = "tools"
message = "Here are some free wooden tools, $executor_name()!"

[[actions]]
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_CUSTOM
command: tools
message: 'Here are some free wooden tools, $executor_name()!'
actions: []
```

We will need to create a command action object inside the custom command.

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_CUSTOM",
    "command": "tools",
    "message": "Here are some free wooden tools, $executor_name()!",
    "actions": [
        {
            "command": "give $executor_name() minecraft:wooden_sword"
        }
    ]
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_CUSTOM"
command = "tools"
message = "Here are some free wooden tools, $executor_name()!"

[[actions]]
command = "give $executor_name() minecraft:wooden_sword"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_CUSTOM
command: tools
message: 'Here are some free wooden tools, $executor_name()!'
actions:
  - command: 'give $executor_name() minecraft:wooden_sword'
```

Before we go ahead and run the command. We need to define the type of execution between `CLIENT` and `SERVER`. Using `CLIENT` will imply that the executors run the command(in this case the `/give` command). This means if the executor doesn't have permission to use the `/give` command our custom command will error out. Now to get around this issue, we can use `SERVER`. Using `SERVER` will imply the internal/dedicated server executes said the custom command, not the executor. In this case for our `/tools` command, we will need to use `SERVER` since the players don't have access to the `/give` command.

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_CUSTOM",
    "command": "tools",
    "message": "Here are some free wooden tools, $executor_name()!",
    "actions": [
        {
            "command": "give $executor_name() minecraft:wooden_sword",
            "commandType": "SERVER"
        }
    ]
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_CUSTOM"
command = "tools"
message = "Here are some free wooden tools, $executor_name()!"

[[actions]]
command = "give $executor_name() minecraft:wooden_sword"
commandType = "SERVER"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_CUSTOM
command: tools
message: 'Here are some free wooden tools, $executor_name()!'
actions:
  - command: 'give $executor_name() minecraft:wooden_sword'
    commandType: SERVER
```

Now let's give the player all the wooden tools.

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_CUSTOM",
    "command": "tools",
    "message": "Here are some free wooden tools, $executor_name()!",
    "actions": [
        {
            "command": "give $executor_name() minecraft:wooden_sword",
            "commandType": "SERVER"
        },
        {
            "command": "give $executor_name() minecraft:wooden_pickaxe",
            "commandType": "SERVER"
        },
        {
            "command": "give $executor_name() minecraft:wooden_axe",
            "commandType": "SERVER"
        },
        {
            "command": "give $executor_name() minecraft:wooden_shovel",
            "commandType": "SERVER"
        }
    ]
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_CUSTOM"
command = "tools"
message = "Here are some free wooden tools, $executor_name()!"

[[actions]]
command = "give $executor_name() minecraft:wooden_sword"
commandType = "SERVER"

[[actions]]
command = "give $executor_name() minecraft:wooden_pickaxe"
commandType = "SERVER"

[[actions]]
command = "give $executor_name() minecraft:wooden_axe"
commandType = "SERVER"

[[actions]]
command = "give $executor_name() minecraft:wooden_shovel"
commandType = "SERVER"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_CUSTOM
command: tools
message: 'Here are some free wooden tools, $executor_name()!'
actions:
  - command: 'give $executor_name() minecraft:wooden_sword'
    commandType: SERVER
  - command: 'give $executor_name() minecraft:wooden_pickaxe'
    commandType: SERVER
  - command: 'give $executor_name() minecraft:wooden_axe'
    commandType: SERVER
  - command: 'give $executor_name() minecraft:wooden_shovel'
    commandType: SERVER
```

We can also add a schedule a start time for commands using `startTime` in milliseconds. This way we can add a 1 second wait time for all the commands to execute. This means we can schedule time accordingly to our liking. So for this example, I will be scheduling them with 1 second wait time between each command.

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_CUSTOM",
    "command": "tools",
    "message": "Here are some free wooden tools, $executor_name()!",
    "actions": [
        {
            "command": "give $executor_name() minecraft:wooden_sword",
            "commandType": "SERVER",
            "startTime": "1000"
        },
        {
            "command": "give $executor_name() minecraft:wooden_pickaxe",
            "commandType": "SERVER",
            "startTime": "1000"
        },
        {
            "command": "give $executor_name() minecraft:wooden_axe",
            "commandType": "SERVER",
            "startTime": "1000"
        },
        {
            "command": "give $executor_name() minecraft:wooden_shovel",
            "commandType": "SERVER",
            "startTime": "1000"
        }
    ]
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_CUSTOM"
command = "tools"
message = "Here are some free wooden tools, $executor_name()!"

[[actions]]
command = "give $executor_name() minecraft:wooden_sword"
commandType = "SERVER"
startTime = "1000"

[[actions]]
command = "give $executor_name() minecraft:wooden_pickaxe"
commandType = "SERVER"
startTime = "1000"

[[actions]]
command = "give $executor_name() minecraft:wooden_axe"
commandType = "SERVER"
startTime = "1000"

[[actions]]
command = "give $executor_name() minecraft:wooden_shovel"
commandType = "SERVER"
startTime = "1000"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_CUSTOM
command: tools
message: 'Here are some free wooden tools, $executor_name()!'
actions:
  - command: 'give $executor_name() minecraft:wooden_sword'
    commandType: SERVER
    startTime: '1000'
  - command: 'give $executor_name() minecraft:wooden_pickaxe'
    commandType: SERVER
    startTime: '1000'
  - command: 'give $executor_name() minecraft:wooden_axe'
    commandType: SERVER
    startTime: '1000'
  - command: 'give $executor_name() minecraft:wooden_shovel'
    commandType: SERVER
    startTime: '1000'
```

You can also add a message in the command action object.

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_CUSTOM",
    "command": "tools",
    "message": "Here are some free wooden tools, $executor_name()!",
    "actions": [
        {
            "command": "give $executor_name() minecraft:wooden_sword",
            "commandType": "SERVER",
            "startTime": "1000",
            "message": "Here is a wooden sword"
        },
        {
            "command": "give $executor_name() minecraft:wooden_pickaxe",
            "commandType": "SERVER",
            "startTime": "1000",
            "message": "Here is a wooden pickaxe"
        },
        {
            "command": "give $executor_name() minecraft:wooden_axe",
            "commandType": "SERVER",
            "startTime": "1000",
            "message": "Here is a wooden axe"
        },
        {
            "command": "give $executor_name() minecraft:wooden_shovel",
            "commandType": "SERVER",
            "startTime": "1000",
            "message": "Here is a wooden shovel, $executor_name()!"
        }
    ]
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_CUSTOM"
command = "tools"
message = "Here are some free wooden tools, $executor_name()!"

[[actions]]
command = "give $executor_name() minecraft:wooden_sword"
commandType = "SERVER"
startTime = "1000"
message = "Here is a wooden sword"

[[actions]]
command = "give $executor_name() minecraft:wooden_pickaxe"
commandType = "SERVER"
startTime = "1000"
message = "Here is a wooden pickaxe"

[[actions]]
command = "give $executor_name() minecraft:wooden_axe"
commandType = "SERVER"
startTime = "1000"
message = "Here is a wooden axe"

[[actions]]
command = "give $executor_name() minecraft:wooden_shovel"
commandType = "SERVER"
startTime = "1000"
message = "Here is a wooden shovel, $executor_name()!"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_CUSTOM
command: tools
message: 'Here are some free wooden tools, $executor_name()!'
actions:
  - command: 'give $executor_name() minecraft:wooden_sword'
    commandType: SERVER
    startTime: '1000'
    message: Here is a wooden sword
  - command: 'give $executor_name() minecraft:wooden_pickaxe'
    commandType: SERVER
    startTime: '1000'
    message: Here is a wooden pickaxe
  - command: 'give $executor_name() minecraft:wooden_axe'
    commandType: SERVER
    startTime: '1000'
    message: Here is a wooden axe
  - command: 'give $executor_name() minecraft:wooden_shovel'
    commandType: SERVER
    startTime: '1000'
    message: 'Here is a wooden shovel, $executor_name()!'
```

**Scenario**

1. Player123 runs `/tools`
2. `Here are some free wooden tools, Player123!`
3. Drops wooden sword, and prints `Here is a wooden sword`; waits for 1 second
4. Drops wooden pickaxe, and prints `Here is a wooden pickaxe`; waits for 1 second
5. Drops wooden axe, and prints `Here is a wooden axe`; waits for 1 second
6. Drops wooden shovel, and prints `Here is a wooden shovel, Player123!`

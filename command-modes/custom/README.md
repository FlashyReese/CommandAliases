# Custom

When using the `COMMAND_CUSTOM` command mode, it creates a new command that can run one or more existing commands with required or optional arguments passthrough.

## Format Structure

The custom command format consists of multiple components and allows for a high degree of customizability. The following components are included:

* [Base Component](base-component.md): The base template for creating a custom command.
* [Child Component](child-component.md): A sub-child component that allows for the inclusion of arguments and sub-commands on a recursive basis.
* [Action Component](action-component.md): The action component allows for the execution of commands, evaluation of command state execution, and the ability to apply additional actions on a recursive basis depending on the command state.
* [Suggestion Provider Component](suggestion-provider-component.md): A suggestion provider for a list of items in the database, or an existing vanilla suggestion provider.

### Full Example

* JSON or JSON5

```json5
{
  "schemaVersion": 1,
  "commandMode": "COMMAND_CUSTOM",
  "command": "hello",
  "permission": 0,
  "message": "hello parent command",
  "children": [
    {
      "child": "name",
      "type": "argument",
      "argumentType": "minecraft:word",
      "suggestionProvider": {
        "suggestionMode": "DATABASE_STARTS_WITH",
        "suggestion": "$executor_name().home.suggestions"
      },
      "permission": 0,
      "message": "Hello name sub command",
      "children": [
        {

        }
      ],
      "actions": [
        {

        }
      ]
    }
  ],
  "actions": [
    {
      "startTime": "1000",
      "id": "generic",
      "command": "say Hello {{name}}",
      "commandType": "SERVER",
      "message": "We tried to say hello {{name}}",
      "requireSuccess": false,
      "messageIfUnsuccessful": "We failed to say hello {{name}}",
      "messageIfSuccessful": "We said hello {{name}}",
      "actionsIfUnsuccessful": [
        {

        }
      ],
      "actionsIfSuccessful": [
        {

        }
      ]
    }
  ]
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_CUSTOM"
command = "hello"
permission = 0
message = "hello parent command"

[[children]]
child = "name"
type = "argument"
argumentType = "minecraft:word"
permission = 0
message = "Hello name sub command"

  [children.suggestionProvider]
  suggestionMode = "DATABASE_STARTS_WITH"
  suggestion = "$executor_name().home.suggestions"

  [[children.children]]

  [[children.actions]]

[[actions]]
startTime = "1000"
id = "generic"
command = "say Hello {{name}}"
commandType = "SERVER"
message = "We tried to say hello {{name}}"
requireSuccess = false
messageIfUnsuccessful = "We failed to say hello {{name}}"
messageIfSuccessful = "We said hello {{name}}"

  [[actions.actionsIfUnsuccessful]]

  [[actions.actionsIfSuccessful]]
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_CUSTOM
command: hello
permission: 0
message: hello parent command
children:
  - child: name
    type: argument
    argumentType: 'minecraft:word'
    permission: 0
    message: Hello name sub command
    suggestionProvider:
      suggestionMode: DATABASE_STARTS_WITH
      suggestion: $executor_name().home.suggestions
    children:
      - {}
    actions:
      - {}
actions:
  - startTime: '1000'
    id: generic
    command: 'say Hello {{name}}'
    commandType: SERVER
    message: 'We tried to say hello {{name}}'
    requireSuccess: false
    messageIfUnsuccessful: 'We failed to say hello {{name}}'
    messageIfSuccessful: 'We said hello {{name}}'
    actionsIfUnsuccessful:
      - {}
    actionsIfSuccessful:
      - {}
```

## Create a custom command using `COMMAND_CUSTOM`

### `Understanding the basics`

To begin, we must set our command mode to `COMMAND_CUSTOM`.

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

With the command mode set, we can now define our new command.

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

Next, let's send a message to the command executor (the player or console that runs the command).

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

To personalize the message, let's get the executor's name and bind it to our message. We can obtain the executor's name using the `$executor_name()` function.

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

To continue, let's give the player a wooden sword. This can be done by using the `actions` field, which takes an array of command action objects as input.

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

To complete this step, we will need to create a command action object inside the custom command.

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

Before we proceed to run the command, we must define the type of execution between `CLIENT` and `SERVER`. Using `CLIENT` will imply that the executors run the command (in this case, the `/give` command), which means that if the executor does not have permission to use the `/give` command, our custom command will error out. To avoid this issue, we can use `SERVER`. Using `SERVER` will imply that the internal or dedicated server executes the custom command, not the executor. In this case, for our `/tools` command, we will need to use `SERVER` since players do not have access to the `/give` command."

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

We can also schedule a start time for commands using `startTime` in milliseconds. This allows us to add a 1 second wait time for all commands to execute, allowing us to schedule the time according to our preferences. In this example, I will be scheduling them with a 1 second wait time between each command.

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

You can also include a message in the command action object.

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

Scenario:

1. Player123 runs the `/tools` command.
2. The message "Here are some free wooden tools, Player123!" is displayed.
3. A wooden sword is dropped, and the message "Here is a wooden sword" is printed; a 1 second wait time is initiated.
4. A wooden pickaxe is dropped, and the message "Here is a wooden pickaxe" is printed; a 1 second wait time is initiated.
5. A wooden axe is dropped, and the message "Here is a wooden axe" is printed; a 1 second wait time is initiated.
6. A wooden shovel is dropped, and the message "Here is a wooden shovel, Player123!" is displayed.


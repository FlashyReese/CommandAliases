# Reassign

When using the `COMMAND_REASSIGN` command mode, it will reassign the existing command to a new command name.

## Create a command alias using `COMMAND_REASSIGN`

First, we need to set our command mode to `COMMAND_REASSIGN`.

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REASSIGN"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REASSIGN"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REASSIGN
```

Now that we have set our command mode, we can define our new command.

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REASSIGN",
    "command": "help"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REASSIGN"
command = "help"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REASSIGN
command: help
```

Finally, we chose what existing command our new command will reassign to. In this case, I chose the `/help` command.

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REASSIGN",
    "command": "help",
    "reassignTo": "minecraft:help"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REASSIGN"
command = "help"
reassignTo = "minecraft:help"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REASSIGN
command: help
reassignTo: 'minecraft:help'
```

Congratulations, you have now reassigned the command `/help` to `/minecraft:help`.

**Note:** The `reassignTo` field must not contain any spaces.

## Examples

#### Game Mode Command

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REASSIGN",
    "command": "gamemode",
    "reassignTo": "gm"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REASSIGN"
command = "gamemode"
reassignTo = "gm"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REASSIGN
command: gamemode
reassignTo: gm
```

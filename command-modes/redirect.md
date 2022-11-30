# Redirect

When using command mode `COMMAND_REDIRECT`, it creates a new command alias that redirects to an existing command with trailing command arguments.

## Create a command alias using `COMMAND_REDIRECT`

First, we need to set our command mode to `COMMAND_REDIRECT`.

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT
```

Now that we have set our command mode, we can define our new command.

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT",
    "command": "s"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT"
command = "s"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT
command: s
```

Finally, we chose what existing command our new command will redirect to. In this case, I chose the `/say` command.

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT",
    "command": "s",
    "redirectTo": "say"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT"
command = "s"
redirectTo = "say"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT
command: s
redirectTo: say
```

## Examples

### Time Command

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT",
    "command": "stime",
    "redirectTo": "time set"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT"
command = "stime"
redirectTo = "time set"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT
command: stime
redirectTo: time set
```

### Scoreboard Command

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT",
    "command": "display",
    "redirectTo": "scoreboard objectives setdisplay"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT"
command = "display"
redirectTo = "scoreboard objectives setdisplay"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT
command: display
redirectTo: scoreboard objectives setdisplay
```

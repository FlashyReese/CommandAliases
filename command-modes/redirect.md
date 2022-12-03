# Redirect

When using the `COMMAND_REDIRECT` command mode, it creates a new command alias that redirects to an existing command along with its trailing command arguments.

## Create a command alias using `COMMAND_REDIRECT`

To begin, we must set our command mode to `COMMAND_REDIRECT`.

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

With the command mode set, we can now define our new command.

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

Finally, we must choose the existing command that our new command will redirect to. In this case, I have chosen the `/say` command.

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

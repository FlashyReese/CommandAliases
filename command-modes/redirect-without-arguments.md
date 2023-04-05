# Redirect without arguments

When using the `COMMAND_REDIRECT_NOARG` command mode, it creates a new command alias that redirects to an existing command **without** its trailing command arguments.

## Create a command alias using `COMMAND_REDIRECT_NOARG`

To begin, we must set our command mode to `COMMAND_REDIRECT_NOARG`.

* JSON or JSON5

```json
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

With the command mode set, we can now define our new command.

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT_NOARG",
    "command": "easy"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT_NOARG"
command = "easy"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT_NOARG
command: easy
```

Finally, we must choose the existing command that our new command will redirect to. In this case, I have chosen the `/difficulty easy` command.

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT_NOARG",
    "command": "easy",
    "redirectTo": "difficulty easy"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT_NOARG"
command = "easy"
redirectTo = "difficulty easy"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT_NOARG
command: easy
redirectTo: difficulty easy
```

Next, let's create another one for the `/gamemode survival` command.

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT_NOARG",
    "command": "gm 0",
    "redirectTo": "gamemode survival"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT_NOARG"
command = "gm 0"
redirectTo = "gamemode survival"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT_NOARG
command: gm 0
redirectTo: gamemode survival
```

## Examples

### Time Command

#### **Day**

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT_NOARG",
    "command": "day",
    "redirectTo": "time set day"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT_NOARG"
command = "day"
redirectTo = "time set day"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT_NOARG
command: day
redirectTo: time set day
```

#### **Night**

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT_NOARG",
    "command": "night",
    "redirectTo": "time set night"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT_NOARG"
command = "night"
redirectTo = "time set night"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT_NOARG
command: night
redirectTo: time set night
```

### Game Mode Command (1.19.2 and below)

#### **Survival**

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT_NOARG",
    "command": "gm 0",
    "redirectTo": "gamemode survival"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT_NOARG"
command = "gm 0"
redirectTo = "gamemode survival"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT_NOARG
command: gm 0
redirectTo: gamemode survival
```

#### **Creative**

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT_NOARG",
    "command": "gm 1",
    "redirectTo": "gamemode creative"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT_NOARG"
command = "gm 1"
redirectTo = "gamemode creative"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT_NOARG
command: gm 1
redirectTo: gamemode creative
```

#### **Adventure**

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT_NOARG",
    "command": "gm 2",
    "redirectTo": "gamemode adventure"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT_NOARG"
command = "gm 2"
redirectTo = "gamemode adventure"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT_NOARG
command: gm 2
redirectTo: gamemode adventure
```

#### **Spectator**

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT_NOARG",
    "command": "gm 3",
    "redirectTo": "gamemode spectator"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT_NOARG"
command = "gm 3"
redirectTo = "gamemode spectator"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT_NOARG
command: gm 3
redirectTo: gamemode spectator
```

### Difficulty Command

#### **Peaceful**

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT_NOARG",
    "command": "peaceful",
    "redirectTo": "difficulty peaceful"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT_NOARG"
command = "peaceful"
redirectTo = "difficulty peaceful"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT_NOARG
command: peaceful
redirectTo: difficulty peaceful
```

#### **Easy**

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT_NOARG",
    "command": "easy",
    "redirectTo": "difficulty easy"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT_NOARG"
command = "easy"
redirectTo = "difficulty easy"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT_NOARG
command: easy
redirectTo: difficulty easy
```

#### **Normal**

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT_NOARG",
    "command": "normal",
    "redirectTo": "difficulty normal"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT_NOARG"
command = "normal"
redirectTo = "difficulty normal"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT_NOARG
command: normal
redirectTo: difficulty normal
```

#### **Hard**

* JSON or JSON5

```json
{
    "schemaVersion": 1,
    "commandMode": "COMMAND_REDIRECT_NOARG",
    "command": "hard",
    "redirectTo": "difficulty hard"
}
```

* TOML

```toml
schemaVersion = 1
commandMode = "COMMAND_REDIRECT_NOARG"
command = "hard"
redirectTo = "difficulty hard"
```

* YAML

```yaml
schemaVersion: 1
commandMode: COMMAND_REDIRECT_NOARG
command: hard
redirectTo: difficulty hard
```

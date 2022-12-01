# Base Component

* JSON or JSON5

```json5
{
    "schemaVersion": 1, // Required | Schema Version
    "commandMode": "COMMAND_CUSTOM", // Required | Custom Command Format
    "command": "hello", // Required | Parent command name
    "permission": 0, // Optional | Default: 0 | If LuckPerms is not installed will fallback to these permission level
    "message": "hello parent command", // Optional | Default: Empty | Executor gets a local message if parent command is executed
    "children": [ // Optional | List of sub-child commands
        {
            
        }
    ],
    "actions": [ // Optional | List of actions
        {
            
        }
    ]
}
```

* TOML

```toml
schemaVersion = 1 # Required | Schema Version
commandMode = "COMMAND_CUSTOM" # Required | Custom Command Format
command = "hello" # Required | Parent command name
permission = 0 # Optional | Default: 0 | If LuckPerms is not installed will fallback to these permission level
message = "hello parent command" # Optional | Default: Empty | Executor gets a local message if parent command is executed

[[children]] # Optional | List of sub-child commands

[[actions]] # Optional | List of actions
```

* YAML

```yaml
schemaVersion: 1 # Required | Schema Version
commandMode: COMMAND_CUSTOM # Required | Custom Command Format
command: hello # Required | Parent command name
permission: 0 # Optional | Default: 0 | If LuckPerms is not installed will fallback to these permission level
message: hello parent command # Optional | Default: Empty | Executor gets a local message if parent command is executed
children: # Optional | List of sub-child commands
  - {}
actions: # Optional | List of actions
  - {}
```

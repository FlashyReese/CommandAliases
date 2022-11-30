# Child Component

* JSON or JSON5

```json5
{
    "child": "name", // Required | Sub-child command placeholder 
    "type": "argument", // Required | Type of sub-child command literal or argument
    "argumentType": "minecraft:word", // Required if type is argument | Argument Type
    "suggestionProvider": {}, // Optional | Suggestion Provider
    "permission": 0, // Optional | If LuckPerms is not installed will fallback to these permission level
    "message": "Hello name sub command", // Optional | Executor gets a local message if child command is executed
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
child = "name" # Required | Sub-child command placeholder 
type = "argument" # Required | Type of sub-child command literal or argument
argumentType = "minecraft:word" # Required if type is argument | Argument Type
suggestionProvider = { } # Optional | Suggestion Provider
permission = 0 # Optional | If LuckPerms is not installed will fallback to these permission level
message = "Hello name sub command" # Optional | Executor gets a local message if child command is executed

[[children]] # Optional | List of sub-child commands

[[actions]] # Optional | List of actions
```

* YAML

```yaml
child: name # Required | Sub-child command placeholder 
type: argument # Required | Type of sub-child command literal or argument
argumentType: 'minecraft:word' # Required if type is argument | Argument Type
suggestionProvider: {} # Optional | Suggestion Provider
permission: 0 # Optional | If LuckPerms is not installed will fallback to these permission level
message: Hello name sub command # Optional | Executor gets a local message if child command is executed
children: # Optional | List of sub-child commands
  - {}
actions: # Optional | List of actions
  - {}
```

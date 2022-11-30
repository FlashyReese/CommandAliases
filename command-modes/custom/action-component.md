# Action Component

* JSON or JSON5

```json5
{
    "startTime": "1000", // Optional | Default: "" | Sets the start time for this current action
    "id": "generic", // Optional | Default: "generic" | Identifier when scheduling actions, can be used to remove an action that is yet to be triggered
    "command": "say Hello {{name}}", // Optional | Command to execute will be processed to map variables and process functions
    "commandType": "SERVER", // Optional | Required if command is present
    "message": "We tried to say hello {{name}}", // Optional | Executor gets a local message
    "requireSuccess": false, // Optional | Default: false | If set to true the subsequent action will not be scheduled if this current command fails to execute therefore stopping the command completely
    "messageIfUnsuccessful": "We failed to say hello {{name}}", // Optional | Executor gets a local message if command fails
    "messageIfSuccessful": "We said hello {{name}}", // Optional | Executor gets a local message if command success
    "actionsIfUnsuccessful": [ // Optional | This set of actions gets executed if the command fails
        {
        
        }
    ],
    "actionsIfSuccessful": [ // Optional | This set of actions gets executed if the command succeeds
        {
        
        }
    ]
}
```

* TOML

```toml
startTime = "1000" # Optional | Default: "" | Sets the start time for this current action
id = "generic" # Optional | Default: "generic" | Identifier when scheduling actions, can be used to remove an action that is yet to be triggered
command = "say Hello {{name}}" # Optional | Command to execute will be processed to map variables and process functions
commandType = "SERVER" # Optional | Required if command is present
message = "We tried to say hello {{name}}" # Optional | Executor gets a local message
requireSuccess = false # Optional | Default: false | If set to true the subsequent action will not be scheduled if this current command fails to execute therefore stopping the command completely
messageIfUnsuccessful = "We said hello {{name}}" # Optional | Executor gets a local message if command fails
messageIfSuccessful = "We failed to say hello {{name}}" # Optional | Executor gets a local message if command success

[[actionsIfUnsuccessful]] # Optional | This set of actions gets executed if the command fails

[[actionsIfSuccessful]] # Optional | This set of actions gets executed if the command succeeds
```

* YAML

```yaml
startTime: '1000' # Optional | Default: '' | Sets the start time for this current action
id: generic # Optional | Default: generic | Identifier when scheduling actions, can be used to remove an action that is yet to be triggered
command: 'say Hello {{name}}' # Optional | Command to execute will be processed to map variables and process functions
commandType: SERVER # Optional | Required if command is present
message: 'We tried to say hello {{name}}' # Optional | Executor gets a local message
requireSuccess: false # Optional | Default: false | If set to true the subsequent action will not be scheduled if this current command fails to execute therefore stopping the command completely
messageIfUnsuccessful: 'We said hello {{name}}' # Optional | Executor gets a local message if command fails
messageIfSuccessful: 'We failed to say hello {{name}}' # Optional | Executor gets a local message if command success
actionsIfUnsuccessful: # Optional | This set of actions gets executed if the command fails
  - {}
actionsIfSuccessful: # Optional | This set of actions gets executed if the command succeeds
  - {}
```

# Action Component

Actions define what happens when a custom command node is executed. An action can run a command, send a message, delay execution, branch on command success, or stop later actions when a required step fails.

Actions are used in the `actions` field of a base command or child command.

## Fields

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `startTime` | string | No | Delay before this action runs, in milliseconds. Empty or missing means no delay. |
| `id` | string | No | Scheduler id. Used by `/commandaliases scheduler match` and `/commandaliases scheduler remove`. |
| `command` | string | No | Command to execute. Placeholders and functions are processed before execution. |
| `commandType` | `CLIENT` or `SERVER` | Required when `command` is set | Chooses whether the action command is executed by the client/player side or server command source. |
| `message` | string | No | Message sent to the command executor after this action is processed. |
| `requireSuccess` | boolean | No | If `true`, later sibling actions are skipped when this action fails. |
| `messageIfSuccessful` | string | No | Message sent when `command` succeeds. |
| `messageIfUnsuccessful` | string | No | Message sent when `command` fails. |
| `actionsIfSuccessful` | action list | No | Nested actions that run when `command` succeeds. |
| `actionsIfUnsuccessful` | action list | No | Nested actions that run when `command` fails. |

## Minimal action

```toml
[[actions]]
command = "say Hello $executor_name()"
commandType = "SERVER"
```

## Action with feedback

```toml
[[actions]]
command = "give $executor_name() minecraft:bread 8"
commandType = "SERVER"
messageIfSuccessful = "You received bread."
messageIfUnsuccessful = "Could not give bread."
```

## Required action

Use `requireSuccess = true` when later actions depend on this action.

```toml
[[actions]]
command = "clear $executor_name() minecraft:diamond 1"
commandType = "SERVER"
requireSuccess = true
messageIfUnsuccessful = "You need a diamond."

[[actions]]
command = "give $executor_name() minecraft:emerald 4"
commandType = "SERVER"
messageIfSuccessful = "Trade complete."
```

If the player does not have a diamond, the emerald action is skipped.

## Delayed action

`startTime` delays the action in milliseconds.

```toml
[[actions]]
startTime = "60000"
id = "$executor_name().timer"
command = "tellraw $executor_name() \"One minute passed.\""
commandType = "SERVER"
```

The `id` can later be used by:

```text
/commandaliases scheduler remove <eventName>
```

## Success branch

```toml
[[actions]]
command = "commandaliases database match $executor_name().kit.claimed"
commandType = "SERVER"

[[actions.actionsIfSuccessful]]
message = "You already claimed this kit."

[[actions.actionsIfUnsuccessful]]
command = "give $executor_name() minecraft:iron_sword"
commandType = "SERVER"
requireSuccess = true

[[actions.actionsIfUnsuccessful]]
command = "commandaliases database put $executor_name().kit.claimed true"
commandType = "SERVER"
messageIfSuccessful = "Kit claimed."
```

Nested branches are useful, but they are harder to read than a flat action list. Prefer `requireSuccess` for simple early exits.

## JSON shape

```json5
{
  "startTime": "1000",
  "id": "generic",
  "command": "say Hello {{name}}",
  "commandType": "SERVER",
  "message": "Tried to say hello {{name}}.",
  "requireSuccess": false,
  "messageIfSuccessful": "Said hello {{name}}.",
  "messageIfUnsuccessful": "Could not say hello {{name}}.",
  "actionsIfSuccessful": [],
  "actionsIfUnsuccessful": []
}
```

## TOML shape

```toml
startTime = "1000"
id = "generic"
command = "say Hello {{name}}"
commandType = "SERVER"
message = "Tried to say hello {{name}}."
requireSuccess = false
messageIfSuccessful = "Said hello {{name}}."
messageIfUnsuccessful = "Could not say hello {{name}}."

[[actionsIfSuccessful]]

[[actionsIfUnsuccessful]]
```

## YAML shape

```yaml
startTime: '1000'
id: generic
command: 'say Hello {{name}}'
commandType: SERVER
message: 'Tried to say hello {{name}}.'
requireSuccess: false
messageIfSuccessful: 'Said hello {{name}}.'
messageIfUnsuccessful: 'Could not say hello {{name}}.'
actionsIfSuccessful: []
actionsIfUnsuccessful: []
```

## Known limitation

Delayed and nested actions are scheduled on the tick loop. The current scheduler can queue follow-up work before a delayed nested action has produced a result. Avoid building aliases where an immediate parent action depends on the success state of a delayed child action.

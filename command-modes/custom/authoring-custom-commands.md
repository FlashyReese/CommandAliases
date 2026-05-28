# Authoring Custom Commands

Custom commands are the most powerful part of Command Aliases. They are also the easiest part to make confusing if too much logic is packed into one file without a clear structure.

This page gives a practical writing style for custom command aliases.

## Mental model

A custom command has three layers:

1. **Command shape**: the root command and its literals or arguments.
2. **Inputs**: values typed by the user, referenced with `{{placeholder}}`.
3. **Actions**: commands and messages executed when a command node is reached.

For example:

```toml
schemaVersion = 1
commandMode = "COMMAND_CUSTOM"
command = "hello"

[[children]]
child = "name"
type = "argument"
argumentType = "minecraft:word"
message = "Hello, {{name}}!"
```

This creates:

```text
/hello <name>
```

When a player runs `/hello Steve`, `{{name}}` becomes `Steve`.

## Prefer TOML for hand-written aliases

Command Aliases supports JSON, JSON5, TOML, and YAML. For hand-written command aliases, TOML is usually the easiest format to maintain because nested actions are readable and strings do not need as much escaping.

JSON is still useful when generating aliases from a tool.

## Keep command shape shallow

A command with one or two child levels is usually easy to read:

```text
/home <name>
/admin home <player> <name>
```

Deep command trees are valid, but they become harder to document and troubleshoot. If a command needs many independent branches, consider splitting it into several aliases.

## Use actions as steps

Read `actions` as a sequence of steps. Each action can run a command, send a message, or both.

```toml
[[actions]]
command = "give $executor_name() minecraft:bread 8"
commandType = "SERVER"
messageIfSuccessful = "You received bread."
messageIfUnsuccessful = "Could not give bread."
```

Use `requireSuccess = true` when later actions depend on the current action.

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

If the `clear` command fails, the `give` command is skipped.

## Avoid deeply nested failure branches

Nested `actionsIfSuccessful` and `actionsIfUnsuccessful` are powerful, but they can make aliases hard to read.

If possible, write the main path as normal actions and use `requireSuccess` to stop early:

```toml
[[actions]]
command = "commandaliases database match $executor_name().home.{{name}}.command"
commandType = "SERVER"
requireSuccess = true
messageIfUnsuccessful = "That home does not exist."

[[actions]]
command = "$get_database_value($executor_name().home.{{name}}.command)"
commandType = "SERVER"
messageIfSuccessful = "Teleporting to {{name}}."
```

Use nested branches only when you genuinely need alternate behavior.

## Name database keys consistently

Database-backed aliases are easier to maintain when every key follows one pattern.

Recommended pattern:

```text
<player>.<feature>.<record>.<field>
```

Examples:

```text
$executor_name().home.main.command
$executor_name().home.suggestions.main
$executor_name().tpa.last_player
```

Avoid mixing several naming styles in the same feature.

## Choose commandType deliberately

`commandType` controls who executes the action command.

| Value | Meaning | Use when |
| --- | --- | --- |
| `CLIENT` | The command is executed from the client/player side. | You want the executor's normal permissions and context. |
| `SERVER` | The command is executed by the server command source. | The alias intentionally performs privileged server work. |

Use `SERVER` carefully. It can let a low-permission command trigger high-permission behavior if the alias permission is too broad.

## Use permissions on public commands

The root custom command and child nodes can include `permission`.

```toml
permission = 2
```

With LuckPerms, the generated permission nodes are based on the command path. Without LuckPerms, the numeric value is used as the vanilla permission level fallback.

## Delay only when needed

`startTime` delays an action in milliseconds.

```toml
[[actions]]
startTime = "60000"
id = "$executor_name().cooldown"
command = "say one minute later"
commandType = "SERVER"
```

Delayed actions are useful for expirations and cooldowns. Avoid relying on complex success/failure branching across delayed nested actions until the action scheduler is redesigned.

## Test with reload and logs

After editing aliases:

```text
/commandaliases reload
```

Watch the server log for the directory tree. A successfully parsed file is marked:

```text
my-alias.toml - Successfully loaded
```

If the file loads but the command does not behave correctly, enable debug mode in `command-aliases-config.json` and test the smallest command path first.
